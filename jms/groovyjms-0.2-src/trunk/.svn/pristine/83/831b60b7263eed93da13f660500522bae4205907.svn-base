package groovy.jms

import groovy.jms.provider.ActiveMQJMSProvider
import javax.jms.Connection
import javax.jms.ConnectionFactory
import javax.jms.MessageListener
import javax.jms.Session

/**
 * This is for testing basic JMS operations to ensure the provided JMS Implementation is functional in unit testing.
 * Test cases should not use GroovyJMS
 */
public class JMSImplTest extends GroovyTestCase {
    def provider = new ActiveMQJMSProvider();
    ConnectionFactory factory;

    void setUp() { factory = provider.getConnectionFactory() }

    void tearDown() { }

    void testQueueListenSendWithTwoThreads() {
        final results = [];
        Connection connection0, connection1;

        new Thread() {
            connection0 = factory.createConnection().with {it.clientID = "testclient0"; it};
            Session session = connection0.createSession(false, Session.AUTO_ACKNOWLEDGE)
            def queue = session.createQueue("testQueueListenSend")
            assertNotNull queue
            def consumer = session.createConsumer(queue)
            consumer.setMessageListener({ results << it} as MessageListener)
            assertNotNull(consumer)
            connection0.start();
        }.start()

        new Thread() {
            connection1 = factory.createConnection().with {it.clientID = "testclient1"; it};
            Session session = connection1.createSession(false, Session.AUTO_ACKNOWLEDGE)
            def queue = session.createQueue("testQueueListenSend")
            def producer = session.createProducer(queue)
            producer.send(session.createTextMessage("this is a test"))
            connection1.start();
        }.start()

        sleep(500)
        assertEquals 1, results.size()
        connection0.close(); connection1.close()
    }

    void testQueueSendReceiveWithTwoThreads() {
        final results = [];
        Connection connection1, connection2;

        new Thread() {
            connection1 = factory.createConnection().with {it.clientID = "testclient1"; it};
            Session session = connection1.createSession(false, Session.AUTO_ACKNOWLEDGE)
            def queue = session.createQueue("testQueueListenSend")
            def producer = session.createProducer(queue)
            producer.send(session.createTextMessage("this is a test"))
            connection1.start();
        }.start()

        new Thread() {
            connection2 = factory.createConnection().with {it.clientID = "testclient0"; it};
            Session session = connection2.createSession(false, Session.AUTO_ACKNOWLEDGE)
            def queue = session.createQueue("testQueueListenSend")
            assertNotNull queue
            def consumer = session.createConsumer(queue)
            connection2.start();
            results << consumer.receive(1000)
        }.start()

        sleep(500)
        assertEquals 1, results.size()
        connection2.close(); connection1.close()
    }


}
