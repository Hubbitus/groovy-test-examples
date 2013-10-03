#!/usr/bin/env groovy

// http://groovy.dzone.com/articles/groovy-ride-camel

@Grab('org.apache.camel:camel-core:2.10.0')
@Grab('org.slf4j:slf4j-simple:1.6.6')

import org.apache.camel.impl.DefaultCamelContext

class MyRouteBuilder extends org.apache.camel.builder.RouteBuilder {
  void configure() {
    from("direct://foo").to("mock://result")
  }
}

mrb = new MyRouteBuilder()
ctx = new org.apache.camel.impl.DefaultCamelContext()
ctx.addRoutes mrb
ctx.start()

p = ctx.createProducerTemplate()
p.sendBody "direct:foo", "Camel Ride for beginner"

e = ctx.getEndpoint("mock://result")
ex = e.exchanges.first()
println "INFO> ${ex}"