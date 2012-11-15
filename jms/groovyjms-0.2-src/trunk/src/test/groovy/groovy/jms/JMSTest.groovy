package groovy.jms

import static groovy.jms.JMS.jms
import groovy.jms.provider.ActiveMQJMSProvider
import java.util.concurrent.CopyOnWriteArrayList
import javax.jms.*

public class JMSTest extends GroovyTestCase {
    def provider = new ActiveMQJMSProvider(); // all test shares the same Broker for performance; shutdown hook is enabled by default
    ConnectionFactory factory; //simulate injection

    void setUp() { factory = provider.getConnectionFactory(); }

    void tearDown() {JMS.clearThreadLocal(true) }

    void testJMSProviderSystemProperty() {
        System.setProperty(JMS.SYSTEM_PROP_JMSPROVIDER, "groovy.jms.provider.ActiveMQJMSProviderNOTEXISTED")
        try { def jms = new JMS(); fail() } catch (e) { }
        assertTrue(true)
        System.properties.remove(JMS.SYSTEM_PROP_JMSPROVIDER)
    }

    void testConstructors() {
        def c = factory.createConnection(), s = c.createSession(false, JMS.DEFAULT_SESSION_ACK)
        def jms = new JMS([factory, c, s], [autoClose: false])
        assertNotNull(jms); assertSame(c, jms.connection); assertSame(s, jms.session); assertFalse(jms.autoClose)
        jms = new JMS([c, null, s], [autoClose: false])  // test null in the arraylist
        assertNotNull(jms); assertSame(c, jms.connection); assertSame(s, jms.session); assertFalse(jms.autoClose)
        try { jms = new JMS(s); fail("session w/o connection shall throw exception")} catch (e) {}
    }

    void testConstructorClosureParameters() {
        def c0 = factory.createConnection(), s0 = c0.createSession(false, JMS.DEFAULT_SESSION_ACK)
        def jms0, jms1 = new JMS([factory, c0, s0]) {jms, c, s -> jms0 = jms; assertSame(c0, c); assertSame(s0, s)}
        assertSame(jms1, jms0)
        jms1 = new JMS([c0, s0]) {jms, c -> jms0 = jms; assertSame(c0, c);}; assertSame(jms1, jms0)
        jms1 = new JMS([c0, s0]) {jms -> jms0 = jms; }; assertSame(jms1, jms0)
    }

    void testGetConnectionSession() {
        Connection c = factory.createConnection()
        new JMS(c) {  //read-only connection and session are automatically available in the JMS closure scope
            assertEquals c, connection
            assertNotNull session
            println connection
            println session
        }
    }

    void testSimple() {
        String result, result2;
        new JMS() {
            "greetingroom".subscribe { result = it.text}
            sleep(1000)
            "how are you?".publishTo "greetingroom"
            sleep(1000)
        }
        assertEquals("how are you?", result)
    }


    void testMapMessage() {
        Map data = ['int': 100, 'double': 100000000d, 'string': 'string', 'character': 'd' as char];
        def results = []
        new JMS() {
            "testqueue".send(data)
            data.sendTo("testqueue")
            sleep(1000);
            results = "testqueue".receiveAll().collect {it.toMap()}
        }

        results.each {Map result ->
            assertEquals data.'int', result.'int'
            assertEquals data.'double', result.'double'
            assertEquals data.'string', result.'string'
            assertEquals data.'character', result.'character'
        }
    }

    void testStaticClosure() {
        jms {
            "testqueue".send("testdata")
            sleep(500)
            assertEquals("testdata", "testqueue".receive(within: 200).text)
        }

        jms(factory) {
            "testqueue".send("testdata1")
            assertEquals("testdata1", "testqueue".receive(within: 200).text)
        }
    }

    void testEachMessage() {
        def jms = new JMS()
        jms.setAutoClose(false)
        jms.send(toQueue: 'testQueue', 'message': 'hello')
        jms.send(toQueue: 'testQueue', 'message': 'hello2')
        sleep(1000)
        int count = 0
        jms.eachMessage("testQueue", [within: 1000]) {m ->
            count++
            assertNotNull(m?.text)
            assertTrue(m.text?.startsWith("hello"))
        }
        assertEquals 2, count
    }

