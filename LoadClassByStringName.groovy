
class someA{
}

String cn = 'A'

//def c = new someA()
//c.class.name

//someA.getSimpleName()

// http://groovy.329449.n5.nabble.com/groovy-Class-forName-td359505.html
this.class.classLoader.loadClass('someA').newInstance()