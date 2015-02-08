// Reported Groovy bug https://jira.codehaus.org/browse/GROOVY-7295

trait Extra {
    String extra() { "I'm an extra method" }
}
class Something {
    String doSomething() { 'Something' }
}

def s = new Something() as Extra;
assert "I'm an extra method" == s.extra()
assert 'Something' == s.doSomething()

def i = new Integer(0) as Extra;
assert "I'm an extra method" == i.extra()

def conf = new ConfigSlurper().parse('some = 7')
// Uncomment it and you are receive error:
def ss = conf as Extra
/*
java.lang.NoClassDefFoundError: Extra
    at ConsoleScript24.run(ConsoleScript24:16)
Caused by: java.lang.ClassNotFoundException: Extra
    ... 1 more
*/