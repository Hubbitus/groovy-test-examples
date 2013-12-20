#!/usr/bin/env groovy

config = new ConfigSlurper().parse(new File('Config.groovy').text).config;

println "config.one=${config.one}"
println "config.two=${config.two}"
println "config.three=${config.three}"
println '====='
println "config.digits.one=${config.digits.one}"
println "config.digits.two=${config.digits.two}"
println "config.digits.three=${config.digits.three}"
println '====='
println "config.digitsMap.one=${config.digitsMap.one}"
println "config.digitsMap.two=${config.digitsMap.two}"
println "config.digitsMap.three=${config.digitsMap.three}"
println '====='
println "config.digitsMixMap.one=${config.digitsMixMap.one}"
println "config.digitsMixMap.two=${config.digitsMixMap.two}"
println "config.digitsMixMap.three=${config.digitsMixMap.three}"
println "config.digitsMixMap.four=${config.digitsMixMap.four}"
println "config.digitsMixMap.typed=${config.digitsMixMap.typed.dump()}"
println '====='
println "config.digitsMix.one=${config.digitsMix.one}"
println "config.digitsMix.two=${config.digitsMix.two}"
println "config.digitsMix.three=${config.digitsMix.three}"
println "config.digitsMix.four=${config.digitsMix.four}"
println "config.digitsMix.typed=${config.digitsMix.typed.dump()}"
println '====='
println "config.digitsMix.dump()=${config.digitsMix.dump()}"
println "config.digitsMixMap.dump()=${config.digitsMixMap.dump()}"
