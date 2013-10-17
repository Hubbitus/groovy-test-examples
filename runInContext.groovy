// http://sdicc.blogspot.ru/2012/12/groovy-dsl-executing-scripts-within.html

class Calculator {
    double w = 3;

    double hypotenuse(double width, double height) {
        Math.sqrt(w * w + height * height)
    }
}

def runInContext(Object context, String script) {
    Closure cl = (Closure) new GroovyShell().evaluate("{->$script}")
    cl.delegate = context
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
}

def calculate(String script) {
    def calculator = new Calculator()
    runInContext(calculator, script)
}

//assert 5.0 ==
println calculate("hypotenuse(100.0, 4.0)")
//////////////

class T{
String field = 'some field'

    public prn(String arg){
        println "[$arg]";
    }

    double w = 3;

    double hypotenuse(double width, double height) {
        Math.sqrt(w * w + height * height)
    }

    public void exec(){
//        def gs = new GroovyShell( new Binding(this.binding) )
//        def gs = new GroovyShell( new Binding(getThis: { this } ) )
//        gs.this = this;
//        gs.evaluate( "println 'field:' + this.field" );

        def runInContext = {Object context, String script->
            Closure cl = (Closure) new GroovyShell().evaluate("{->$script}")
            cl.delegate = context
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
        }

        def calculate = {String script ->
//            def calculator = new Calculator()
//            runInContext(calculator, script)
            runInContext(this, script)
        }

        println calculate("hypotenuse(100.0, 4.0)")
//        calculate("""prn "w=$w; field=$field";""")
        runInContext(this, """prn "w=$w; field=$field";""")
    }
}

T t = new T();
t.exec();
