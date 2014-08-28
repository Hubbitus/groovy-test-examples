#!/bin/env groovy

import groovy.xml.*

def t = new test();

println new MarkupBuilder().transformValue("test & ttt");

class test{
	def one = 1;
	def two = 2;
}