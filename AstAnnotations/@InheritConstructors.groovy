import groovy.transform.Canonical
import groovy.transform.TupleConstructor
import groovy.transform.InheritConstructors

@TupleConstructor
class DelegateConstructorBase{
    /**
     * Constructor to allow groovy map syntax and additionally init resulting metaclass also
     * {@link http://stackoverflow.com/a/6268928/307525}
     *
     * @param map
     */
    DelegateConstructorBase(Map map){
        for (entry in map) {
            if (entry.value instanceof Closure){
                ((Closure)entry.value).delegate = this;
                ((Closure)entry.value).resolveStrategy = Closure.DELEGATE_ONLY;
            }
            this."${entry.key}" = entry.value;
        }
    }
}

@InheritConstructors
class A extends DelegateConstructorBase{
    public String getT(){
        return "A:getT()";
    }

    public String str = "A";
    
    public returnStr = {->
        return str;
    }

//    A(Map map){ super(map); }
}

@InheritConstructors
class B extends A{
    public String str = "B";

//    B(Map map){ super(map); }
}


String str = 'Outer str'
def String getT(){ return "_outer_:getT()"; }

A a = new A([:]);
//a.returnStr()
B b = new B([:]);
B bb = new B(returnStr: { str + ":" + getT()})
bb.returnStr.delegate = bb
bb.returnStr.resolveStrategy = Closure.DELEGATE_ONLY
//b.returnStr.resolveStrategy = Closure.DELEGATE_FIRST
//bb.returnStr.delegate = bb
[ a, b, bb, a.returnStr(), b.returnStr(), bb.returnStr(), b.returnStr.resolveStrategy, bb.returnStr.delegate ]
