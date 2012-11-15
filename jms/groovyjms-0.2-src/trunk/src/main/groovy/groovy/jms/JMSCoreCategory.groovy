package groovy.jms

import javax.jms.*
import org.apache.log4j.Logger
import java.lang.reflect.Method

/**
 * 1. JMS ConnectionFactory, Connection, and Session are top level objects that essentially can be used interchangable.
 *  - use(JMS){ jms.connect().queue()...} <br>
 *  - use(JMS){ jms.session().topic()...} <br>
 *  - use(JMS){ jms.queue() or jms.topic() } <br>
 *
 *  In the first time any of the above method is called, a connection and session are established in the thread context.
 * A JMS instance is created to store the connection factory, connection and session. The use of connect() or session()
 * is for obtaining the session or connection for direct JMS usage in case where optimization is needed. The JMS category
 * always re-use the connection/session until close() is called.
 *
 *  after usage, the close() must be classed to clean up ThreadLocal instance. e.g. jms.close(), connection.close(), session.close().
 * Notice that closing a session automatically close the connection in JMS Category's context
 *
 * 2. a Queue or Topic must be obtained to send message
 *
 * 3. send(), receive(), receiveAll(), subscribe() are the actual methods to interact with JMS provider
 *
 *
 * TODO:
 * keep commit for connectionfactory, connection, rename session to close.
 * make return value consistent like
 *  - for createXXX, return XXX
 *  - otherwise, use the caller type as return type
 * remove those List<Message>.commit(), use session.close instead.
 * - remove the dependency to connectionFactory, user may supply connection and connectionFactory may be null
 */
class JMSCoreCategory {
    static Logger logger = Logger.getLogger(JMSCoreCategory.class)
    //static final ThreadLocal<Connection> connection = new ThreadLocal<Connection>();
    //static final ThreadLocal<Session> session = new ThreadLocal<Session>();
    static final clientIdPrefix;
    static {
        try {clientIdPrefix = InetAddress.getLocalHost()?.hostName} catch (e) { logger.error("fail to get local hostname on JMS static init")}
    }

    static final Map<String, Method> methodCache = [
            'JMS.getConnection': JMS.methods.find { it.name == 'getConnection'},
            'JMS.getSession': JMS.methods.find { it.name == 'getSession'},
            'Connection.start': Connection.methods.find {it.name == 'start'},
            'Session.createConsumer': Session.methods.find {it.name == 'createConsumer'}

    ];


    static Connection getConnection(subject) { return methodCache.'JMS.getConnection'.invoke(JMS.getThreadLocal()) }

    static Session getSession(subject) { return methodCache.'JMS.getSession'.invoke(JMS.getThreadLocal()) }

    //static void set(JMS jms) { JMSCoreCategory.jms.set(jms)}

    /** *****************************************************************************************************************
     * TOP LEVEL PRIVATE METHOD for establish Connection and Session
     ***************************************************************************************************************** */

    //TODO consider to set a timeout to handle uncommitted JMS thread
    //TODO consider to add a parameter to enable/disable transaction
    //Remarks: it's hardcoded to reuse session per thread
    static Connection establishConnection(ConnectionFactory factory, String clientId = null, boolean force = false) {
        if (!factory) throw new IllegalStateException("factory must not be null")
        if (!JMS.getThreadLocal()) JMS.setThreadLocal(new JMS()); //default JMS instance for JMSCategory usage
        if (force && JMS.getThreadLocal()?.connection) JMS.clearThreadLocal(true)
        if (JMS.getThreadLocal()?.connection) return JMS.getThreadLocal().connection;
        org.apache.log4j.MDC.put("tid", Thread.currentThread().getId());
        Connection conn = factory.createConnection();
        conn.setClientID(clientId ?: clientIdPrefix + ':' + System.currentTimeMillis());
        conn.setExceptionListener({JMSException e -> logger.error("JMS Exception", e)} as ExceptionListener);
        JMS.getThreadLocal()?.connection = conn;
        conn.start();
        return conn;
    }

    static Session establishSession(Connection conn) {
        if (!JMS.getThreadLocal()) JMS.setThreadLocal(new JMS()); //default JMS instance for JMSCategory usage
        if (JMS.getThreadLocal()?.session) return JMS.getThreadLocal().session;
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (JMS.getThreadLocal()) JMS.getThreadLocal().session = session;
        return session;
    }


