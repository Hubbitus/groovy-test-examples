package groovy.jms

import groovy.jms.provider.ActiveMQJMSProvider
import groovy.jms.provider.JMSProvider
import javax.jms.Connection
import javax.jms.ConnectionFactory
import javax.jms.Session
import org.apache.log4j.Logger
import javax.jms.MessageListener
import javax.jms.Topic
import javax.jms.Queue
import javax.jms.MessageConsumer
import javax.jms.TopicSubscriber
import javax.jms.QueueReceiver
import javax.jms.MessageProducer
import javax.jms.Destination
import javax.jms.Message

/**
 * Connection and session is either provided to constructor, or be created in the construction. In either case, connection
 * will not be re-created.
 *
 * when connection and session are provided, autoClose is set to false, otherwise, it's true
 */
class JMS extends AbstractJMS {
    static Logger logger = Logger.getLogger(JMS.class.name)
    static final hostName;
    static {
        try {hostName = InetAddress.getLocalHost()?.hostName} catch (e) { logger.error("fail to get local hostname on JMS static init")}
    }
    public static String SYSTEM_PROP_JMSPROVIDER = "groovy.jms.provider"
    public static final int DEFAULT_SESSION_ACK = Session.AUTO_ACKNOWLEDGE; //TODO consider to add support for other ack mode
    boolean started = false, closed = false, autoClose = true, initialized = false;
    static JMSProvider provider;
    Connection connection;//TODO add @delegate after upgraded to 1.6beta2
    Session session; //TODO add @delegate after upgraded to 1.6beta2
    MessageProducer producer; // optional, created upon the first use
    MessageConsumer consumer; // optional, created upon the first use


    JMS(ConnectionFactory r, Closure exec) { this(r, null, exec) }

    JMS(Connection r, Closure exec) { this(r, null, exec) }

    JMS(List r, Closure exec) { this(r, null, exec) }

    /**
     * @resource either a single object or an ArrayList, it accepts ConnectionFactory, Connection, and Session. Any
     * resource must be compatiable.
     * if both connection and connection factory are specified, the factory is ignored
     * if Session is specified, the associated Connection is required.
     * if none are specified, a default ConnectionFactory will be used
     * Only one resource of each kind shall be used. The first one is taken if there are duplication
     */
    JMS(resource = null, Map cfg = null, Closure exec = null) {
        ConnectionFactory f = (resource instanceof List) ? resource.find {it instanceof ConnectionFactory} : (resource instanceof ConnectionFactory) ? resource : null
        Connection c = (resource instanceof List) ? resource.find {it instanceof Connection} : (resource instanceof Connection) ? resource : null
        Session s = (resource instanceof List) ? resource.find {it instanceof Session} : (resource instanceof Session) ? resource : null

        //validate
        if (s && !c) throw new IllegalArgumentException("when Session is provided, it is required to provide a Connection - f: $f, c: $c, s: $s")

        if ((!f && !c) || (f && !c)) {
            this.connection = ((f) ?: getDefaultConnectionFactory()).createConnection()
            this.connection.clientID = getDefaultClientID()
            this.session = this.connection.createSession(false, DEFAULT_SESSION_ACK)
        } else if (c) {
            if (!f) logger.warn("JMS() - connection and factory are both provided. factory is ignored - f: $f, c: $c, s: $s")
            this.connection = c;
            if (!c) this.connection.clientID = getDefaultClientID()
            this.session = s ?: c.createSession(false, DEFAULT_SESSION_ACK)
        } else {
            throw new UnsupportedOperationException("unhandled case - f: $f, c: $c, s: $s")
        }

        if (cfg?.containsKey('autoClose')) autoClose = cfg.'autoClose'

        org.apache.log4j.MDC.put("tid", Thread.currentThread().getId());
        JMS.setThreadLocal(this)
        if (logger.isTraceEnabled()) logger.trace("JMS() - constructed -  this: ${toString()}, f: $f, c: $c, s: $s, cfg: $cfg, exec?: ${exec != null}  ")
        if (exec) run(exec);
    }

    static run(Closure c) {
        def result;
        def jms = JMS.getThreadLocal()
        if (!jms?.started) jms.start();
        use(JMSCategory) {
            //delegate.set(); //set JMS to Category ThreadLocal
            switch (c.parameterTypes.length) {
                case 0: result = c(); break;
                case 1: result = c(jms); break;
                case 2: result = c(jms, jms.connection); break;
                default: result = c(jms, jms.connection, jms.session); break;
            }
        }
        if (jms?.autoClose && !jms.closed) jms.close();
        return result;
    }

