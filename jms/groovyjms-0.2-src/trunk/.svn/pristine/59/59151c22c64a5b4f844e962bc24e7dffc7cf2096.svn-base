package groovy.jms.provider

import org.apache.activemq.broker.BrokerRegistry

class ActiveMQJMSProviderTest extends GroovyTestCase {
    void testCreateDefaultBroker() {
        def provider = new ActiveMQJMSProvider();
        provider.getConnectionFactory();
        
        assertNotNull provider.broker
        assertTrue provider.broker.started
        assertNotNull BrokerRegistry.getInstance().findFirst();
        assertEquals provider.broker, BrokerRegistry.getInstance().findFirst()
    }

}