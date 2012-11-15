package groovy.jms.provider

import javax.jms.ConnectionFactory
import org.apache.activemq.pool.PooledConnectionFactory
import groovy.jms.JMSPool

class ActiveMQPooledJMSProvider extends ActiveMQJMSProvider {
    /**
     * ActiveMQ Default
     * maxConnections = 1
     * maximumActive = 500    //  maximum number of active sessions per connection
     * idleTimeout = 30*1000
     */
    Map connectionFactoryConfig = [maxConnections: 500, maximumActive: 1, idleTimeout: 30 * 1000]

    ActiveMQPooledJMSProvider(Map cfg = null) {
        if (cfg) cfg.each {k, v -> connectionFactoryConfig.put(k, v)}
    }

    public ConnectionFactory getConnectionFactory() {
        try {
            if (JMSPool.enableAutoBroker && this.broker?.isStarted()) startBroker();
            factory = factory ?: new PooledConnectionFactory(CONNECTOR_URL);
            connectionFactoryConfig.each {k, v -> factory[k] = v}
            return factory;
        } catch (NoClassDefFoundError e) {
            throw new ClassNotFoundException("Cannot find ActiveMQ library, please put ActiveMQ jar in the classpath. e: ${e.message}");
        }
    }
}