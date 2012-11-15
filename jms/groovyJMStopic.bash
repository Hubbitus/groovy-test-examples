#!/bin/bash

set -x

#groovy --classpath $( echo `ls -1 target/classes/groovy/jms/*` | sed 's/ /:/g' ) groovyJMStopic.groovy

groovy --classpath groovyjms-0.2-20081120.jar --debug groovyJMStopic.groovy
