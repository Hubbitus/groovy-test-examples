#!/usr/bin/env groovy

// http://saltnlight5.blogspot.se/2012/08/getting-started-with-apache-camel-using.html
// http://mrhaki.blogspot.ru/2009/04/use-apache-camel-plugin-in-grails.html

@Grab('org.apache.camel:camel-core:2.10.0')
@Grab('org.apache.camel:camel-mail:2.10.0')
@Grab('org.apache.camel:camel-groovy:2.10.0')
@Grab('org.slf4j:slf4j-simple:1.6.6')
import org.apache.camel.*
import org.apache.camel.impl.*
//import org.apache.camel.builder.*
import org.apache.camel.language.groovy.GroovyRouteBuilder


def camelContext = new DefaultCamelContext()
camelContext.addRoutes(new GroovyRouteBuilder() {
    def void configure() {
//        from("imap://mail.spb.ant-inform.ru?username=p.alexeev@spb.ant-inform.ru&password=pasha_test&folderName=INBOX.Zabbix-alerts.wait.2013-10-18 Олег&consumer.delay=10000")
        from('imap://mail.spb.ant-inform.ru?username=imus.monitor@spb.ant-inform.ru&password=lWX9Dta8&folderName=INBOX&consumer.delay=10000')
//        .filter {
//          true
//           println "Filter: $it"
//           println "Filter . Dump(): ${it.in.message.getContentStream().text.dump()}"
//           println "Filter . Dump(): ${it.in.message.getContent().dump()}"
//
//            it.in.headers.subject.contains('Analytics')
//        }
        .process({ exchange ->
            println '----------------------------------------------------------------------------------------------------'
            println '++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'
            println '===================================================================================================='
            println "exchange=$exchange"
            println '===================================================================================================='
            println "exchange.dump=${exchange.dump()}"
            println '===================================================================================================='
//            println "exchange.in.message.inputStream.text=${exchange.in.message.inputStream.text.dump()}"
            println "MailBody: ${exchange.in.message.content.getBodyPart((int)0).content}";
/*
            exchange.in.message.getContent().each{
                println "MultiPart.content: ${it.getBodyPart((int)0).content.dump()}"
            }
*/
            println '===================================================================================================='
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
