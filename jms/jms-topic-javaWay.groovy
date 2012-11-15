#!/usr/bin/groovy

// http://stackoverflow.com/questions/6755071/groovyconsole-activemq-error-noclassdeffounderror-could-not-initialize-class-o

import org.apache.activemq.*
import org.apache.activemq.command.*
import org.apache.activemq.transport.tcp.*
import javax.jms.*
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

//@ Grab(group='org.apache.activemq', module='activemq-all', version='5.5.0')
//@ Grab(group='org.slf4j', module='slf4j-api', version='1.6.2')
//@ Grab(group='org.slf4j', module='slf4j-log4j12', version='1.6.2')


class TextListener implements MessageListener{
	/**
	 * Casts the message to a TextMessage and displays
	 * its text. A non-text message is interpreted as the
	 * end of the message stream, and the message
	 * listener sets its monitor state to all done
	 * processing messages.
	 *
	 * @param message    the incoming message
	 */
	void onMessage(javax.jms.Message message) {
		TextMessage  msg = (TextMessage) message;

		try {
			println("SUBSCRIBER: Reading message: " + msg.getText());
		} catch (JMSException e) {
			System.err.println("Exception in onMessage(): " + e.toString());
		}
	}
}


def connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.100.148:61616");
def connection = connectionFactory.createTopicConnection();
connection.setClientID('SomeTest1');
def session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
def topic = session.createTopic("IMUS.CMD.RESPONSE");
//def topicSubscriber = session.createDurableSubscriber(topic, "MakeItLast");
def topicSubscriber = session.createDurableSubscriber(topic, "Test");
def topicListener = new TextListener();
topicSubscriber.setMessageListener(topicListener);
connection.start();

try {
	System.in.read();
} catch (IOException e) {
	e.printStackTrace();
}

session.close();
connection.close();