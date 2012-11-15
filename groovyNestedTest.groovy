#!/bin/env groovy

def t = new test();
t.testMethod();

class test{
	// Will be read by demand from XML
	private static HashMap MAP = [
		one: 1,
		two: 2,
		three: 3,
		four: 4
	];

	void testMethod(){

		println MAP['one'];
		println this.MAP['two'];

		try{
			[ 1, 2 ].each{
				[ 1, 2 ].each{
					println MAP['three'];
					println this.MAP['four'];
				}
			}
		}
		catch(Throwable t){
		}
	}
}