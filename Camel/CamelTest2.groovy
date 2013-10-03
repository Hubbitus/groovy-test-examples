#!/usr/bin/env groovy

// http://saltnlight5.blogspot.se/2012/08/getting-started-with-apache-camel-using.html
// http://mrhaki.blogspot.ru/2009/04/use-apache-camel-plugin-in-grails.html

@Grab('org.apache.camel:camel-core:2.10.0')
@Grab(group='org.apache.camel', module='camel-mail', version='2.10.0')
@Grab('org.slf4j:slf4j-simple:1.6.6')
import org.apache.camel.*
import org.apache.camel.impl.*
import org.apache.camel.builder.*

def camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
    def void configure() {
        from("imap://mail.spb.ant-inform.ru?username=p.alexeev@spb.ant-inform.ru&password=pasha_test&folderName=INBOX.test&consumer.delay=10000")
//        .filter {
//          true
//           println "Filter: $it"
//            it.in.headers.subject.contains('Analytics')
//        }
        .process({ exchange ->
            println "exchange=$exchange"
            println "exchange.dump=${exchange.dump()}"
//            exchange.in.attachments.each { attachment ->
//                def datahandler = attachment.value
//                def xml = exchange.context.typeConverter.convertTo(String.class, datahandler.inputStream)
//                def file = new File('.', datahandler.name) << xml
//                log.info "Saved " + file.name
//            }
        } as Processor)
    }
})
camelContext.start()

addShutdownHook{ camelContext.stop() }
synchronized(this){ this.wait() }
