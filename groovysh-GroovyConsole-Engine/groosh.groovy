#!/bin/env groovy

// https://kenai.com/projects/groovy-groosh/pages/Home

@Grapes([
	@Grab(group='org.codehaus.groovy.modules.groosh', module='groovy-groosh',version='[0.3.5,)'),
	@GrabConfig(systemClassLoader=true)
])
import groosh.Groosh
Groosh.withGroosh(this)

cat('/home/pasha/.bashrc') >> stdout
// Don't work
//cat('~/.bashrc') >> stdout

//ls() >> stdout
// Don't work
//ls('*.groovy') >> stdout
