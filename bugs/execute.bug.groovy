#!/bin/env groovy

// https://issues.apache.org/jira/browse/GROOVY-7545

String cmd = "sh -c 'echo test' "
println "cmd: $cmd"
def proc = cmd.execute()
proc.waitForProcessOutput(System.out, System.err)
proc.waitFor()