    /* void testOnMessageWithCategoryMethod() {
           def jms = new JMS().with {it.setAutoClose(false); it}, queue = "testOnMessageWithCategoryMethod"
        jms.onMessage(queue: queue) {map, msg ->           // for map message
            assertNotNull(msg)
            assertTrue(msg instanceof Message)
        }
        jms.send(toQueue: queue, message: queue)
       }

    void testOnMapMessageWithOneClosureParameters() {
        def jms = new JMS().with {it.setAutoClose(false); it}, queue = "testOnMapMessageWithOneClosureParameters"
        jms.onMessage(queue: queue) {map ->           // for map message
            assertNotNull(map)
            assertTrue(map instanceof Map)
            assertEquals(queue, map.'name')
        }
        jms.send(toQueue: queue, message: [name: queue])
    }


    void testOnMapMessageWithTwoClosureParameters() {
        def jms = new JMS().with {it.setAutoClose(false); it}, queue = "testOnMapMessageWithTwoClosureParameters"
        jms.onMessage(queue: queue) {map, msg ->           // for map message
            assertNotNull(map)
            assertTrue(map instanceof Map)
            assertNotNull(msg)
            assertTrue(msg instanceof Message)
            assertEquals(queue, map.'name')
        }
        jms.send(toQueue: queue, message: [name: queue])
    }*/

    void testOnMessageNonDurableTopic() {
        def jms = new JMS().with {it.setAutoClose(false); it}, topic = "testOnMessageNonDurableTopic", result = [] as CopyOnWriteArrayList
        // 1 - listen with Closure for a single topic, with 'topic'
        def listener = {MapMessage m -> result << m.getString('key0')} as MessageListener
        jms.onMessage([topic: topic, durable: false], listener)
        jms.send toTopic: topic, message: [key0: 'value0']
        sleep(500)
        assertEquals 1, result.size()
        assertEquals 'value0', result[0]
        result.clear()
        //TODO THIS TEST CASE DOESN'T TEST DURABLE!!!
    }

    void testOnMessageForTopic() {
        def jms = new JMS().with {it.setAutoClose(false); it}, topic = "testOnMessageForTopic", result = [] as CopyOnWriteArrayList

        // 1 - listen with Closure for a single topic, with 'topic'
        def listener = {MapMessage m -> result << m.getString('key0')} as MessageListener
        jms.onMessage(topic: topic, listener)
        jms.send toTopic: topic, message: [key0: 'value0']
        sleep(500)
        assertEquals 1, result.size()
        assertEquals 'value0', result[0]
        result.clear()

        jms.stopMessage(topic: topic)

        // 2 -listen with anonymous closure for two topics, with 'fromTopic'
        try {
            jms.onMessage(fromTopic: [topic, 'anotherTopic']) {MapMessage m -> result << m.getString('key0')}
            jms.send toTopic: topic, message: [key0: 'value1']
            sleep(500)
            assertEquals("fail to unsubscribe", 1, result.size())
            assertEquals 'value1', result[0]
            result.clear()
        } catch (JMSException e) {
            fail("possibly fail to unsubscribe")
        }
    }

    void testOnMessageForQueue() {
        def jms = new JMS().with {it.setAutoClose(false); it}, dest = "testOnMessageForQueue", result = [] as CopyOnWriteArrayList

        // 1 - listen with Closure for a single queue with 'queue'
        def listener = {MapMessage m -> result << m.getString('key0')} as MessageListener
        jms.onMessage(queue: dest, listener)
        jms.send toQueue: dest, data: [key0: 'value0'] //Test using 'data' synonym
        sleep(500)
        assertEquals 1, result.size()
        assertEquals 'value0', result[0]
        result.clear()

        jms.stopMessage(queue: dest)

        // 2 -listen with anonymous closure, for two queues, with 'fromQueue'
        try {
            jms.onMessage(fromQueue: [dest, 'anyQueue']) {MapMessage m -> result << m.getString('key0')}
            jms.send toQueue: dest, message: [key0: 'value1']
            sleep(500)
            assertEquals("fail to unsubscribe", 1, result.size())
            assertEquals 'value1', result[0]
            result.clear()
        } catch (JMSException e) {
            fail("possibly fail to unsubscribe")
        }
    }

