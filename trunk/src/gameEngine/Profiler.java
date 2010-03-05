package gameEngine;

public class Profiler {

	static long startTime = 0;
	static long endTime = 0;
	static int numberOfRuns = 0;
	static float totalRuntime = 0;
	static boolean profilerActive = false;
	
	/**
	 * JJ> Begins monitoring the processing time something uses. Remember to call Profiler.end()
	 *     when the computation is complete
	 */
	public static void begin() {
		
		//Already running a profiler
		if( profilerActive ) return;
		
		//Begin profiler
		startTime = System.currentTimeMillis();
		numberOfRuns++;
		
		//Tell them we have started
		Log.print("PROFILER: Beginning profiling.");
		System.out.println("PROFILER: Beginning profiling.");
	}

	/**
	 * JJ> Ends the profiling and writes out the results.
	 */
	public static void end() {
		
		//The profiler is not running!
		if( !profilerActive ) return;
		
		//Calculate runtime
		endTime = System.currentTimeMillis()-startTime;
		
		//Keep track of total runtime
		totalRuntime += endTime;
		
		//Spit out message to tell them the results
		String output = "PROFILER: Used " + endTime + " milliseconds to generate background. (Average: " + totalRuntime/numberOfRuns + ")";
		Log.print(output);		
		System.out.println(output);
	}

}
