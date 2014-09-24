#!/opt/groovy-2.3.6/bin/groovy
// ^ Note groovy 2.3 required for @Builder annotation

// Groovy BUG: http://jira.codehaus.org/browse/GROOVY-7087

import groovy.transform.TupleConstructor
import groovy.transform.ToString
import groovy.transform.InheritConstructors
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy=SimpleStrategy, prefix='with')
@TupleConstructor
@ToString
class Person{
    String name;

    void setName(String n){
        println "Called setName(n=$n)"
        name = n;
    }
}

println Person.constructors

println  ( [ new Person(name: 'Map style'), new Person('@TupleConsructor'), new Person().with{ name = 'With style'; it }, new Person().withName('@Builder') ] )