#!/bin/env groovy

import groovy.xml.MarkupBuilder

	if (!args.length){
	println 'You MUST provide argument - file for processing';
	System.exit(1);
	}

//println new File('/home/pasha/1C/tools/results/debug.xml').text;

//def records = new XmlParser().parseText(new File('/home/pasha/1C/tools/results/debug.xml').text);
def xml = new XmlParser().parseText('-' == args[0] ? System.in.text : new File(args[0]).text);

//println xml.errors.error.dump()
//println xml.records.record.collect{ "${it.pc.'@UID'[0]};${it.codeKDF.text()}" }.join("\n");

println xml.errors.error.findAll{ it.@code }.size()