    void testOnMessageForQueueAndTopic() {
        def jms = new JMS().with {it.setAutoClose(false); it}, queue = "testQueue", topic = "testTopic", result = [] as CopyOnWriteArrayList

        // 1 - listen with Closure
        def listener = {MapMessage m -> result << m} as MessageListener
        jms.onMessage(queue: queue, topic: topic, listener)
        jms.send toQueue: queue, message: [key0: 'queue message']
        jms.send toTopic: topic, message: [key0: 'topic message']
        sleep(500)
        assertEquals 2, result.size()
        result.clear()

        jms.stopMessage(queue: queue, topic: topic)

        // 2 -listen with anonymous closure
        try {
            jms.onMessage(queue: queue, topic: topic) {MapMessage m -> result << m.getString('key0')}
            jms.send toQueue: queue, message: [key0: 'queue message1']
            jms.send toTopic: topic, message: [key0: 'topic message1']
            sleep(500)
            assertEquals("fail to unsubscribe", 2, result.size())
            result.clear()
        } catch (JMSException e) {
            fail("possibly fail to unsubscribe")
        }
    }

    void testReceiveWithoutWith() {
        def jms = new JMS()
        jms.setAutoClose(false)
        jms.send(toQueue: 'testReceiveQueue', 'message': 'hello')
        jms.send(toQueue: 'testReceiveQueue', 'message': [key: 'value'])
        def result = jms.receive(fromQueue: 'testReceiveQueue', within: 500) // only support receiveAll
        assertEquals 2, result.size()
        result = jms.receive(fromQueue: 'testReceiveQueue', within: 500) // no more message
        assertEquals 0, result.size()

        jms.send(toQueue: 'testReceiveQueue', 'message': 'hello2')
        result = []
        result = jms.receive(fromQueue: 'testReceiveQueue', within: 500) // put closure at the end
        assertEquals 1, result.size()
    }


    void testReceiveWith() {
        def jms = new JMS()
        jms.setAutoClose(false)
        jms.send(toQueue: 'testReceiveQueue', 'message': 'hello')
        jms.send(toQueue: 'testReceiveQueue', 'message': [key: 'value'])
        def result = []
        jms.receive(fromQueue: 'testReceiveQueue', within: 500, with: {result = it}) // only support receiveAll
        assertEquals 2, result.size()
        jms.receive(fromQueue: 'testReceiveQueue', within: 500, with: {result = it}) // no more message
        assertEquals 0, result.size()

        jms.send(toQueue: 'testReceiveQueue', 'message': 'hello2')
        result = []
        jms.receive(fromQueue: 'testReceiveQueue', within: 500) {result = it.text} // put closure at the end
        assertEquals 1, result.size()
    }

    void testSendReceiveMapMessageProperties() {
        def jms = new JMS(), queue = "testSendReceiveMapMessageProperties"
        jms.send(toQueue: queue, message: [queue: queue], properties: [hello: 'world'])
        def result = jms.receive(fromQueue: queue, within: 500)
        assertEquals 1, result.size()
        use(JMSCategory) {
            //assertEquals queue, result[0].get('queue') //TODO check this
            assertEquals 'world', result[0].getStringProperty('hello')
            assertEquals 'world', result[0].getProperty('hello')
        }
    }

    void testNewInstance() {
        def jms = JMS.newInstance()
        jms.setAutoClose(false)
        assertNotNull jms
        def result = []
        jms.send toQueue: 'queue0', message: 'newinstance'
        jms.receive fromQueue: 'queue0', within: 1000, with: {result = it}
        assertNotNull result
        assertEquals 1, result.size()
    }

    void testDefaultConfig() {
        def jms = new JMS()
        assertTrue jms.autoClose
    }

}
