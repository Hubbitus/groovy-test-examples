// Example of http://naleid.com/blog/2009/07/30/modularizing-groovy-config-files-with-a-dash-of-meta-programming

def configObject = new ConfigSlurper().parse( SecurityConfig )

println configObject
