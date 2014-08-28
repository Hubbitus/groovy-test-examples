// http://groovy.codehaus.org/gapi/groovy/lang/Lazy.html

class A{
String str;

    A(){
        println 'Constructor A()'
    }
    
    A(String s){
        println "Constructor A(String s=${s})"
        str = s;
    }
    
    String toString(){
        "A{str=${str}}"
    }
}

class Test{
    @Lazy a = new A();
    String s = 'some string'
    @Lazy b = new A('first')
    @Lazy c = { new A('second') }()
    @Lazy d = new A(s)
    @Lazy lazyString = { println 'Called lazys init'; 'PREFIX: ' + s }()
}

'test'

Test t = new Test()

//println t.s
//println t.a
//println t.b
//println t.c
t.s = 'another string'
//println t.d
println ([ t.lazyString, t.lazyString ])