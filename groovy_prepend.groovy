#!/bin/env groovy

/*
class StringPrepender {
	static String prepend(String string, String prep) {
//	string = prep + string
	string = "qwerty";
		return "-!-";
	}
}
*/

String.metaClass.prepend = {add->
	delegate.value = add + delegate;
//	delegate.replaceAll("te", "New content")
//	println delegate.value;
//	println add
//	return "ttt"
	}

def s = "text";
println "Original string: '${s}'"

s.prepend('QAZ');

println "Prepended string: '${s}'"
