#!/bin/env groovy
// http://pleac.sourceforge.net/pleac_groovy/userinterfaces.html

//----------------------------------------------------------------------------------
// Not idiomatic for Groovy to use text-based applications here.

// Using jcurses: http://sourceforge.net/projects/javacurses/
// use Toolkit.screenWidth and Toolkit.screenHeight

// 'barchart' example
@Grab(group='com.baulsupp.kolja', module='jcurses', version='0.9.5.3')
import jcurses.system.Toolkit
numCols = Toolkit.screenWidth
rand = new Random()
if (numCols < 20) throw new RuntimeException("You must have at least 20 characters")
values = (1..5).collect { rand.nextInt(20) }  // generate rand values
max = values.max()
ratio = (numCols - 12)/max
values.each{ i ->
    printf('%8.1f %s\n', [i as double, "*" * ratio * i])
}

// gives, for example:
//   15.0 *******************************
//   10.0 *********************
//    5.0 **********
//   14.0 *****************************
//   18.0 **************************************
// Run from command line not inside an IDE which may give false width/height values.
//----------------------------------------------------------------------------------