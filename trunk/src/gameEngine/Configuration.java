package gameEngine;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Configuration {
	public boolean debugMode = false;
	public boolean fullScreen = false;
	private VideoQuality videoQuality;
	private RenderingHints quality = new RenderingHints(null);
	
	public static enum VideoQuality {
		VIDEO_LOW,
		VIDEO_NORMAL,
		VIDEO_HIGH
	}

	/**
	 * JJ> Sets the graphics quality for all rendering processes.
	 *     Can be either VIDEO_LOW, VIDEO_NORMAL or VIDEO_HIGH
	 */
	public void setVideoQuality( VideoQuality setQuality) {
		videoQuality = setQuality;
		
		//Clear any previous settings
		quality.clear();
		
		//Add the new graphic rendering hints
		switch( setQuality )
		{
			case VIDEO_LOW:
			{
				quality.add( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF ) );
				quality.add( new RenderingHints( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED ) );
				quality.add( new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED ) );
			   	break;
			}
			
			case VIDEO_HIGH:
			{
				quality.add( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) );
				quality.add( new RenderingHints( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR ) );		//Bicubic filtering is waay to costly to be useful!
			   	quality.add( new RenderingHints( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY ) );
				quality.add( new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY ) );
			   	break;
			}
			
			default: case VIDEO_NORMAL:
			{
				quality.add( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT ) );
				quality.add( new RenderingHints( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT ) );
				quality.add( new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT ) );
				break;
			}
		}
	}
			
	/**
	 * JJ> Modifies the Graphics2D to use the current video settings
	 */
	public void getGraphicsSettings(Graphics2D g){
		g.addRenderingHints( quality );
	}
	
	public VideoQuality getQualityMode() {
		return videoQuality;
	}
	
	/**
	 * JJ> Save current configuration settings
	 */
	public void exportSettings(){
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
				if( getQualityMode() == VideoQuality.VIDEO_LOW ) 			save.write("LOW");
				else if( getQualityMode() == VideoQuality.VIDEO_HIGH ) 	save.write("HIGH");
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
				if ( fullScreen ) 		save.write("TRUE");
				else 			  		save.write("FALSE");
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
	public Configuration(String fileName) {
		File conf = new File(fileName);
		boolean useDefault = false;
		
		//See if a configuration file exists
		if( conf.exists() )
		{
			try 
			{
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
						if( line.endsWith("HIGH") ) 	setVideoQuality(VideoQuality.VIDEO_HIGH);
						else if( line.endsWith("LOW") ) setVideoQuality(VideoQuality.VIDEO_LOW);
						else 							setVideoQuality(VideoQuality.VIDEO_NORMAL);
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
						if( line.endsWith("TRUE") ) fullScreen = true;
					}
				}
								
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
			setVideoQuality( VideoQuality.VIDEO_NORMAL );
			Sound.enabled = true;
			fullScreen = false;
		}
	}
}
