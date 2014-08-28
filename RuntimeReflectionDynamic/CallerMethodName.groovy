import org.codehaus.groovy.runtime.StackTraceUtils

class E{
    public static String callerMethodName(int stackDepth = 4){
//        StackTraceUtils.sanitize(new Throwable()).stackTrace[1].methodName
            StackTraceUtils.sanitize(new Throwable()).stackTrace[stackDepth].methodName
    }
}

class A{
    public aa(){
        E.callerMethodName();
    }

    public ab(){
        aa()
    }
}

A a = new A();
println a.ab()
