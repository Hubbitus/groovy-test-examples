
String outer = 'Outer string';

// http://sourceforge.net/apps/phpbb/freeplane/viewtopic.php?f=1&t=272
def engine = new GroovyScriptEngine('/home/pasha/temp/groovy-test/');
def value = engine.run('embedding.groovy', binding);
