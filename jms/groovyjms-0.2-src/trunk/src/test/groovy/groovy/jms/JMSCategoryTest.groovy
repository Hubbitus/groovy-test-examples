package groovy.jms

import groovy.jms.provider.ActiveMQJMSProvider
import javax.jms.*

class JMSCategoryTest extends GroovyTestCase {
    def provider = new ActiveMQJMSProvider(); // all test shares the same Broker for performance; shutdown hook is enabled by default
    ConnectionFactory jms; //simulate injection

    void setUp() { jms = provider.getConnectionFactory() }

    void tearDown() {JMS.close();}

    void testDefaultConnFactory() {
        assertNotNull("default conn factory is not available", provider.getConnectionFactory())
    }

    void testTopic() {
        String messageToSend = "this is the message to send"
        String messageToCheck;
        use(JMSCategory) {
            jms.session().topic("testTopic0").subscribe({Message m -> messageToCheck = m.text} as MessageListener)
            sleep(500)
            jms.topic("testTopic0").send(messageToSend);
        }
        sleep(500)
        assertEquals("callback message doesn't match", messageToSend, messageToCheck)
    }

    void testTopicSubscriber() {
        use(JMSCategory) {
            MessageListener listener = {m -> println m} as MessageListener
            TopicSubscriber subscriber = jms.session().topic("testTopic0").subscribe(listener)
            assertNotNull subscriber
            assertNotNull subscriber.messageListener
            JMS.close();
        }
    }

    void testCoreAPIListenToQueue() { //test core api
        use(JMSCategory) {
            MessageListener listener = {m -> println m} as MessageListener
            QueueReceiver receiver = jms.session().queue("testCoreAPIListenToQueue").listen(listener)
            assertNotNull receiver
            assertNotNull receiver.messageListener
            JMS.close();
        }
    }

    void testStringQueueListen() {
        use(JMSCategory) {
            def result = []
            jms.session()
            "testStringQueueListen".listen {m -> result << m}
            "testStringQueueListen".send("message0")
            "testStringQueueListen".send("message1")
            sleep(500)
            assertEquals 2, result.size()
            JMS.close();
        }
    }

    void testQueueInTheSameSession() {
        String messageToSend = "this is the message to send", messageToCheck;
        List<Message> messages
        use(JMSCategory) {
            Session session = jms.session(); session.queue("testQueueInTheSameSession").send(messageToSend);
            sleep(100); messages = session.queue("testQueueInTheSameSession").receiveAll(1000); session.close();
        }
        assertEquals("message size incorrect", 1, messages?.size())
        assertEquals("callback message doesn't match", messageToSend, messages[0].text)
        JMS.close()
    }

    void testQueueInTheDiffConn() {
        String messageToSend = "this is the message to send", messageToCheck;
        List<Message> messages = [];
        new Thread() {
            use(JMSCategory) {Session session = jms.session(); session.queue("testQueueInTheDiffConn").send(messageToSend); JMS.close()}
        }.start()
        sleep(1000)
        new Thread() {
            use(JMSCategory) {messages = jms.queue("testQueueInTheDiffConn").receiveAll(1000); JMS.close()}
        }.start()

        assertEquals("message size incorrect", 1, messages.size())
        assertEquals("callback message doesn't match", messageToSend, messages[0].text)
    }

    void testTempQueuSyncReply() {
        String messageToSend = "passphase", messageToCheck;
        Queue replyQueue;
        use(JMSCategory) {
            replyQueue = jms.session().createQueue("replyQueue")
            jms.session().queue("testQueue0").send(messageToSend, [JMSCorrelationID: 'unittest', JMSReplyTo: replyQueue])
            JMS.close();
        }
        use(JMSCategory) {
            Message message = jms.connect().queue("testQueue0").receive(1000); JMS.close();
            assertNotNull("fail to retrieve message", message)
            String replyText = message.text + "ABC";
            assertNotNull(message.JMSReplyTo)
            jms.connect(); message.JMSReplyTo.send(replyText, [JMSCorrelationID: 'unittest']); JMS.close();
        }
        use(JMSCategory) {
            jms.connect(); messageToCheck = replyQueue.receive(1000).text; JMS.close();
        }
        assertEquals("passphase hash doesn't match", messageToSend + "ABC", messageToCheck)
    }

    void testReplyWithReplyMethod() {
        String messageToSend = "passphase";
        Queue replyQueue;
        use(JMSCategory) {
            replyQueue = jms.session().createQueue("replyQueue")
            jms.session().queue("testQueue0").send(messageToSend, [JMSCorrelationID: 'unittest', JMSReplyTo: replyQueue])
            JMS.close();
        }
        use(JMSCategory) {
            def message = jms.queue("testQueue0").receive(1000)
            assertNotNull("fail to retrieve message", message)
            message.reply(message.text + "ABC")
            JMS.close();
        }
        Message messageToCheck;
        use(JMSCategory) {
            jms.connect(); messageToCheck = replyQueue.receive(1000); JMS.close();
        }
        assertEquals("passphase hash doesn't match", messageToSend + "ABC", messageToCheck.text)
        assertEquals("JMSCorrelationID doesn't match", 'unittest', messageToCheck.JMSCorrelationID)
    }

    void testJMS() {
        use(JMSCategory) {
            jms.session()
            "queue".send("message")
            assertNotNull("queue".receive(within: 1000))
        }
    }

}