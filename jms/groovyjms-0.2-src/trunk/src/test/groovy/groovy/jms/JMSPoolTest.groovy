package groovy.jms

import groovy.jms.JMS
import groovy.jms.JMSPool
import groovy.jms.provider.ActiveMQPooledJMSProvider
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.jms.Message
import javax.jms.Queue
import org.apache.log4j.Logger
import org.apache.log4j.MDC
import static groovy.jms.JMS.jms

class JMSPoolTest extends GroovyTestCase {
    static Logger logger = Logger.getLogger(JMSPoolTest.class.name)
    ActiveMQPooledJMSProvider provider;

    void setUp() {
        org.apache.log4j.MDC.put("tid", Thread.currentThread().getId());
        //provider = new ActiveMQPooledJMSProvider()
        //provider.getConnectionFactory(); //trigger init
    }

   /* void testConstructor() {
        def pool = new JMSPool()
        assertNotNull pool?.connectionFactory
        assertTrue(pool?.connectionFactory instanceof PooledConnectionFactory)
        pool.shutdown()
    }*/

    void testOnMessageThread() {
        def pool = new JMSPool()
        pool.onMessage(topic: 'testTopic', threads: 1) {m -> println m}
        pool.shutdown()
    }

    void testAbstractGetQueue() {
        def pool = new JMSPool(corePoolSize: 1, maximumPoolSize: 1, keepAliveTime: 1, unit: TimeUnit.SECONDS)
        Queue q = pool.createQueue("testAbstractGetQueue")
        assertNotNull(q)
        assertEquals("testAbstractGetQueue", q.getQueueName())
    }

    void testMultipleSenderSingleReceiverOnQueue() { // just to prove message could be sent
        def pool = new JMSPool(maximumPoolSize: 10), result = [], counter = 0, count = 20, queueName = "testMultipleSenderSingleReceiverOnQueue"
        sleep(100)
        count.times { pool.send(toQueue: queueName, message: 'message #' + it) }
        sleep(500)

        jms(pool.connectionFactory) {
            result += queueName.receiveAll(within: 2000)
        }
        result?.eachWithIndex {it, i -> println "$i\t$it"}
        assertEquals(count, result.size())

        sleep(5000)
        pool.shutdown()
        sleep(10000)
        //use stable non-Pool JMS to retreive and verify results
    }

    void testTopicOnMessage() {
        def pool = new JMSPool()
        def result = []
        pool.onMessage([topic: 'testTopic', threads: 1]) {m -> logger.debug("testTopicOnMessage() - received message m: ${m}"); result << m}
        sleep(1000)
        pool.send(toTopic: 'testTopic', message: 'this is a test')
        sleep(1000)
        result.eachWithIndex {it, i -> println "$i\t$it"}
        assertEquals(1, result.size())
    }

    void testQueueOnMessageWithTwoPools() {
        def results = [], dest = "testQueueOnMessageWithTwoPools"
        new Thread() {
            org.apache.log4j.MDC.put("tid", Thread.currentThread().getId());
            def incomingPool = new JMSPool()
            def message;
            incomingPool.onMessage(fromQueue: dest, threads: 1) {Message m ->
                logger.debug("${dest}() - received message m: ${m}");
                results << m
            }
        }.start()
        sleep(500)
        new Thread() {
            org.apache.log4j.MDC.put("tid", Thread.currentThread().getId());
            def outgoingPool = new JMSPool(), reply = outgoingPool.createQueue("/reply")
            outgoingPool.send(toQueue: dest, message: [1: 'one'], replyTo: reply, properties: ['hello': 'world'])
        }.start()
        sleep(1500)
        assertEquals "fail to receive message", 1, results.size()
    }

    void testQueueSendReceiveReplyWithTwoPools() {
        def dest = "testQueueSendReceiveWithTwoPools", replyDest = dest + "-reply"
        logger.info("testQueueSendReceiveWithTwoPools() - begin")

        def sendJob;
        new Thread() {
            def outgoingPool = new JMSPool()
            sendJob = outgoingPool.send(toQueue: dest, message: 'this is a test', replyTo: outgoingPool.createQueue(replyDest), properties:[note:'note'])
        }.start()
        sleep(2000)
        assertNotNull(sendJob); assertTrue(sendJob.get()) //wait for the job to complete
        logger.info("testQueueSendReceiveWithTwoPools() - sent msg")
        sleep(500)

        org.apache.log4j.MDC.put("tid", Thread.currentThread().getId());
        def incomingPool = new JMSPool()
        def msgs = incomingPool.receive([fromQueue: dest, within: 5000, threads: 1])
        assertEquals "fail to receive message", 1, msgs.size()
        def msg = msgs?.get(0); assertNotNull msg
        Future sendReply
        new Thread() {
            try {
                def outgoingPool = new JMSPool()
                sendReply = outgoingPool.send(reply: msg, message: "received")
                assertNotNull(sendReply)
            } catch (e) {
                logger.error(e)
            }
        }.start()
        sleep(2000)
        assertNotNull(sendReply)
        def jobResult = sendReply.get(); assertNotNull(jobResult); assertTrue(jobResult)
        sleep(1000)
        def replyMsgs = incomingPool.receive(fromQueue: replyDest, within: 1000, threads: 1)
        assertEquals("fail to receive reply msg", 1, replyMsgs.size())

        def replyMsg = replyMsgs.get(0)
        assertNotNull(replyMsg); assertEquals("received", replyMsg.'text') ;
        assertEquals("fail to copy properties", 'note', replyMsg.'note')
        logger.info("testQueueSendReceiveWithTwoPools() - end")
    }


    void testMultipleConcurrentOutgoingThreads() {
        def outgoingPool = new JMSPool(maximumPoolSize: 10)
        outgoingPool.send(queue: ['q1', 'q2', 'q3', 'q4', 'q5'], message: 'hi', threads: 5)


    }

}