#!/bin/env groovy

// Reported: http://jira.codehaus.org/browse/GROOVY-5684

abstract class Base{
//public static Class cls = Base;

// MUST be redefined, but static methods can't be abstract: http://groovy.codehaus.org/JN3025-Inheritance
public static Class getCls(){};

//	public static Class getCls(){
//		return Base;
//	}

	public static String getTestName(){
println "getTestName(): this=$this; cls=${cls}";
return "=${cls.simpleName}=";
return 'T';
	}

//	public static void init(ths){
//println "init(ths): ths=$ths; this=$this; ths.cls=${ths.cls}";
//		ths.cls.metaClass.static.getTestName = ths.&getTestName;
//	}

	public static void init(ths){
println "init(ths): ths=$ths; this=$this; ths.cls=${ths.cls}";

println ths.metaClass.methods.findAll{ it.name == 'getTestName'};
//println ths.metaClass.methods.findAll{ it.name == 'getTestName'}*.dump();
//println ths.metaClass.methods.findAll{ it.name == 'getTestName' && it.cachedClass.simpleName == ths.simpleName }*.dump();
//println ths.metaClass.getMetaMethod('getTestName').cachedClass.name;
//println ths.metaClass.getMetaMethod('getTestName', [Base] as Class[] );

//if (ths.metaClass.getMetaMethod('getTestName').cachedClass.name != ths.class.simpleName){ // Base method does not redefined, do do it
ths.metaClass.static.getTestName = this.&getTestName;
//}

println ths.metaClass.methods.findAll{ it.name == 'getTestName'};

		ths.metaClass.static.init = {
//this.metaClass.static.getTestName = this.&getTestName;
			ths.cls.metaClass.static.getTestName = ths.&getTestName;
		};
		ths.init();
	}
}

class A extends Base{
//public static Class cls = Integer;

	public static Class getCls(){
		return Integer;
	}

//	public static String getTestName(){
//		return "AAA=${cls.simpleName}=AAA";
//	}

	static{
		init(this);
	}
}

class B extends Base{
//public static Class cls = String;

	public static Class getCls(){
		return String;
	}

	public static String getTestName(){
		return "BBB=${cls.simpleName}=BBB";
	}

	static{
		init(this);
	}
}

A a = new A();
B b = new B();

println ( ['a[Integer.getTestName()]': Integer.getTestName(), 'b[String.getTestName()]': String.getTestName()] );
