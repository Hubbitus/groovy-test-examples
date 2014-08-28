#!/usr/bin/env groovy

import java.util.logging.*

System.setProperty('java.util.logging.SimpleFormatter.format', '[%1$tF %1$tT] %4$s: %5$s%n');

// Manual configure logger to be standalone (http://stackoverflow.com/questions/4292599/how-to-redirect-java-util-logging-to-a-file)
log = Logger.getLogger("main");
logHandler = new FileHandler('/home/pasha/temp/processKnown.log', 5*1024*1024 /* 5Mb */, 3 /* LogRotateCount */, false);
Formatter f = new SimpleFormatter()
logHandler.setFormatter(f);
log.addHandler(logHandler);

log.info('Some log');
//log.info(f.format);