package test

import javax.jms.MessageListener
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.Session

class EMCTests extends GroovyTestCase {

    void testEMC() {
        def factory = new ActiveMQConnectionFactory("vm://localhost")
        def connection = factory.createConnection().with{it.clientID = "testclient";it};
        def session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
       def topic = session.createTopic("testTopic")
        assertNotNull session

        def listener = {} as MessageListener
        listener.getClass().metaClass.time = null
        listener.time = System.currentTimeMillis()

        println listener.time
        def subscriber = session.createDurableSubscriber(topic, "subscription0", null, false)
        println listener.time  //sometimes println the time, sometimes print the default false
        assertNotNull listener.time
    }

}