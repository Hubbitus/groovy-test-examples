package groovy.jms.provider

import javax.jms.ConnectionFactory
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.broker.BrokerRegistry
import org.apache.activemq.broker.BrokerService
import org.apache.log4j.Logger
import groovy.jms.JMSPool
import groovy.jms.JMS
import groovy.jms.JMSCoreCategory
import org.apache.activemq.command.ActiveMQMapMessage
import groovy.jms.JMSUtils
import org.apache.activemq.command.ActiveMQTextMessage

class ActiveMQJMSProvider implements JMSProvider {
    static Logger logger = Logger.getLogger(ActiveMQJMSProvider.class.name)
    public static final String BROKER_NAME = "groovy.jms.provider.ActiveMQJMSProvider.broker"
    public static final String CONNECTOR_URL = "vm://localhost?broker.persistent=false&jms.useAsyncSend=true"//
    static BrokerService broker;
    ConnectionFactory factory;

    public boolean isAvailable() {
        try {
            return Class.forName("org.apache.activemq.ActiveMQConnectionFactory") != null
        } catch (e) { return false; }
    }

    synchronized static protected startBroker() {
        if (!broker) {
            BrokerRegistry registry = BrokerRegistry.getInstance();
            broker = registry.findFirst() ?: new BrokerService(brokerName: BROKER_NAME, useJmx: false,
                    persistent: false, useShutdownHook: true, transportConnectorURIs: [CONNECTOR_URL])
            if (!broker.transportConnectorURIs.find {it == CONNECTOR_URL}) broker.addConnector(CONNECTOR_URL)
            if (logger.isInfoEnabled()) logger.info("startBroker() - create broker - broker: $broker")
        }
        if (!broker.isStarted()) broker.start()
    }

    public ConnectionFactory getConnectionFactory() {
        try {
            if (logger.isInfoEnabled()) logger.info("getConnectionFactory() - broker: $broker, broker.isStarted? ${broker?.isStarted()}")
            if (JMS.enableAutoBroker && !broker?.isStarted()) startBroker();
            if (logger.isInfoEnabled()) logger.info("getConnectionFactory() - broker: $broker, broker.isStarted? ${broker?.isStarted()}")
            factory = factory ?: new ActiveMQConnectionFactory(CONNECTOR_URL);
            enhanceActiveMQClasses()
            return factory;
        } catch (NoClassDefFoundError e) {
            throw new ClassNotFoundException("Cannot find ActiveMQ library, please put ActiveMQ jar in the classpath. e: ${e.message}");
        }
    }

    /**
     * Perform enhancement in initialization time so it won't slow down the first message
     */
    private static enhanceActiveMQClasses() {
        if (!JMSUtils.isEnhanced(ActiveMQMapMessage.class)) JMSUtils.enhance(ActiveMQMapMessage.class)
        if (!JMSUtils.isEnhanced(ActiveMQTextMessage.class)) JMSUtils.enhance(ActiveMQTextMessage.class)
    }
}