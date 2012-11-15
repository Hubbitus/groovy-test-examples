package groovy.jms.pool

import groovy.jms.JMS
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask
import javax.jms.ConnectionFactory
import javax.jms.MessageListener
import org.apache.log4j.Logger
import java.util.concurrent.Future
import groovy.jms.provider.ActiveMQJMSProvider
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.jms.Connection
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.jms.Session

/**
 * One connector per thread
 */
class JMSConnector extends FutureTask {
    static Logger logger = Logger.getLogger(JMSConnector.class.name)
    def cancelled = false, interval = 200, throughput = 0;
    private ThreadLocal<JMS> jms = new ThreadLocal<JMS>()
    Map cfg;
    def target
    ConnectionFactory connectionFactory;

    JMSConnector(ConnectionFactory f, Map cfg, target, Callable c) {
        super(c)
        if (!f) throw new IllegalArgumentException("A JMS ConnectionFactory must be provided as the first argument in the constructor")
        if (!cfg.containsKey('topic') && !cfg.containsKey('queue')) throw new IllegalArgumentException("at least one topic or queue must be specified")
        this.connectionFactory = f; this.cfg = cfg; this.target = target;
        println ("constructed ${this}")

    }

    String toString(){
        return "${super.toString()}, cfg: $cfg"
    }

    void run() {
        try {
            if (!jms.get()) {
                Connection connection = connectionFactory.createConnection().with {it.clientID = JMS.getDefaultClientID() + ":" + Thread.currentThread().id; it};
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
                jms.set(new JMS(connection, session));
            }
            jms.get().onMessage(cfg, target)
        } catch (e) {
            e.printStackTrace()
            logger.error("error in running", e)
        }
    }

    /*public boolean cancel(boolean mayInterruptIfRunning) {
        println("JMSConnector.cancel()")
        super(cancel(mayInterruptIfRunning))
        //cannceled = true; return true;
    }*/

    public static void main(args) {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(10)
        ConnectionFactory factory = new ActiveMQJMSProvider().getConnectionFactory()
        JMSConnector connector = new JMSConnector(factory, [queue: 'myQueue'], {m -> println m} as MessageListener, {return connector.throughput} as Callable)
        Future f0 = pool.scheduleAtFixedRate(connector, 0, 100, TimeUnit.MILLISECONDS)
        sleep(1000)
        println "future.isDone()? " + f0.isDone()
        f0.cancel(true)
        sleep(5000)
        println "future.isDone()? " + f0.isDone()
        sleep(5000)

    }

}