    /** *****************************************************************************************************************
     * TOP LEVEL PUBLIC METHOD for call by users
     ***************************************************************************************************************** */
    //this method doesn't create session
    static Connection connect(ConnectionFactory factory, String clientId = null) {
        if (JMS.getThreadLocal()?.connection) return JMS.getThreadLocal().connection;
        Connection connection = establishConnection(factory, (clientId) ? clientId : '')
        if (logger.isTraceEnabled()) logger.trace("connect() - return connection: $connection (${connection.clientID}), clientId: $clientId")
        return connection;
    }

    static Session session(ConnectionFactory factory, String clientId = null) {
        if (JMS.getThreadLocal()?.session) return JMS.getThreadLocal().session;
        Connection conn = connect(factory, clientId)
        Session session = establishSession(conn);
        if (logger.isTraceEnabled()) logger.trace("session() - return session: $session")
        return session;
    }

    static Session session(Connection connection) {
        if (JMS.getThreadLocal().session) return JMS.getThreadLocal().session;
        Session session = establishSession(connection);
        if (logger.isTraceEnabled()) logger.trace("session() - return session: $session")
        return session;
    }

    static MessageConsumer createConsumer(Session session) {
        MessageConsumer consumer = methodCache.'Session.createConsumer'.invoke(session)
        JMS.getThreadLocal().consumer = consumer
        return consumer;
    }

    static start(ConnectionFactory target) {
        if (!JMS.getThreadLocal().connection) throw new IllegalStateException("not existing connection to start")
        start(JMS.getThreadLocal().connection);
    }

    static start(Connection target) {
        if (JMS.getThreadLocal()?.connection) { methodCache.'Connection.start'.invoke(JMS.getThreadLocal().connection, null)}
        else { methodCache.'Connection.start'.invoke(target, null)}

    }

    /** *****************************************************************************************************************
     * DESTINATION CREATION METHODS
     ***************************************************************************************************************** */
    // create topic
    static Topic topic(ConnectionFactory factory, String dest) {
        Connection connection = connect(factory);
        Session session = session(connection);
        Topic topic = session.createTopic(dest);
        //if (logger.isTraceEnabled()) logger.trace("topic() - return topic: $topic")
        return topic;
    }

    static Topic topic(Connection connection, String dest) {
        Session session = session(connection);
        Topic topic = session.createTopic(dest);
        //if (logger.isTraceEnabled()) logger.trace("topic() - return topic: $topic")
        return topic;
    }

    static Topic topic(Session session, String dest) {
        Topic topic = session.createTopic(dest);
        //if (logger.isTraceEnabled()) logger.trace("topic() - return topic: $topic")
        return topic;
    }

    static Queue queue(ConnectionFactory factory, String dest) {
        Connection connection = connect(factory);
        Session session = session(connection);
        Queue queue = session.createQueue(dest);
        //if (logger.isTraceEnabled()) logger.trace("queue() - return queue: $queue")
        return queue;
    }

    static Queue queue(Connection connection, String dest) {
        Session session = session(connection);
        Queue queue = session.createQueue(dest);
        //if (logger.isTraceEnabled()) logger.trace("queue() - return queue: $queue")
        return queue;
    }

    static Queue queue(Session session, String dest) {
        Queue queue = session.createQueue(dest);
        //if (logger.isTraceEnabled()) logger.trace("queue() - return queue: $queue")
        return queue;
    }

    /** ****************************************************************************************************************
     * Messenging methods
     ***************************************************************************************************************** */
    static Topic send(Topic dest, message, Map msgCfg = null) {
        return sendMessage(dest, message, msgCfg);
    }

    static Queue send(Queue dest, message, Map msgCfg = null) {
        return sendMessage(dest, message, msgCfg);
    }

    static Destination reply(Message incoming, message, Map msgCfg = null) {
        if (!incoming.JMSReplyTo) throw new RuntimeException("the incoming message does not contain a reply address")
        if (incoming.JMSCorrelationID)
            msgCfg = (msgCfg) ? msgCfg.with {if (!it.containsKey('JMSCorrelationID')) it.put('JMSCorrelationID', incoming.JMSCorrelationID); it} : ['JMSCorrelationID': incoming.JMSCorrelationID]
        return send(incoming.JMSReplyTo, message, msgCfg)
    }