    static void jms(Closure exec) { jms(getDefaultConnectionFactory(), exec)}

    static void jms(ConnectionFactory f, Closure exec) {
        if (!exec || !f) throw new IllegalArgumentException("ConnectionFactory and Execution closure must not be null")
        def jms = new JMS(f, exec)
    }

    static void jms(Connection c, Session s = null, Closure exec) {
        if (!c || !exec) throw new IllegalArgumentException("Connection and Execution closure must not be null")
        def jms = (s) ? new JMS(c, s, exec) : new JMS(c, exec)
    }


    synchronized void start() {
        //if (logger.isTraceEnabled()) logger.trace("start()")
        connection.start()
        JMS.setThreadLocal(this)
        started = true;
    }

    void connect() { // connection.start()
        connection.start()
    }

    synchronized static ConnectionFactory getDefaultConnectionFactory() {
        if (provider) return provider.getConnectionFactory()
        String className = System.getProperty(SYSTEM_PROP_JMSPROVIDER)
        JMSProvider provider = className ? Class.forName(className)?.newInstance() : new ActiveMQJMSProvider();
        return provider.connectionFactory;
    }

    static String getDefaultClientID() { return "$hostName:${Thread.currentThread().id}:${System.currentTimeMillis()}" }

    static Connection getConnectionWithDefaultClientID(ConnectionFactory f) {
        Connection c = f.createConnection(); c.setClientID(getDefaultClientID())
        return c;
    }


    static void close() {
        if (logger.isTraceEnabled()) logger.trace("JMS.close()")
        getThreadLocal()?.closed = true; //TODO review if it is still needed
        JMS.clearThreadLocal(true)
    }

    void eachMessage(String queueName, Map cfg = null, Closure c) {
        if (!started) start();
        use(JMSCoreCategory) {
            if (!session) throw new IllegalStateException("session was not available")
            session.queue(queueName).receiveAll(cfg?.'within' ?: null).each {m -> c(m)}
        }
        if (autoClose && !closed) close();
    }

    def firstMessage(String queueName, Map cfg = null, Closure c) {
        if (!started) start();
        use(JMSCoreCategory) {
            if (!session) throw new IllegalStateException("session was not available")
            c(session.queue(queueName).receive(cfg?.'within' ?: null))
        }
        if (autoClose && !closed) close();
    }

    Map messageConsumers = Collections.synchronizedMap(new WeakHashMap());

    /**
     * support any of topic,queue,fromTopic or fromQueue
     */
    def onMessage(Map cfg, Object target) {
        try {
            if (!started) start();
            use(JMSCoreCategory) {
                if (!session) throw new IllegalStateException("session was not available")
                if (!cfg || !(cfg.containsKey('topic') || cfg.containsKey('queue') || cfg.containsKey('fromTopic') || cfg.containsKey('fromQueue'))) throw new IllegalArgumentException("first argument of onMessage must have a Map with 'queue' or 'topic' key")

                MessageListener listener = target instanceof MessageListener ? target : target as MessageListener
                int statsTopicSubscribed = 0, statsQueueSubscribed = 0;
                if (cfg.containsKey('topic') || cfg.containsKey('fromTopic')) {
                    def topicDest = [], t = cfg.get('topic'), ft = cfg.get('fromTopic')
                    if (t) { if (t instanceof Collection) { topicDest += t} else { topicDest << t} }
                    if (ft) { if (ft instanceof Collection) { topicDest += ft} else { topicDest << ft} }

                    topicDest.each {
                        Topic topic = it instanceof Topic ? it : session.topic(it)
                        messageConsumers.put(topic.subscribe(cfg, listener), topic); //TODO no need to put value
                    }
                    statsTopicSubscribed = topicDest.size()
                }

                if (cfg.containsKey('queue') || cfg.containsKey('fromQueue')) {
                    def queueDest = [], q = cfg.get('queue'), fq = cfg.get('fromQueue')
                    if (q) { if (q instanceof Collection) { queueDest += q} else { queueDest << q} }
                    if (fq) { if (fq instanceof Collection) { queueDest += fq} else { queueDest << fq} }

                    queueDest.each {
                        Queue queue = (it instanceof Queue) ? it : session.queue(it);
                        messageConsumers.put(queue.listen(listener), queue);//TODO no need to put value
                    }
                    statsQueueSubscribed = queueDest.size()
                }
                JMS.getThreadLocal().connection.start()
                if (logger.isTraceEnabled()) logger.trace("onMessage() - subscribed to ${statsTopicSubscribed} topic(s) and ${statsQueueSubscribed} queue(s), cfg: $cfg, registered consumers: ${messageConsumers?.size()}")
            }
        } catch (e) {
            logger.error("onMessage() - exception - cfg: $cfg", e);
        }
        if (autoClose && !closed) close();
    }

