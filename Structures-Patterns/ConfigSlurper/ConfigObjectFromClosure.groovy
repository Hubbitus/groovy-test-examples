// Interesting example from http://www.redtoad.ca/ataylor/2013/01/creating-a-groovy-configobject-from-a-closure/

class ClosureScript extends Script {
    Closure closure
    def run() {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = this
        closure.call()
    }
}

def config = {
  environments {
    development {
      host = 'localhost'
      port = 8080
    }
  }
}
def env = 'development'
def settings = new ConfigSlurper(env). parse(new ClosureScript(closure: config))
println "host is $settings.host, port is $settings.port"