    private static Object sendMessage(Destination dest, message, Map cfg = null) {
        if (!JMS.getThreadLocal()?.connection) throw new IllegalStateException("No connection. Call connect() or session() first.")
        if (!JMS.getThreadLocal()?.session) JMS.getThreadLocal().session = establishSession(JMS.getThreadLocal().connection)
        if (logger.isTraceEnabled()) logger.trace("send() - dest: $dest, message: $message, cfg: $cfg")
        try {
            MessageProducer producer = JMS.getThreadLocal().session.createProducer(dest);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            Message jmsMessage;
            if (message instanceof Message) {
                jmsMessage = message;
            } else if (message instanceof String) {
                jmsMessage = JMS.getThreadLocal().session.createTextMessage((String) message);
            } else if (message instanceof Map) {
                jmsMessage = JMS.getThreadLocal().session.createMapMessage();
                message.each {k, v ->
                    k = (k instanceof String) ? k : String.valueOf(k)
                    if (v instanceof Boolean) jmsMessage.setBoolean(k, v);
                    else if (v instanceof Byte) jmsMessage.setByte(k, v);
                    else if (v instanceof Character) jmsMessage.setChar(k, v);
                    else if (v instanceof Short) jmsMessage.setShort(k, v);
                    else if (v instanceof Integer) jmsMessage.setInt(k, v);
                    else if (v instanceof Float) jmsMessage.setFloat(k, v);
                    else if (v instanceof Long) jmsMessage.setLong(k, v);
                    else if (v instanceof Double) jmsMessage.setDouble(k, v);
                    else if (v instanceof String) jmsMessage.setString(k, v);
                    else { jmsMessage.setObject(k, (v instanceof Serializable) ? v : v.toString()); }
                }
            } else if (message instanceof InputStream) {
                throw new UnsupportedOperationException("stream message is not implemented")
            } else {
                if (!(message instanceof Serializable)) throw new IllegalArgumentException("object for object message must be serializable, object.class: ${message.getClass()}")
                jmsMessage = JMS.getThreadLocal().session.createObjectMessage()
                jmsMessage.setObject(message)
            }
            if (!JMSUtils.isEnhanced(jmsMessage.getClass())) JMSUtils.enhance(jmsMessage.getClass()) // this may need to be move up in the message creation logic

            //TODO review if there are any missing JMSproperty
            if (cfg && cfg.containsKey('replyTo') && !(cfg.'replyTo' instanceof Destination)) throw new IllegalArgumentException("replyTo must be a JMS Destination")
            cfg?.remove('replyTo')?.with {jmsMessage.setJMSReplyTo(it)}
            cfg?.remove('timestamp')?.with {jmsMessage.setJMSTimestamp(it)}
            cfg?.remove('priority')?.with {jmsMessage.setJMSPriority(it)}
            cfg?.remove('messageID')?.with {jmsMessage.setJMSMessageID(it)}
            cfg?.remove('deliveryMode')?.with {jmsMessage.setJMSDeliveryMode(it)}
            cfg?.remove('redelivered')?.with {jmsMessage.setJMSRedelivered(it)}
            cfg?.remove('correlationID')?.with {jmsMessage.setJMSCorrelationID(it)}
            cfg?.remove('type')?.with {jmsMessage.setJMSType(it)}
            cfg?.remove('expiration')?.with {jmsMessage.setJMSExpiration(it)}
            cfg?.remove('destination')?.with {jmsMessage.setJMSDestination(it)}

            if (cfg && cfg.containsKey('properties')) {
                def properties = cfg.remove('properties')
                if (!(properties instanceof Map)) throw new IllegalArgumentException("properties must be a Map, properties.class: ${properties?.getClass()}, properties: $properties")
                properties.each {k, v -> jmsMessage.setProperty(k, v) }
            }
            cfg?.each {k, v -> jmsMessage[k] = v}
            if (logger.isTraceEnabled()) logger.trace("sendMessage() - jmsMessage: $jmsMessage")
            producer.send(jmsMessage);
            JMS.getThreadLocal().connection.start()

        } catch (e) { logger.error("send() - error - dest: $dest, message: $message, cfg: $cfg", e)}
        return dest;
    }


    static QueueReceiver listen(Queue queue, MessageListener listener, String messageSelector = null) {
        if (!JMS.getThreadLocal()?.connection) throw new IllegalStateException("No connection. Call connect() or session() first.")
        if (!JMS.getThreadLocal()?.session) JMS.getThreadLocal().session = establishSession(JMS.getThreadLocal().connection)
        QueueReceiver receiver = JMS.getThreadLocal().session.createReceiver(queue)
        receiver.setMessageListener(listener)
        if (logger.isTraceEnabled()) logger.trace("listen() - queue: \"$queue\", messageSelector: \"$messageSelector\", listener: ${listener}")
        return receiver;
    }

