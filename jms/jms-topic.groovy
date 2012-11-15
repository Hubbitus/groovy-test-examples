#!/usr/bin/groovy

// http://stackoverflow.com/questions/6755071/groovyconsole-activemq-error-noclassdeffounderror-could-not-initialize-class-o

import org.apache.activemq.*
import javax.jms.*
import org.apache.log4j.*
import groovy.jms.JMS

@Grab(group='org.apache.activemq', module='activemq-all', version='5.5.0')
//@ Grab(group='org.slf4j', module='slf4j-api', version='1.6.2')
@Grab(group='org.slf4j', module='slf4j-log4j12', version='1.6.2')
//@Grab(group='org.slf4j', module='slf4j-nop', version='1.6.2')
//@ Grab(group='org.slf4j', module='slf4j-log4j12', version='1.6.5')
//@ Grab(group='org.slf4j', module='log4j-over-slf4j', version='1.6.5')
@Grab(group='log4j', module='log4j', version='1.2.17')

brokerUrl = 'tcp://192.168.100.148:61616'

//ActiveMQConnectionFactory jms = new ActiveMQConnectionFactory(brokerURL: brokerUrl);

//def factory = new ActiveMQConnectionFactory("tcp://192.168.100.148:61616");
//def connection = connectionFactory.createConnection();

//use(JMS){
//	println jms;
//     jms.topic("IMUS.CMD.RESPONSE").subscribe(
//         {Message m -> println "hey i got a message. it says, '${m.text}'"} // as MessageListener
//     );
//}

/*
new JMS(){
	def topic0 = factory.topic("IMUS.CMD.RESPONSE");
 // your jms code
}
*/

new JMS(){
	println connection;
	println session;
}