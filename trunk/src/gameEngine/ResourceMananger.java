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
package gameEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * JJ> A static class for handling the loading of any files when exported as a JAR.
 * @author Johan Jansen and Anders Eie
 */
public class ResourceMananger {

	/**
	 * JJ> Finds the specified file and returns the absolute URL path to load it
	 * @note If you want to find the path of a folder and not a file, you need to end the path with a "/"
	 * @param path The String that contains the file name and abstract path (example: "/data/image.jpg")
	 * @return The URL containing the full absolute path to the file or null if it was not found.
	 */
	public static URL getFilePath(String path) {
		
		//Make sure the path is absolute
		if( !path.startsWith("/") ) path = "/" + path;
		
		return ResourceMananger.class.getResource(path);
	}
		
	/**
	 * JJ> Returns true if the specified file or directory was found, false otherwise.
	 * @note If you want to find the path of a folder and not a file, you need to end the path with a "/"
	 * @param path The String to the file or directory to check if exists
	 * @return true if found, false if not found.
	 */
	public static boolean fileExists(String path) {
		
		//Make sure the path is absolute
		if( !path.startsWith("/") ) path = "/" + path;

		//Try to find the file
		URL url = ResourceMananger.class.getResource(path);
		if(url == null) return false;
		
		//Yep, it's there all right
		return true;
	}
	
	/**
	 * JJ> This function gets a list of all the files in the specified folder
	 * @note If you want to find the path of a folder and not a file, you need to end the path with a "/"
	 * @param path The String to the directory to check.
	 * @return An array of every File found in the directory.
	 * @throws IOException 
	 */
	public static String[] getFileList(String path) {			
		ArrayList<String> list = new ArrayList<String>();
		
		//Here we actually trim away the slash if needed
		if(path.startsWith("/")) path = path.substring(1);
		
		try 
		{
			//Load files from the JAR file
			JarInputStream jarFile = getJarFile();
			if(jarFile != null)
			{
				JarEntry file;
				
				while ((file = jarFile.getNextJarEntry()) != null)
				{
					if( file.isDirectory() ) continue;
					String fileName = file.getName();
					if( !fileName.startsWith(path) ) continue;
					list.add("/" + fileName);
				}
			}
			
			//That did not work, let's try to load them as local files instead...
			else
			{
				//Local directories use a different path
				File dir = new File("src/" + path);
				if(!dir.exists()) throw new Exception("Directory was not found: src/" + path);
				
				File[] fileList = dir.listFiles(); 
				for(File f : fileList)
				{
					if( !f.isFile() ) continue;
					list.add( "/" + path + f.getName() );
				}
			}
		}
		catch (Exception e) 
		{
			Log.warning("Could not get file list (" + path + "): "+ e);
		}
		
		//Convert the list to a String array and return it
		String retval [] = new String[list.size()];
		return list.toArray(retval);
	}
	
	 /** JJ> Finds the specified file and opens an InputStream from it.
	 * @param path Which file to open.
	 * @return InputStream to the file specified in the parameter.
	 */
	public static InputStream getInputStream(String path){
		if( !path.startsWith("/") ) path = "/" + path;
		return ResourceMananger.class.getResourceAsStream(path);
	}
	
	/**
	 * JJ> This function gets the JarInputStream to the JAR file the game is run from.
	 * @return The .jar file this game is running or null if it is not run through a jar file.
	 */
	public static JarInputStream getJarFile(){
		//Are we actually running from a JAR file?
		if( getRunningJarName() == null ) return null;
		
		//Try to open the stream
		InputStream stream = getInputStream(getRunningJarName());
		if( stream == null ) return null;
		
		//Try to open a stream to our own JAR file
		try 
		{
			return new JarInputStream(stream);
		} 
		catch (IOException e) 
		{
			Log.warning("Could not locate JAR file: " + e);
		}
		
		//Failed, either incorrect JAR name or we are simply not inside a JAR file
		return null;
	}
	
	/**
	 * JJ> This function returns the name of the jar file we are running from.
	 * @return The String of the jar file we are running from. Returns null if we are not running
	 *         from a jar.
	 */
	public static String getRunningJarName() {
			String jarFile = System.getProperty("java.class.path");
			final String SLASH = System.getProperty("file.separator");
			
			//Not running from a jar file if there are multiple jar files in class path
			if( jarFile.indexOf(';') != -1 ) return null;
			if( !jarFile.endsWith(".jar") ) return null;
			
			//Trim everything until just the jar file remains
			jarFile = jarFile.substring( jarFile.lastIndexOf(SLASH)+1 );
			
			return jarFile;
		 }	

}