    // subscribe to a topic
    static TopicSubscriber subscribe(Topic topic, Map cfg = null, MessageListener listener) {
        if (!JMS.getThreadLocal()?.connection) throw new IllegalStateException("No connection. Call connect() or session() first.")
        if (!JMS.getThreadLocal()?.session) JMS.getThreadLocal().session = establishSession(JMS.getThreadLocal().connection)
        def subscriptionName = cfg?.'subscriptionName' ?: topic.topicName + ':' + JMS.getThreadLocal().session.toString()
        def messageSelector = cfg?.'messageSelector', noLocal = cfg?.'noLocal' ?: false
        def durable = (cfg?.containsKey('durable') && !cfg.'durable') ? false : true, session = JMS.getThreadLocal().session
        TopicSubscriber subscriber = (durable) ? session.createDurableSubscriber(topic, subscriptionName, messageSelector, noLocal) :
            session.createSubscriber(topic, messageSelector, noLocal)
        subscriber.setMessageListener(listener);
        //TODO add a messaging wrapper that support enhancement checking
        // if (!JMSUtils.isEnhanced(message.class)) JMSUtils.enhance(message.class)
        // TODO 

        if (logger.isTraceEnabled()) logger.trace("subscribe() - topic: \"$topic\", durable: $durable, subscriptionName: \"$subscriptionName\", messageSelector: \"$messageSelector\", noLocal: $noLocal, listener: ${listener}")
        return subscriber;
        /*if (!listener.getClass().fields.find {it.name == "subscriptionName"}) {
          listener.getClass().metaClass.subscriptionName = null
      }
      listener.subscriptionName = subscriptionName*/
    }

    static Topic unsubscribe(Topic topic, String subscriptionName = null) {
        if (!JMS.getThreadLocal()?.connection) throw new IllegalStateException("No connection. Call connect() or session() first.")
        if (!JMS.getThreadLocal()?.session) JMS.getThreadLocal().session = establishSession(JMS.getThreadLocal().connection)

        subscriptionName = subscriptionName ?: topic.topicName + ':' + JMS.getThreadLocal().session.toString()
        if (logger.isTraceEnabled()) logger.trace("unsubscribe() - topic: $topic, subscriptionName: $subscriptionName")
        JMS.getThreadLocal().session.unsubscribe(subscriptionName)
    }

    static Message receive(Queue dest, Integer waitTime = null) {
        if (!JMS.getThreadLocal()?.connection) throw new IllegalStateException("No connection. Call connect() or session() first.")
        if (!JMS.getThreadLocal()?.session) JMS.getThreadLocal().session = establishSession(JMS.getThreadLocal().connection)
        MessageConsumer consumer = JMS.getThreadLocal().consumer ?: JMS.getThreadLocal().session.createConsumer(dest);
        Message message = (waitTime) ? consumer.receive(waitTime) : consumer.receiveNoWait();
        if (message && !JMSUtils.isEnhanced(message.class)) JMSUtils.enhance(message.class)
        consumer.close();
        if (logger.isTraceEnabled() && message) logger.trace("receive() - from $dest - return $message");
        return message;
    }

    static List<Message> receiveAll(Queue dest, Integer waitTime = null) {
        if (!JMS.getThreadLocal()?.connection) throw new IllegalStateException("No connection. Call JMS.connect() first.")
        if (!JMS.getThreadLocal()?.session) JMS.getThreadLocal().session = establishSession(JMS.getThreadLocal().connection)
        List<Message> messages = [];
        try {
            MessageConsumer consumer = JMS.getThreadLocal().consumer ?: JMS.getThreadLocal().session.createConsumer(dest);
            boolean first = true;
            Message message;
            while (first || message) {
                message = (waitTime) ? consumer.receive(waitTime) : consumer.receiveNoWait();
                if (message) {
                    if (!JMSUtils.isEnhanced(message.getClass())) JMSUtils.enhance(message.getClass())
                    messages << message;
                }
                first = false;
            }
            consumer.close();
            if (logger.isTraceEnabled() && messages?.size()) logger.trace("receiveAll() - from $dest - return size(): ${messages.size()}, messages: $messages");
        } catch (e) {logger.error(e.message, e)}

        return messages;
    }


}