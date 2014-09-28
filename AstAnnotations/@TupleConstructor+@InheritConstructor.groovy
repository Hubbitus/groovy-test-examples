// http://permalink.gmane.org/gmane.comp.lang.groovy.user/65835

import groovy.transform.Canonical
import groovy.transform.TupleConstructor
import groovy.transform.ToString
import groovy.transform.InheritConstructors

class Base{
    // java.lang.NoSuchMethodError: Base: method <init>()V not found
//    Base(){ println "Called Base()" }

    Base(Map map){
        println "Called Base(Map map)"
        for (entry in map) {
            if (entry.value instanceof Closure){
                ((Closure)entry.value).delegate = this;
                ((Closure)entry.value).resolveStrategy = Closure.DELEGATE_ONLY;
            }
            this."${entry.key}" = entry.value;
        }
    }
}

// !!! @TupleConstructor(force=true) XOR @InheritConstructors may be used only!!! See users@ ML question with subject "@TupleConstructor together with @TupleConstructor"
//@InheritConstructors
@TupleConstructor(force=true, includeFields=true)
@ToString(includeNames=true, includeFields=true)
class Person extends Base{
    String name
    public Integer age

/*
    def setAge(Integer a){
        age = a + 1000
    }
*/
}

/*
//@InheritConstructors
@TupleConstructor(force=true, includeSuperFields=true, includeSuperProperties=true)
@ToString(includeNames=true, includeSuper=true)
class PersonPlus extends Person{
    def addon;
}
*/

println Person.constructors
//Person p = new Person();

[ new Person(name: 'Pavel', age: 100), new Person('Pavel1', 200) ]
//PersonPlus pp = new PersonPlus('Pavel', 33, '+')