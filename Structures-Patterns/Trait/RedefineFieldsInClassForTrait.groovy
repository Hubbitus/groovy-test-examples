class C{
    static def conf = new ConfigSlurper().parse(
'''
dir = '/root'

subDir = { "$dir/sub" }
'''
    );
}

trait T{
    def getConf(){
        C.conf
    }
    
    void testConf(){
        println conf
    }
}

class Tt implements T{
    // !!! Even trait method WILL use that field
    def conf;
    
    def Tt(){
        conf = C.conf.clone();
    }
}


Tt t = new Tt();
println "C.conf.dir=${C.conf.dir}"
println "t.testConf()=${t.testConf()}"
C.conf.dir = '/home'
println "C.conf.dir=${C.conf.dir}"
println "t.testConf()=${t.testConf()}"
