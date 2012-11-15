// http://www.andrejkoelewijn.com/wp/2011/03/18/listing-activemq-queues-with-jmx-and-groovy/

import javax.management.*
import javax.management.remote.*
import javax.lang.management.*

def serverUrl = 'service:jmx:rmi:///jndi/rmi://192.168.100.148:1099/jmxrmi'

def env = [(JMXConnector.CREDENTIALS): ["smx","smx"] as String[]]
def jmxUrl = new JMXServiceURL(serverUrl)
def connect = JMXConnectorFactory.connect(jmxUrl,env)
def server = connect.MBeanServerConnection

def query = new ObjectName('org.apache.activemq:Type=Queue,*')
def queues = server.queryNames(query,null)
printf "%-50s, %7s, %7s, %7s, %7s, %7s, %7s, %7s, %7s, %7s\n", "Name", "Avg QT", "Max QT", "Min QT", "Enq Cnt", "Dsp Cnt", "Exp Cnt", "Inf Cnt", "Consumrs", "Producr"

printf "%-50s, %7s, %7s, %7s, %7s, %7s, %7s, %7s, %7s, %7s\n", "----", "------", "------", "------", "-------", "-------", "-------", "-------", "--------", "-------"

queues.each { queue ->
        def queueBean = new GroovyMBean(server,queue)
        printf "%-50s, %7.2g, %7.2g, %7.2g, %7s, %7s, %7s, %7s, %7s, %7s\n",
                queueBean.Name,
                queueBean.AverageEnqueueTime as Float, queueBean.MaxEnqueueTime as Float, queueBean.MinEnqueueTime as Float,

                queueBean.EnqueueCount, queueBean.DispatchCount, queueBean.ExpiredCount, queueBean.InFlightCount,
                queueBean.ConsumerCount, queueBean.ProducerCount
}
