//package imus.ais

import java.math.RoundingMode
//import org.jaxen.util.SelfAxisIterator
/**
 * Created by IntelliJ IDEA.
 * User: pasha
 * Date: 02.01.12
 * Time: 22:01
 * To change this template use File | Settings | File Templates.
 */

/**
 * Helper class logger of progress
 */
class ProgressLogger {

	public class Spent{
		public long spent;
		public String spentStr;
		
		public Spent(long spent){
			this.spent = spent;
			spentStr = formatTimeElapsedSinceNanosecond(spent);
		}
	}

	private long start;
	private long last;
	private long total;
	public int each;
	public String objName;
	private long current = 0;
	private Closure outMethod;

	public ProgressLogger(long total, Closure outMethod, int each = 100, String objName = 'object'){
		this.total = total;
		this.last = this.start = System.nanoTime();
		this.each = each;
		this.objName = objName;
		this.outMethod = outMethod;
	}

	/**
	 * Reset timers
	 */
	public void reset(){
		this.last = this.start = System.nanoTime();
	}

	/**
	 * Create from list
	 *
	 * @param list
	 * @param each
	 * @return
	 */
	public ProgressLogger(List list, Closure outMethod, int each = 100){
		this(list.size(), outMethod, each, list.size() ? list.get(0).class?.simpleName : 'empty list');
	}

	/**
	 * Static method to measure one closure execution.
	 *
	 * @param outMethod
	 * @param execute
	 * @param objName
	 * @param beginMessage
	 * @return 
	 */
	public static Spent measure(Closure outMethod, Closure execute, String objName = '', String beginMessage = null){
		if (beginMessage) outMethod(beginMessage);
		ProgressLogger pl = new ProgressLogger(1, outMethod, -1, '');
		if (objName) pl.objName = objName;
		execute();
		return pl.stop();
	}

	/**
	 * Main method to log message
	 *
	 * @param current
	 */
	public void logProgress(long current){
		long spent = System.nanoTime() - last;
		long spentFromStart = System.nanoTime() - start;
		int leaved = total - current;

		if ( ! (current % each) || current == total - 1 ){
			outMethod(sprintf(
					'Process %s #%d from %d (%.2f%%). Spent (pack %d elements) time: %.4fs (from start: %s)%s'
					,objName
					,current
					,total
					,current/total * 100	// %
					,each
					,spent / (10 ** 9)		// spent time
					,formatTimeElapsedSinceNanosecond(spentFromStart)	// spent from start
					, ( (leaved && current != 0) ? sprintf(', Estimated items: %d, time: %s', leaved, formatTimeElapsedSinceNanosecond( (spentFromStart/(current)*(total - current)).toLong() ) ) : '' )
			));
			last = System.nanoTime();
		}
	}

	/**
	 * For use just as time measure.
	 * Assumed only once call after object creation.
	 * @return Rerurns list with 2 elements: 0 - long nanoseconds, 1 - String - formated string of time.
	 */
	public Spent stop(){
		Spent spent = new Spent(System.nanoTime() - start);

		outMethod( (objName ? "Stop processing $objName. " : '') + 'Spent time: ' + spent.spentStr);
		spent;
	}

	/**
	 * Tick on each element
	 */
	public void next(){
		logProgress(current++);
	}

	/**
	 * Format nanoseconds to elapsed time format
	 * Protoype got from: http://stackoverflow.com/questions/567659/calculate-elapsed-time-in-java-groovy
	 *
	 * @param time difference in nanoseconds
	 * @return Human readable string representation - eg. 2 days, 14 hours, 5 minutes
	 */
	public static String formatTimeElapsedSinceNanosecond(long nanosDiff) {

		if(nanosDiff / (10 ** 6) < 100){ return sprintf("%.3fs", (nanosDiff / (10 ** 9)).setScale(3, RoundingMode.HALF_UP) );}

		String formattedTime = "";
		long secondInNanos = 10 ** 9;
		long minuteInNanos = secondInNanos * 60;
		long hourInNanos = minuteInNanos * 60;
		long dayInNanos = hourInNanos * 24;
		long weekInNanos = dayInNanos * 7;
		long monthInNanos = dayInNanos * 30;

		def timeElapsed = [0, 0, 0, 0, 0, 0];
		// Define time units - plural cases are handled inside loop
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
		timeElapsed[0] = sprintf("%.3f", (nanosDiff / secondInNanos).setScale(3, RoundingMode.HALF_UP));	// seconds

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
	}
}