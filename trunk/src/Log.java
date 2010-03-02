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

public class Log 
{
	static BufferedWriter logFile = null;
		
	//JJ> Initialize the logging system
	public static boolean initialize()
	{
	     try {	            
	          //Construct the BufferedWriter object
	          logFile = new BufferedWriter(new FileWriter("game.log"));
	          
	          //Start writing to the output stream
	          println("Creating log file");    
	      } 
	       
	      catch (FileNotFoundException ex) {
	            ex.printStackTrace();
	      } 
	        
	        catch (IOException ex) {
	            ex.printStackTrace();
	        } 
	        
	   return false;
	}
	
	
	//JJ> Writes the specified text to the log file and adds a new line feed
	static void println(String text)
	{
		print(text + "\n");
		//logFile.newLine();		//JJ> should use this function instead of \n
								 	//    since new line feeds are system dependent
	}
	
	//JJ> Writes the specified text to the log file
	static void print(String text)
	{
	     try  {
	    	 
	        //Writing happens live, we want the log to be up to date if the 
	        //program crashes for some reason
	        logFile.write("Creating log file");
	        logFile.flush();
	        
	     } 	       
	     catch (FileNotFoundException ex) {
	    	 ex.printStackTrace();
	     } 	        
	     catch (IOException ex) {
	    	 ex.printStackTrace();
	     } 
	}
	
	
	//JJ> Close log file
	static void close()
	{
		if (logFile == null) return; 

		//Close the BufferedWriter
        try  {
        	logFile.flush();
            logFile.close();
        } 
        
        catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
}
