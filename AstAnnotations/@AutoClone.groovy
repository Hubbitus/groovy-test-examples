package imus.asa.prefetch

import groovy.transform.AutoClone
import groovy.transform.EqualsAndHashCode
import groovy.transform.InheritConstructors
import static groovy.transform.AutoCloneStyle.*

// Initially from: http://groovyconsole.appspot.com/script/830001

@AutoClone(style=CLONE)
class Address{
	String street
	Integer building;
}

@AutoClone(style=CLONE)
class Name{
	String name, family
}

@AutoClone(style=CLONE, includeFields=true)
class Person{
	Name name
	public Address address;
}

Person person = new Person(name: new Name(name: 'One'), address: new Address(street: 'Some'))

Person clone = person.clone()

assert person != clone
assert person.name != clone.name
assert person.address != clone.address