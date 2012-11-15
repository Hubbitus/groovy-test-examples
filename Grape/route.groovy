#!/usr/bin/groovy

// http://www.andrejkoelewijn.com/wp/2009/02/28/groovy-and-grape-easiest-way-to-send-gtalk-message-with-apache-camel/

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.language.groovy.GroovyRouteBuilder;

@Grab(group='org.apache.camel', module='camel-groovy', version='1.6.0')
@Grab(group='org.apache.camel', module='camel-xmpp', version='1.6.0')
@Grab(group='org.apache.camel', module='camel-core', version='1.6.0')
class SampleRoute extends GroovyRouteBuilder {
  public void configure(){
    from("file:///tmp/jabber").
      to("xmpp://talk.google.com:5222/touser@gmail.com?serviceName=gmail.com&user=fromuser&password=secret");
  }
}

def camelCtx = new DefaultCamelContext()
camelCtx.addRoutes(new SampleRoute());
camelCtx.start();