    def stopMessage(Map cfg) {
        try {
            if (!started) throw new IllegalStateException("jms is not started yet")
            if (!cfg || !(cfg.containsKey('topic') || cfg.containsKey('queue'))) throw new IllegalArgumentException("first argument of onMessage must have a Map with 'queue' or 'topic' key")

            use(JMSCoreCategory) {
                if (!session) throw new IllegalStateException("session was not available")
                int statsTopicSubscribed = 0, statsQueueSubscribed = 0;
                if (cfg.containsKey('topic') || cfg.containsKey('fromTopic')) {
                    def topicDest = []
                    def t = cfg.get('topic'), ft = cfg.get('fromTopic')
                    if (t) { if (t instanceof Collection) { topicDest += t} else { topicDest << t} }
                    if (ft) { if (ft instanceof Collection) { topicDest += ft} else { topicDest << ft} }

                    topicDest.each {String topicName ->
                        def consumers = messageConsumers.keySet().findAll {MessageConsumer c -> c instanceof TopicSubscriber && c.topic.topicName == topicName}
                        consumers.each {MessageConsumer c ->
                            Topic topic = c.topic;
                            c.setMessageListener(null)
                            c.close()
                            topic.unsubscribe();
                            if (logger.isTraceEnabled()) logger.trace("stopMessage() - dest: $cfg, consumer: $c")
                            messageConsumers.remove(c)
                        }
                    }
                    statsTopicSubscribed = topicDest.size()
                }

                if (cfg.containsKey('queue') || cfg.containsKey('fromQueue')) {
                    def queueDest = []
                    def q = cfg.get('queue'), fq = cfg.get('fromQueue')
                    if (q) { if (q instanceof Collection) { queueDest += q} else { queueDest << q} }
                    if (fq) { if (fq instanceof Collection) { queueDest += fq} else { queueDest << fq} }

                    queueDest.each {String queueName ->
                        def consumers = messageConsumers.keySet().findAll {MessageConsumer c -> c instanceof QueueReceiver && c.queue.queueName == queueName}
                        consumers.each {MessageConsumer c ->
                            Queue queue = c.queue;
                            c.setMessageListener(null)
                            c.close()
                            if (logger.isTraceEnabled()) logger.trace("stopMessage() - dest: $cfg, consumer: $c")
                            messageConsumers.remove(c)
                        }
                    }
                    statsQueueSubscribed = queueDest.size()
                }
                if (logger.isTraceEnabled()) logger.trace("stopMessage() - unsubscribed to ${statsTopicSubscribed} topic(s) and ${statsQueueSubscribed} queue(s), cfg: $cfg, registered consumers: ${messageConsumers?.size()}")
            }
        } catch (e) {
            logger.error("stopMessage() - exception - dest: $cfg", e);
        }
        if (autoClose && !closed) close();
    }

    /**
     * jms.receive(){ messages-> }* jms.receive(with:{messages->})
     * def result = jms.receive()
     *
     * a topic doesn't support synchronous retrieval, any specified topic will be subscribed instead.
     *
     * @return if 'with' is specified, there will be no return value; otherwise, return an arraylist of messages
     */
    def receive(Map params, Closure with = null) {
        if (!started) start();
        if (!(params.containsKey('fromQueue') || params.containsKey('fromTopic'))) throw new IllegalArgumentException("either toQueue or toTopic must present")
        //if (!with && !params.containsKey('with')) throw new IllegalArgumentException("receive message must provide a \"with\"")

        def result = [];
        def fromQueue = params.'fromQueue', fromTopic = params.'fromTopic'
        with = (with) ?: params.'with'

        use(JMSCoreCategory) {
            if (!session) throw new IllegalStateException("session was not available")

            if (fromQueue) {
                int timeout = (params.'within') ? Integer.valueOf(params.'within') : 0
                if (fromQueue instanceof Collection) {
                    result += fromQueue.collect {q -> session.queue(q).receiveAll(timeout)}
                } else {
                    result += session.queue(fromQueue).receiveAll(timeout)
                }
            }

            if (fromTopic) {
                if (fromTopic instanceof Collection) {
                    fromTopic.each {t -> session.topic(t).subscribe(with)}
                } else {
                    session.topic(fromTopic).subscribe(with)
                }
            }
        }
        if (autoClose && !closed) close();
        if (with) { with(result) } else { return result; }
        return result;
    }


