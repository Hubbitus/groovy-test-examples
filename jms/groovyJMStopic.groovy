#!/bin/env groovy

this.getClass().classLoader.rootLoader.addURL(new File("file:///home/pasha/temp/groovy-test/groovyjms-0.2-20081120.jar").toURL())

//import groovy.jms.JMS;

new JMS(){

}