import groovy.transform.TupleConstructor

// Illustration of groovy BUG https://jira.codehaus.org/browse/GROOVY-7288

trait Some{
    static final String STATIC = 'static data' // Commenting it out make it work

    String so(){
        println ('STATIC=' + STATIC)
    }
}

class Delegator implements Some{
    String delegated(){
        println 'delegated work'
    }
}

class SomeClass{
    @Delegate Delegator delegator = new Delegator();
}

def s = new SomeClass()
s.delegated()

/*
result:
2 compilation errors:

Can't have an abstract method in a non-abstract class. The class 'SomeClass' must be declared abstract or the method 'java.lang.String Some__STATIC$get()' must be implemented.
 at line: 17, column: 1

Can't have an abstract method in a non-abstract class. The class 'SomeClass' must be declared abstract or the method 'void Some__STATIC$set(java.lang.String)' must be implemented.
 at line: 17, column: 1
*/