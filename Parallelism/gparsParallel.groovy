import imus.ais.ProgressLogger
import static groovyx.gpars.GParsPool.runForkJoin
import groovyx.gpars.GParsPool

@Grab(group='org.codehaus.gpars', module='gpars', version='0.10')
class Config {
    static DATA_COUNT = 2**19
    static THREADS = 4
}

// Fake i18n
Class.metaClass.static._ = {String key, ...params-> sprintf(key, params)}; // Static call _(...)
Object.metaClass._ = {String key, ...params-> sprintf(key, params)}; // Instance call _(...)
Class.metaClass.static._if = {boolean condition, String key, ...params-> if(condition) sprintf(key, params) else '';}; // Static call _if(...)
Object.metaClass._if = {boolean condition, String key, ...params-> if(condition) sprintf(key, params) else ''; }; // Instance call _if(...)

items = [] as List<Integer>
items.addAll(1..Config.DATA_COUNT)
Collections.shuffle(items)

println "expectedMax = ${Config.DATA_COUNT}"

(1..20).each{i->
println '========================================='
println "Parrallel computing in $i threads"
//    GParsPool.withPool(Config.THREADS) {
    GParsPool.withPool(i){
//    computedMax = items.parallel.max()
//    println "Parralel computedMax = ${computedMax}"
        ProgressLogger.benchmark(items.parallel.&max, 1000, System.out.&println)
    }
}

println "Serial computedMax = ${items.max()}"
ProgressLogger.benchmark(items.&max, 1000, System.out.&println)
