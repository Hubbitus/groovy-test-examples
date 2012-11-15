#!/bin/env groovy

class Base{

	public static String dump(){
		"delegate=${delegate.class}; owner=${owner.class}; this=${this.class}; delegate.value=${delegate.value}"
	}

	public Base(){
		[S, I].each{c->
			c.metaClass.static.init = {
				cls.metaClass.dump = {
					"delegate=${delegate.class}; owner=${owner.class}; this=${this.class}; delegate.value=${delegate.value}"
				}
			}
			c.init();
		}
	}
}

class S extends Base{
public static Class cls = String;
}

class I extends Base{
public static Class cls = Integer;
}

Base base = new Base();

println "test".dump();
