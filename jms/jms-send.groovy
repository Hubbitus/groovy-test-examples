#!/usr/bin/groovy

// http://stackoverflow.com/questions/6755071/groovyconsole-activemq-error-noclassdeffounderror-could-not-initialize-class-o

import org.apache.activemq.*
import org.apache.activemq.command.*
import javax.jms.*

@Grab(group='org.apache.activemq', module='activemq-all', version='5.5.0')
@Grab(group='org.slf4j', module='slf4j-api', version='1.6.2')
@Grab(group='org.slf4j', module='slf4j-log4j12', version='1.6.2')
def connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.100.148:61616");
def connection =connectionFactory.createConnection();

def session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
def dest = new ActiveMQQueue("ALL.CMD.IN.STR");
def producer = session.createProducer(dest);
connection.start();

def MapMessage message = session.createMapMessage();
message.setString("test", "test string");

producer.send(message);

session.close();
connection.close();