// Demonstration of http://jira.codehaus.org/browse/GROOVY-7094

class A{
    def some = '_A.some field direct_';

    def getSome(){
        '-A.getSome()-'
    }
    
    def test(){
        println "this=$this"
        println "super=$super"
        println "this.some: ${this.some}"
        println "this.@some: ${this.@some}"
    }
}

class B extends A{
    def getSome(){
        '-B.getSome()-'
    }
    
    def test(){
        println "this=$this"
        println "super=$super"
        println "this.some: ${this.some}"
//        println "this.@some: ${this.@some}" //groovy.lang.MissingFieldException: No such field: some for class: B
        println "super.some: ${super.some}"
        println "super.@some: ${super.@some}"
        println "super.getSome(): ${super.getSome()}"
    }
}

A a = new A();
B b = new B();

println 'From A:'
a.test()
println "outside class: a.some: ${a.some}"
println "outside class: a.@some: ${a.@some}"

println 'From B:'
b.test()
println "outside class: b.some: ${b.some}"
//println "outside class: b.@some: ${b.@some}" // groovy.lang.MissingFieldException: No such field: some for class: B
