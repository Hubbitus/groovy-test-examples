#!/usr/bin/env groovy

// http://saltnlight5.blogspot.se/2012/08/getting-started-with-apache-camel-using.html

@Grab('org.apache.camel:camel-core:2.10.0')
@Grab('org.slf4j:slf4j-simple:1.6.6')
import org.apache.camel.*
import org.apache.camel.impl.*
import org.apache.camel.builder.*

def camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
    def void configure() {
        from("timer://jdkTimer?period=3000")
            .to("log://camelLogger?level=INFO")
            .process(new Processor() {
                def void process(Exchange exchange) {
                    println("Hello World!" + exchange)
                }
            })
    }
})
camelContext.start()

addShutdownHook{ camelContext.stop() }
synchronized(this){ this.wait() }
