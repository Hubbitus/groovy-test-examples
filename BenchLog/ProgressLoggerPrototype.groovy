#!/bin/env groovy

import groovy.time.*

/**
 * Helper class logger of progress
 */
class AisProgressLogger {

private long start;
private long last;
private long total;
public int each;
public String objName;
private long current = 0;
public Closure outMethod;

	public AisProgressLogger(long total, int each = 100, String objName = 'object', Closure outMethod = { log.info(it) }){
		this.total = total;
		start = System.nanoTime();
		last = start;
		this.each = each;
		this.objName = objName;
		this.outMethod = outMethod;
	}

	/**
	 * Create from list
	 *
	 * @param list
	 * @param each
	 * @return
	 */
	public AisProgressLogger(List list, int each = 100, Closure outMethod = { log.info(it) }){
		this(list.size(), each, list.get(0).class.simpleName, outMethod);
	}

	/**
	 * Format nanoseconds to elapsed time format
	 * Prototype got from: http://stackoverflow.com/questions/567659/calculate-elapsed-time-in-java-groovy
	 *
	 * @param time difference in nanoseconds
	 * @return Human readable string representation - eg. 2 days, 14 hours, 5 minutes
	 */
	public static String formatTimeElapsedSinceNanosecond(long nanosDiff) {

		if(nanosDiff / (10 ** 6) < 100){ return sprintf("%.3fs", nanosDiff / (10 ** 9) );}

		String formattedTime = "";
		long secondInNanos = 10 ** 9;
		long minuteInNanos = secondInNanos * 60;
		long hourInNanos = minuteInNanos * 60;
		long dayInNanos = hourInNanos * 24;
		long weekInNanos = dayInNanos * 7;
		long monthInNanos = dayInNanos * 30;

		def timeElapsed = [0, 0, 0, 0, 0, 0];
		// Define time units - plural cases are handled inside loop
//		String timeElapsedText[] = {"s", "m", "h", "day", "week", "month"};
		def timeElapsedText = ["sec", "min", "hour", "day", "week", "month"];

		timeElapsed[5] = (int) (nanosDiff / monthInNanos);	// months
		nanosDiff = nanosDiff % monthInNanos;
		timeElapsed[4] = (int) (nanosDiff / weekInNanos);	// weeks
		nanosDiff = nanosDiff % weekInNanos;
		timeElapsed[3] = (int) (nanosDiff / dayInNanos);		// days
		nanosDiff = nanosDiff % dayInNanos;
		timeElapsed[2] = (int) (nanosDiff / hourInNanos);	// hours
		nanosDiff = nanosDiff % hourInNanos;
		timeElapsed[1] = (int) (nanosDiff / minuteInNanos);	// minutes
		nanosDiff = nanosDiff % minuteInNanos;
		timeElapsed[0] = sprintf("%.3f", nanosDiff / secondInNanos);		// seconds

		// Only adds 3 significant high valued units
		int i = (timeElapsed.size()-1);
		for(int j=0; i>=0 && j<3; i--){
			// loop from high to low time unit
			if(timeElapsed[i] != 0){
				formattedTime += ((j>0)? ", " :"") \
					+ timeElapsed[i] \
					+ " " + timeElapsedText[i] \
					+ ( (timeElapsed[i] != 1)? "s" : "" );
				++j;
			}
		} // end for - build string

		return formattedTime;
	} // end of formatTimeElapsedSinceNanosecond utility method

	/**
	 * Main method to log message
	 *
	 * @param current
	 */
	public void logProgress(long current){
		long spent = System.nanoTime() - last;
		long spentFromStart = System.nanoTime() - start + 1000000000000;
		int leaved = total - current;

		if ( ! (current % each) ){
		outMethod(sprintf(
			'Process %s #%d from %d (%.2f%%). Spent (pack %d elements) time: %.4fs (from start: %s)%s'
			,objName
			,current
			,total
			,current/total * 100	// %
			,each
			,spent / (10 ** 9)		// spent time
			,formatTimeElapsedSinceNanosecond(spentFromStart) // spent from start
			, ( (leaved && current != 0) ? sprintf(', Estimated items: %d, time: %s', leaved, formatTimeElapsedSinceNanosecond( (spentFromStart/(current)*(total - current)).toLong() ) ) : '' )
		));
		}
	}

	public void next(){
		logProgress(current++);
	}
}

AisProgressLogger pl = new AisProgressLogger(30, 5, 'Test1', {println(it)});
 (0..30).each{
     pl.next()
 }

pl = new AisProgressLogger(100, 10, 'TTT2', {println(it)});
 (0..100).each{
     pl.next()
 }

List l = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ].toList();
pl = new AisProgressLogger(l, 3, {println(it)});
for (Object ll in l){
	pl.next();
}