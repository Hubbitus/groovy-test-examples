package test;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class JMSSimulator {
    private static JMSSimulator _instance;
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private JMSSimulator() {
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                for (Map.Entry<String,Queue<Message>> entry : topicMessages.entrySet()) {
                    String topic = entry.getKey();
                    if (subscribers.containsKey(topic)) {
                        Queue<Message> q = entry.getValue();
                        while (!q.isEmpty()) {
                            subscribers.get(topic).onMessage(q.poll());
                        }
                    }

                }
            }
        }, 10, 1, TimeUnit.SECONDS);
    }

    public static JMSSimulator getInstance() {
        if (_instance == null) _instance = new JMSSimulator();
        return _instance;
    }

    Map<String, MessageListener> subscribers = new ConcurrentHashMap<String, MessageListener>(); //one listener per topic

    public void subscribe(String topic, MessageListener listener) {
        subscribers.put(topic, listener);
    }


    Map<String, Queue<Message>> topicMessages = new ConcurrentHashMap<String, Queue<Message>>();

    public void send(String topic, Message message) {
        if (!topicMessages.containsKey(topic)) topicMessages.put(topic, new LinkedBlockingQueue());
        topicMessages.get(topic).offer(message);
    }

}
