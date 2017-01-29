#!/bin/env groovy

// Idea @BUG https://youtrack.jetbrains.com/issue/IDEA-107789

println this.getClass().getResource('/')
println Thread.currentThread().getContextClassLoader().getClass().getResource('/') // Works (http://stackoverflow.com/questions/9939045/groovy-script-classpath-issue-with-systemclassloader)
println Thread.currentThread().getContextClassLoader().getResource('/') // Don't work

println ClassLoader.systemClassLoader.getClass().getResource('/') // Don't work
println ClassLoader.systemClassLoader.getResource('/') // Don't work
println ClassLoader.getResource('/') // Don't work
println new GroovyClassLoader(Thread.currentThread().getContextClassLoader()).getResourceAsStream('/') // Don't work

