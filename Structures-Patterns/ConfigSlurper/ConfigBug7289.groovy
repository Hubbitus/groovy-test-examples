#!/opt/groovy-2.3.6/bin/groovy

// https://jira.codehaus.org/browse/GROOVY-7289

// #!/usr/bin/env groovy
ConfigObject config = new ConfigSlurper().parse(
'''
config{
		dir = '..';

		transform = {
			"_${dir}_"
		}
}
'''
).config;

assert '..' == config.dir
assert '_.._' == config.transform()
/*
Assertion failed:

assert '_.._' == config.transform()
              |  |      |
              |  |      _[:]_
              |  [dir:.., transform:script14230891097201063544444$_run_closure1$_closure2@101952da]
              false
*/

ConfigObject configFix = new ConfigSlurper().parse(
'''
config{
		dir = '..';

		transform = {
			print "$delegate" // FIX it (black magick?). Something like 'println "TT: $delegate"' also work

//			delegate // not work
//			println delegate // not work
//			println 'TT' + delegate // not work
//			println "TT: $delegate" // BLACK MAGIC???

			"_${dir}_"
		}
}
'''
).config;

assert '..' == configFix.dir
assert '_.._' == configFix.transform()
