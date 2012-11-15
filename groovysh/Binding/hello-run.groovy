// http://groovy.codehaus.org/Embedding+Groovy

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

String[] roots = [ "./" ];
GroovyScriptEngine gse = new GroovyScriptEngine(roots);
Binding binding = new Binding();
binding.setVariable("input", "world");
gse.run("hello.groovy", binding);
System.out.println(binding.getVariable("output"));