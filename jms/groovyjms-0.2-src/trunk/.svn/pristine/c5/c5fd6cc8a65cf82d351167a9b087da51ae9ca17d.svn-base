package groovy.jms

import javax.jms.*

class JMSCategory extends JMSCoreCategory {

    static Map toMap(MapMessage mapMessage) {
        def result = [:]
        mapMessage.mapNames.each {result.put(it, mapMessage.getObject(it))}
        return result;
    }

    static receive(String dest, Map cfg = null) {
        int timeout = (cfg?.within) ? Integer.valueOf(cfg.within) : 0
        return JMS.getThreadLocal().session.queue(dest).receive(timeout);
    }

    static receiveAll(String dest, Map cfg = null) {
        int timeout = (cfg?.'within') ? Integer.valueOf(cfg.'within') : 0
        return JMS.getThreadLocal().session.queue(dest).receiveAll(timeout);
    }

    static Queue send(String dest, String message) {
        return JMS.getThreadLocal().session.queue(dest).send(message);
    }

    static Queue sendTo(Map message, String dest) {
        return JMS.getThreadLocal().session.queue(dest).send(message);
    }

    static Queue sendTo(String message, String dest) {
        return JMS.getThreadLocal().session.queue(dest).send(message);
    }

    static Queue send(String dest, Map message) {
        return JMS.getThreadLocal().session.queue(dest).send(message);
    }

    static Topic subscribe(String dest) { return JMS.getThreadLocal().session.topic(dest); } //TODO consider to remove this

    static TopicSubscriber with(Topic topic, Map cfg = null, Closure l) { with(topic, cfg, l as MessageListener); } //TODO consider to remove this

    static TopicSubscriber with(Topic topic, Map cfg = null, MessageListener l) { topic.subscribe(cfg, l) } //TODO consider to remove this

    static TopicSubscriber subscribe(String dest, Map cfg = null, Closure listener) {
        return JMS.getThreadLocal().session.topic(dest).with(cfg, listener);
    }

    static TopicSubscriber subscribe(String dest, Map cfg = null, MessageListener listener) {
        return JMS.getThreadLocal().session.topic(dest).with(cfg, listener);
    }

    static void publishTo(String textMessage, String dest, Map cfg = null) {
        if (!JMS.getThreadLocal().session) throw new IllegalStateException("ThreadLocal session is not available")
        Topic topic = JMS.getThreadLocal().session.topic(dest);
        sendMessage(topic, textMessage, cfg);
    }


    static Queue listen(String dest) {
        return JMS.getThreadLocal().session.queue(dest);
    }


    static QueueReceiver with(Queue dest, Closure listener) {
        with(dest, listener as MessageListener);
    }

    static QueueReceiver with(Queue dest, MessageListener listener) {
        dest.listen(listener)
    }

    static QueueReceiver listen(String dest, Closure listener) {
        return JMS.getThreadLocal().session.queue(dest).with(listener);
    }

    static QueueReceiver listen(String dest, MessageListener listener) {
        return JMS.getThreadLocal().session.queue(dest).with(listener);
    }
}