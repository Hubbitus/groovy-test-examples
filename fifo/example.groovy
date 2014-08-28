#!/bin/env groovy

println 'start';
int no = 0;

FileInputStream fis;

while (fis = new FileInputStream('fifo')){
	fis.eachLine {line->
		println "${no++}) $line"
	}
fis.close();
}

println 'end';
