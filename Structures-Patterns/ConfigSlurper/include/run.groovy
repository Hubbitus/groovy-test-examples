//1) Example of http://naleid.com/blog/2009/07/30/modularizing-groovy-config-files-with-a-dash-of-meta-programming
def configObject = new ConfigSlurper().parse( SecurityConfig )
println configObject


//2) My example as Hack https://github.com/Hubbitus/groovy-test-examples/commit/38f521f64bb8999861537922317b61e83045b08e
// It fails if included config for example contain imports
def configObjectPlain = new ConfigSlurper().parse(
"""
	class SecurityConfig extends ComposedConfigScript {
		def run() { // normal contents of a config file go in here
			${new File('SecurityConfigPlain.groovy').text}
		}
	}
"""
)
println configObjectPlain


//3) Variant with CompileConfiguration
//3.1) Base example from http://groovy.codehaus.org/Using+Testing+Frameworks+with+Groovy
import org.codehaus.groovy.control.CompilerConfiguration

abstract class ScriptBaseTestScript extends Script {
	def foo() {
		"this is foo"
	}
}

def configuration = new CompilerConfiguration();
configuration.setScriptBaseClass('ScriptBaseTestScript');;
def shell = new GroovyShell(this.class.classLoader, new Binding(), configuration);
assert shell.evaluate("foo()"), "this is foo";

println shell.evaluate("foo()");

// 3.2) Both together
/*
def configuration1 = new CompilerConfiguration();
configuration1.setScriptBaseClass('ComposedConfigScript');
def shell1 = new GroovyShell(this.class.classLoader, new Binding(), configuration);
println shell1.evaluate(new File('SecurityConfigPlain.groovy').text)
*/
println ( (new SecurityConfigPlain() as Script) )