package gameEngine;
//********************************************************************************************
//*
//*    This file is part of Project Narwhal.
//*
//*    Project Narwhal is free software: you can redistribute it and/or modify it
//*    under the terms of the GNU General Public License as published by
//*    the Free Software Foundation, either version 3 of the License, or
//*    (at your option) any later version.
//*
//*    Project Narwhal is distributed in the hope that it will be useful, but
//*    WITHOUT ANY WARRANTY; without even the implied warranty of
//*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//*    General Public License for more details.
//*
//*    You should have received a copy of the GNU General Public License
//*    along with Project Narwhal.  If not, see <http://www.gnu.org/licenses/>.
//*
//********************************************************************************************

import java.util.HashMap;
import java.util.Map;

/**
 * JJ> This class allows us to closely and exactly profile how much time the computer uses on certain
 *     calculations and computations. Simply call Prorfiler.start(String name) when the function begins 
 *     and Profiler.end(String name) when it is done, where 'name' is a unique String to identify this
 *     specific profile.
 * @author Johan Jansen og Anders Eie
 *
 */
public class Profiler {

	static Map<String, Profiler> profileList = new HashMap<String, Profiler>();
	
	/**
	 * JJ> Begins monitoring the processing time something uses. Remember to call Profiler.end()
	 *     when the computation is complete. Measures in milliseconds
	 *     @note  if you have already run a nanosecond profiler with the same String identifier,
	 *            all previous profile data will be lost!
	 */
	public static void begin( String profileName ) {
		if( !profileList.containsKey( profileName ) ) profileList.put( profileName,  new Profiler( profileName ) );		
		profileList.get(profileName).start(false);
	}

	/**
	 * JJ> Begins monitoring the processing time something uses. Remember to call Profiler.end()
	 *     when the computation is complete. Measures in nanoseconds.
	 *     @note  if you have already run a millisecond profiler with the same String identifier,
	 *            all previous profile data will be lost!
	 */
	public static void beginNano( String profileName ) {
		if( !profileList.containsKey( profileName ) ) profileList.put( profileName,  new Profiler( profileName ) );		
		profileList.get(profileName).start(false);
	}

	/**
	 * JJ> Ends the profiling and writes out the results.
	 */
	public static void end( String profileName ) {
		if( !profileList.containsKey( profileName ) ) return;		
		profileList.get(profileName).stop();
		
	}

	/**
	 * JJ> Resets a specific profile
	 */
	public static void reset( String profileName ) {
		if( !profileList.containsKey( profileName ) ) return;		
		profileList.get(profileName).reset();
	}
	
	/*
	 * JJ> The instanced version of this class can only be accessed through the static functions above
	 */
	private long startTime, endTime;
	private int numberOfRuns;
	private float totalRuntime;
	private boolean profilerActive, useNano;
	private String profileName;
	
	private Profiler(String newName)
	{
		reset();
		profileName = newName;
	}
	
	//Reset all profiling data
	private void reset()
	{
		startTime = 0;
		endTime = 0;
		numberOfRuns = 0;
		totalRuntime = 0;
		profilerActive = false;
		useNano = false;		
	}
	
	//Start profiling
	private void start(boolean nanoTimer) {
		
		//Already running a profiler
		if( profilerActive ) return;
		profilerActive = true;
		
		//Data is invalid if we mix milliseconds and nanoseconds
		if( useNano != nanoTimer )
		{
			this.reset();
			useNano = nanoTimer;
		}
		
		//Begin profiler
		startTime = useNano ? System.nanoTime() : System.currentTimeMillis();
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
		endTime = ( useNano ? System.nanoTime() : System.currentTimeMillis() ) - startTime;
		
		//Keep track of total runtime
		totalRuntime += endTime;
		
		//Spit out message to tell them the results
		//We use system console directly to save time
		System.out.println("PROFILER: " + profileName + " - Used " + endTime + " milliseconds. (Average: " + totalRuntime/numberOfRuns + ")");
	}

}