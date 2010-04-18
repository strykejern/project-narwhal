package gameEngine;

import gameEngine.Video.VideoQuality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Configuration {
	public static boolean debugMode = false;
	
	/**
	 * JJ> Save current configuration settings
	 */
	public static void exportSettings(){
		File conf = new File("config.ini");
		
		//Delete any existing file
		conf.delete();
		if( !conf.exists() )
		{
			try 
			{
				conf.createNewFile();
				BufferedWriter save = new BufferedWriter( new FileWriter(conf) );
				
				//Graphic quality
				save.write( "[GRAPHICS]: " );
				if( Video.getQualityMode() == VideoQuality.VIDEO_LOW ) 			save.write("LOW");
				else if( Video.getQualityMode() == VideoQuality.VIDEO_HIGH ) 	save.write("HIGH");
				else 															save.write("NORMAL");
				save.newLine();
				
				//Sound enabled
				save.write( "[SOUND]: " );
				if( Sound.enabled ) save.write("ON");
				else 				save.write("OFF");
				save.newLine();

				//Sound enabled
				save.write( "[MUSIC]: " );
				if( Music.musicEnabled ) save.write("ON");
				else 				save.write("OFF");
				save.newLine();

				//Full screen
				save.write("[FULL_SCREEN]: ");
				if ( Video.fullScreen ) save.write("TRUE");
				else 			  		save.write("FALSE");
				save.newLine();
					
				//Resolution (overrides full screen
				save.write( "[SCREEN_WIDTH]: " + Video.getScreenWidth() );
				save.newLine();
				save.write( "[SCREEN_HEIGHT]: " + Video.getScreenHeight() );
				save.newLine();
				
				save.close();
				Log.message("Configuration settings saved: " + conf.getAbsolutePath() );
			} 
			
			//Something went wrong
			catch (Exception e) 
			{
				Log.warning("Could not save settings: " + e);
			}
		}
		else Log.warning("Could not delete old config file.");
	}

	/**
	 * JJ> Load configuration settings
	 */
	public static void loadSettings() {
		File conf = new File("config.ini");
		boolean useDefault = false;
		
		//See if a configuration file exists
		if( conf.exists() )
		{
			try 
			{
				int resX = 800, resY = 600;
				BufferedReader parse = new BufferedReader( new FileReader(conf) );
				
				//Parse the config file
				while(true)
				{
					String line = parse.readLine();
					
					//Reached end of file
					if(line == null) break;
					
					//Graphic quality
					if(line.startsWith("[GRAPHICS]:"))
					{
						if( line.endsWith("HIGH") ) 	Video.setVideoQuality(VideoQuality.VIDEO_HIGH);
						else if( line.endsWith("LOW") ) Video.setVideoQuality(VideoQuality.VIDEO_LOW);
						else 							Video.setVideoQuality(VideoQuality.VIDEO_NORMAL);
					}
					
					//Sound enabled
					else if(line.startsWith("[SOUND]:"))
					{
						if( line.endsWith("OFF") ) 	Sound.enabled = false;
						else 						Sound.enabled = true;
					}

					//Music enabled
					else if(line.startsWith("[MUSIC]:"))
					{
						if( line.endsWith("OFF") ) 	Music.musicEnabled = false;
						else 						Music.musicEnabled = true;
					}

					//Full screen
					else if(line.startsWith("[FULL_SCREEN]:"))
					{
						if( line.endsWith("TRUE") ) Video.fullScreen = true;
					}
					
					//Resolution (overrides full screen
					else if(line.startsWith("[SCREEN_WIDTH]:"))
					{
						line = line.substring(line.indexOf(":")+1);
						resX = Integer.parseInt(line.trim());
					}
					else if(line.startsWith("[SCREEN_HEIGHT]:"))
					{
						line = line.substring(line.indexOf(":")+1);
						resY = Integer.parseInt(line.trim());
					}
				}
				
				//Set screen dimensions
				Video.setResolution(resX, resY);
				
				//Close file
				Log.message("Configuration file successfully parsed.");
				parse.close();
			} 
			
			//Something went wrong, revert to default settings
			catch (Exception e) 
			{
				Log.warning(e);
				useDefault = true;
			}

		}
		else useDefault = true;
		
		//Use default settings
		if( useDefault )
		{
			Log.message("Could not read configuration settings. Reverting to default settings.");
			Video.setVideoQuality( VideoQuality.VIDEO_NORMAL );
			Sound.enabled = true;
	        Video.setResolution(800, 600);
		}
	}
}