    static final SEND_DEST_PARAMS = ['toQueue', 'queue', 'toTopic', 'topic', 'toDest', 'dest', 'reply']

    /**
     * toQueue/toTopic - at least one must be provided. they may be a String or Queue/Topic or a Collection of String/Queue/Topic
     * message/data - synonym of each other, at least one must be provided. a message/data may be a JMS Message, a
     * String (Text Message), a Map (Map Message), or any serializable Java object. Stream is not supported yet.
     */
    def send(Map spec) {
        try {
            if (!started) start();
            if (!spec) throw new IllegalArgumentException("spec shall not be null")
            if (!spec.keySet().any {it in JMS.SEND_DEST_PARAMS}) throw new IllegalArgumentException("send() - required dest params is not found. required: ${JMS.SEND_DEST_PARAMS}")
            if (!spec.containsKey('message') && !spec.containsKey('data')) throw new IllegalArgumentException("send message must have a \"message\"")
            if (spec.containsKey('reply') && !(spec.'reply' instanceof Message)) throw new IllegalArgumentException("reply must take a JMS Message as value, reply.class: ${spec.'reply'.getClass()}, reply: ${spec.'reply'}")
            if (spec.containsKey('reply') && !(spec.'reply'.getJMSReplyTo())) throw new IllegalArgumentException("replying message must have a JMSReplyTo destination. reply: $reply")
            if (spec.containsKey('properties') && !(spec.'properties' instanceof Map)) throw new IllegalArgumentException("'properties' must be a Map. properties.class: ${spec.'properties'.getClass()}, properties: ${spec.'properties'}")

            //dest: toQueue, toTopic ; handle String or List<String>
            //replyTo: queueName or [destName: type];
            def message = spec.remove('message') ?: spec.remove('data')

            def toQueue, toTopic, toDest
            if (spec.containsKey('reply')) {
                Message replyMsg = spec.remove('reply')
                if (!spec.containsKey('correlationID') && replyMsg.JMSCorrelationID) spec.'correlationID' = replyMsg.JMSCorrelationID
                toDest = replyMsg.getJMSReplyTo()
                if (!spec.containsKey('copyProperties') || !spec.'copyProperties') {
                    spec.'properties' = (!spec.containsKey('properties')) ? [:] : spec.'properties'
                    replyMsg.propertyNames.each { if (!spec.'properties'.containsKey(it)) spec.'properties'."$it" = replyMsg."$it"}
                }
            } else {
                toQueue = spec.remove('toQueue') ?: spec.remove('queue')
                toTopic = spec.remove('toTopic') ?: spec.remove('topic')
                toDest = spec.remove('toDest') ?: spec.remove('dest')
            }


            if (toDest && toDest instanceof Collection && !toDest.every {it instanceof Destination}) throw new IllegalArgumentException("toDest collection argument is not valid")
            else if (toDest && !(toDest instanceof Destination)) throw new IllegalArgumentException("non-collection toDest must be a JMS destination, toDest.class: ${toDest.getClass()}, toDest: $toDest")


            use(JMSCoreCategory) {
                if (!session) throw new IllegalStateException("session was not available")

                if (logger.isTraceEnabled()) logger.trace("send() - spec: $spec, toQueue: $toQueue, toTopic: $toTopic, toDest: $toDest")

                if (toQueue) {
                    if (toQueue instanceof Collection) {
                        toQueue.each {q -> session.queue(q).send(message, spec)}
                    } else {
                        session.queue(toQueue).send(message, spec)
                    }
                }

                if (toTopic) {
                    if (toTopic instanceof Collection) {
                        toTopic.each {t -> session.topic(t).send(message, spec)}
                    } else {
                        session.topic(toTopic).send(message, spec)
                    }
                }

                if (toDest) {
                    if (toDest instanceof Collection) {
                        toDest.each {Destination d -> d.send(message, spec)}
                    } else {
                        toDest.send(message, spec)
                    }
                }
            }
            if (autoClose && !closed) close();
        } catch (e) {logger.error("send() - error", e)}
    }


    String toString() { return "JMS { ${super.toString()}, nestLevel: $nestLevel, session: $session, connection: $connection, autoClose: $autoClose"}

}