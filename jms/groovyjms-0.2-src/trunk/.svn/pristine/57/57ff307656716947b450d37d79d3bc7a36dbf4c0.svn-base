package groovy.jms

import groovy.jms.pool.JMSThread
import java.util.concurrent.Callable
import java.util.concurrent.Future
import javax.jms.Queue
import org.apache.log4j.Logger
import javax.jms.Session
import javax.jms.Topic

/**
 * It provides features shared by JMS and JMSPool, as well as define mandatory methods
 */
abstract class AbstractJMS {
    static Logger logger = Logger.getLogger(AbstractJMS.class.name)
    static boolean enableAutoBroker = true;

    static nest = [initialValue: {return [] as LinkedList}] as ThreadLocal<List<JMS>>;
    int nestLevel = 0;

    static void setThreadLocal(JMS jms) {
        nest.get().addLast(jms)
        jms.nestLevel = nest.get().size()
        if (nest.get().size() > 1) { logger.warn("setThreadLocal() - nested jms is detected - jms: $jms, nest: ${nest.get()}")}
    }

    static JMS getThreadLocal() { return nest.get().peekLast() }

    static JMS getPreviousThreadLocal() { int i = nest.get().size() - 1; if (i >= 0) { return nest.get().get(i)}}

    static void clearThreadLocal(boolean close = true) {
        if (nest.get().size() > 1) {logger.warn("clearThreadLocal() - nested jms is detected - jms: ${getThreadLocal()}, nest: ${nest.get()}")}
        def jms = getThreadLocal()
        if (jms && !jms.closed) { jms.connection?.start(); jms.connection?.close(); jms.session = null; jms.connection = null } //flush message and close
        if (nest.get().size() > 0) nest.get().removeLast()
    }

    static void resetThreadLocal() { nest.get().clear()}

    Queue createQueue(String queueName) {
        if (this instanceof JMS) {
            return session.createQueue(queueName);
        } else {
            Future task = threadPool.submit({
                if (logger.isTraceEnabled()) logger.trace("createQueue() - executing submitted job - jms? ${JMSThread.jms.get() != null}")
                return JMSThread.jms.get().session.createQueue(queueName)
            } as Callable);
            return task.get();
        }
    }

    Topic createTopic(String destName) {
        if (this instanceof JMS) {
            return session.createTopic(destName);
        } else {
            Future task = threadPool.submit({
                if (logger.isTraceEnabled()) logger.trace("createTopic() - executing submitted job - jms? ${JMSThread.jms.get() != null}")
                return JMSThread.jms.get().session.createTopic(destName)
            } as Callable);
            return task.get();
        }
    }

    // abstract onMessage(Map cfg, Object target)
}