#!/bin/env groovy

import groovy.transform.ToString

@ToString
class A{
	@ToString
	static class Aa{
		public String s = 'String';
		Integer i = 777;
		protected int pti = 66;
		private int pr = 666;
	}
}


A.Aa aa = new A.Aa();

println aa;