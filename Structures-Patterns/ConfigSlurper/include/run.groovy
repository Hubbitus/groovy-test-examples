// Example of http://naleid.com/blog/2009/07/30/modularizing-groovy-config-files-with-a-dash-of-meta-programming

def configObject = new ConfigSlurper().parse( SecurityConfig )
println configObject


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


//println ( new GroovyClassLoader().parseClass( new SecurityConfigPlain() ) )

