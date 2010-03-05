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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class Log 
{
	static BufferedWriter logFile = null;
    
	//JJ> Initialize the logging system
	public static boolean initialize() {
	     try 
	     {	            
	          //Construct the BufferedWriter object
	          logFile = new BufferedWriter(new FileWriter("game.log"));
	          
	          //Start writing to the output stream
	          message("System date is: " +  getDate());
	          message("Initializing logging system... Success!");
	          return true;
	      } 
	      catch (FileNotFoundException ex) 
	      {
	          ex.printStackTrace();
	      }   
	      catch (IOException ex) 
	      {
	          ex.printStackTrace();
	      } 
	        
	   return false;
	}
		
	
	//JJ> Writes the specified text to the log file and adds a new line feed	
	public static void message(String rawText) {
		//logFile.newLine();		//@TODO: JJ> should use this function instead of \n
	 	//    							since new line feeds are system dependent
		String text = "INFO: " + rawText + "\n"; 
		System.out.print(text);
		print(text);
	}
	public static void warning(String rawText) {
		String text = "WARNING: " + rawText + "\n"; 
		System.err.print(text);
		print(text);
	}
	public static void error(String rawText) {
		String text = "ERROR: " + rawText + "\n"; 
		System.err.print(text);
		print(text);
	}
	
	//JJ> Writes the specified text to the log file
	public static void print(String text) {
		if( logFile == null ) return;
		
		String message = getTime() + " " + text;
	    try  
	    {	 
	        //Writing happens live, we want the log to be up to date if the 
	        //program crashes for some reason
	        logFile.write(message);
	        logFile.flush();	        
	     } 	       
	     catch (FileNotFoundException ex) {
	    	 ex.printStackTrace();
	     } 	        
	     catch (IOException ex) {
	    	 ex.printStackTrace();
	     } 
	}
	
	//JJ> Helper functions to quickly get time and date
    static private String getDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
    	return date.format( cal.getTime() );
    }
    static private String getTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss");
    	return date.format( cal.getTime() );
    }
	
	//JJ> Close log file
    public static void close() {
		if (logFile == null) return; 

		//Close the BufferedWriter
        try  
        {
	        message("Shutting down logging system...");
            logFile.close();
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
	}
}