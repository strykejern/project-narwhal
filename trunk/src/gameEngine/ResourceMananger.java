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
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class ResourceMananger {

	/**
	 * JJ> Finds the specified file and returns the absolute URL path to load it
	 * @param path The String that contains the file name and abstract path (example: "/data/image.jpg")
	 * @return The URL containing the full absolute path to the file or null if it was not found.
	 */
	public static URL getFilePath(String path) {
		
		//Make sure the path is absolute
		if( !path.startsWith("/") ) path = "/" + path;
		
		return ResourceMananger.class.getResource(path);
	}
	
	/**
	 * JJ> Finds the specified file and returns the absolute URI path to load it. 
	 *     URI is useful when loading files
	 * @param path The String that contains the file name and abstract path (example: "/data/image.jpg")
	 * @return The URI containing the full absolute path to the file or null if it was not found.
	 */
	public static URI getFileURI(String path) {
		
		//Make sure the path is absolute
		if( path.charAt(0) != '/' ) path = "/" + path;
		
		//Try to convert the full path name into a URI for file reading
		try 
		{
			return ResourceMananger.class.getResource(path).toURI();
		} 
		catch (Exception e) 
		{
			Log.warning(e);
			return null;
		}
	}
	
	/**
	 * JJ> Returns true if the specified file or directory was found, false otherwise.
	 * @param path The String to the file or directory to check if exists
	 * @return true if found, false if not found.
	 */
	public static boolean fileExists(String path) {
		
		//Make sure the path is absolute
		if( path.charAt(0) != '/' ) path = "/" + path;

		//Try to find the file
		URL url = ResourceMananger.class.getResource(path);
		if(url == null) return false;
		
		//Yep, it's there all right
		return true;
	}
	
	/**
	 * JJ> This function gets a list of all the files in the specified path
	 * @param path The String to the directory to check.
	 * @return An array of every File found in the directory.
	 */
	public static File[] getFileList(String path) {	
		
		return new File( getFileURI(path) ).listFiles();
	}
	
	 /** JJ> Finds the specified file and opens an InputStream from it.
	 * @param path Which file to open.
	 * @return InputStream to the file specified in the parameter.
	 */
	public static InputStream getInputStream(String path){
		if( !path.startsWith("/") ) path = "/" + path;
		return ResourceMananger.class.getResourceAsStream(path);		
	}

}
