@Singleton class Config{
    public def var1 = 'one'
    String var2 = 'two'

    /**
    * Due to the Groovy BUG http://jira.codehaus.org/browse/GROOVY-6264 it is not possible dinamically override static setProperty
    * Nor invokeMethod (http://groovy.codehaus.org/ExpandoMetaClass+-+Overriding+static+invokeMethod) called for direct statement like Config.var1 = '11' and works only for methods
    * End even direct Config.metaClass.static.setVar1 = {String f,String value-> "QAZ"} does not called (to cnsider dinamyc generation)
    **/
    public static set(String name, value){
        Config.instance."$name" = value;
    }

    static{
        Config.metaClass.static.propertyMissing = {prop->
            instance."$prop"
        }
    }
}


//        Config.metaClass.static.propertyMissing = {prop->
  //          instance."$prop"
    //    }
/*
        Config.metaClass.static.propertyMissing = {prop, val->
            println "static.propertyMissing = {prop, val"
        }
        Config.metaClass.static.methodMissing = {prop, args->
//            instance."$prop"
            println "methodMissing: $prop"
        }
        Config.metaClass.static.getProperty = {prop, val->
            instance."$prop" = val
        }
        
        Config.metaClass.'static'.invokeMethod = {prop, args->
            println ".invokeMethod = {$prop, $args->"
        }
*/
//Config c = new Config()
[ Config.instance, Config.instance ]

println Config.var1
Config.var1 = '11'
println Config.var1

println Config.var2
Config.var2 = '22'
println Config.var2