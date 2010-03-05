package gameEngine;

import java.util.HashMap;
import java.util.Map;

public class Profiler {

	static Map<String, Profiler> profileList = new HashMap<String, Profiler>();
	
	/**
	 * JJ> Begins monitoring the processing time something uses. Remember to call Profiler.end()
	 *     when the computation is complete
	 */
	public static void begin( String profileName ) {
		if( !profileList.containsKey( profileName ) ) profileList.put( profileName,  new Profiler( profileName ) );		
		profileList.get(profileName).start();
	}

	/**
	 * JJ> Ends the profiling and writes out the results.
	 */
	public static void end( String profileName ) {
		if( !profileList.containsKey( profileName ) ) return;		
		profileList.get(profileName).stop();
		
	}

	
	/*
	 * JJ> The instanced version of this class can only be accessed through the static functions above
	 */
	private long startTime, endTime;
	private int numberOfRuns;
	private float totalRuntime;
	private boolean profilerActive;
	private String profileName;
	
	private Profiler(String newName)
	{
		startTime = 0;
		endTime = 0;
		numberOfRuns = 0;
		totalRuntime = 0;
		profilerActive = false;
		profileName = newName;		
	}
	
	//Start counting
	private void start()
	{
		//Already running a profiler
		if( profilerActive ) return;
		profilerActive = true;

		//Begin profiler
		startTime = System.currentTimeMillis();
		numberOfRuns++;
		
		//Tell them we have started
		//We use system console directly to save time
		System.out.println("PROFILER: " + profileName + " - Beginning profiling.");
	}

	//Stop counting and spit out feedback
	private void stop() {
		
		//The profiler is not running!
		if( !profilerActive ) return;
		profilerActive = false;
		
		//Calculate runtime
		endTime = System.currentTimeMillis()-startTime;
		
		//Keep track of total runtime
		totalRuntime += endTime;
		
		//Spit out message to tell them the results
		//We use system console directly to save time
		System.out.println("PROFILER: " + profileName + " - Used " + endTime + " milliseconds. (Average: " + totalRuntime/numberOfRuns + ")");
	}

}
