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

import gameEngine.Video.VideoQuality;

import java.awt.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JFrame;
import javax.swing.JPanel;

import narwhal.Game;
import narwhal.MainMenu;
import narwhal.Shipyard;


/**
 * JJ> Main game class, here the important top stuff happens
 * @author Johan Jansen and Anders Eie
 *
 */
public class GameWindow extends JPanel implements Runnable, KeyListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private static final int TARGET_FPS = 1000 / 60;		//60 times per second
	private JFrame frame;
	private Input keys;
	
	private Game theGame;
	private MainMenu theMenu;
	private gameState state;
	private Shipyard selectShip;
	
	public static enum gameState {
		GAME_MENU,
		GAME_PLAYING, 
		GAME_EXIT,
		GAME_SELECT_SHIP
	}
	
	/**
	 * JJ> The main game function, here is where everything starts
	 * @param args
	 */
	public static void main(String[] args) {
    	//Initialize the logging system, do this first so that error logging happens correctly.
    	Log.initialize();
    	    	
    	//Load settings
        Video.initialize();
    	loadSettings();
    	
		//Initialize the frame window where we draw stuff
    	JFrame parentWindow = new JFrame("Project Narwhal", Video.getGraphicsConf());		
    	
    	//Full screen
    	{
       // 	Video.setFullscreen();
    	//	parentWindow.setUndecorated(true);								//Remove borders
        } 	
   
    	parentWindow.getContentPane().add(new GameWindow(parentWindow));
    	parentWindow.setSize(Video.getResolution());
		parentWindow.setResizable(false);
        parentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentWindow.setVisible( true );
       	parentWindow.setIgnoreRepaint( true );								//This ensures there is no flickering       	
  	}
	
	public GameWindow(JFrame frame) {
    	
		//The actual frame
		this.frame = frame;
		Image2D icon = new Image2D("/data/icon.png");
		frame.setIconImage( icon.getSnapshot() );

		//Input controls
		frame.addKeyListener(this);
		frame.addMouseListener(this);
		keys = new Input();	
		
       	//Start in the main menu
       	state = gameState.GAME_MENU;
       	theMenu = new MainMenu(keys);
		
		//Thread (do last so that everything above is properly loaded before the main loop begins)
       	Thread mainLoop = new Thread(this);
       	mainLoop.setPriority(Thread.MAX_PRIORITY);
       	mainLoop.start();
	}
	
	//JJ> This is the main game loop
	public void run() {
		
		// Remember the starting time
    	long tm = System.currentTimeMillis();
    			 
		while( state != gameState.GAME_EXIT )
    	{
			//Update mouse position within the frame
			keys.update(frame.getMousePosition());
			
			//Start a new game if needed
			if( theGame == null && state == gameState.GAME_PLAYING)
			{
		       	theGame = new Game(keys, 5, selectShip);
			}

			try
			{
				while (painting) Thread.sleep(0, 5000);
			}
			catch (Exception e) { Log.warning(e); }
			
			if(state == gameState.GAME_PLAYING)
			{
				state = theGame.update();
				frame.getContentPane().setCursor(Video.BLANK_CURSOR);	//TODO: bad change cursor every frame?
			}
			else if(state == gameState.GAME_MENU)
			{
		    	frame.getContentPane().setCursor( null );				//TODO: bad change cursor every frame?
				state = theMenu.update(theGame == null);
			}
			else if(state == gameState.GAME_SELECT_SHIP)
			{
				if(selectShip == null) selectShip = new Shipyard(keys);
				
		    	frame.getContentPane().setCursor( null );				//TODO: bad change cursor every frame?
		    	state = selectShip.update();
			}
			repaint();
			
			try 
			{
				tm += TARGET_FPS;
				Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
			}
			catch (Exception e) { Log.warning(e); }
		}
		
		exit(0);
	}
	
	/**
	 * JJ> Load configuration settings
	 */
	private static void loadSettings() {
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
						if( line.endsWith("HIGH") ) Video.setVideoQuality(VideoQuality.VIDEO_HIGH);
						else if( line.endsWith("LOW") ) Video.setVideoQuality(VideoQuality.VIDEO_LOW);
						else Video.setVideoQuality(VideoQuality.VIDEO_NORMAL);
					}
					
					//Sound enabled
					else if(line.startsWith("[SOUND]:"))
					{
						if( line.endsWith("OFF") ) Sound.enabled = false;
						else Sound.enabled = true;
					}

					//Music enabled
					else if(line.startsWith("[MUSIC]:"))
					{
						if( line.endsWith("OFF") ) Music.enabled = false;
						else Music.enabled = true;
					}

					//Full screen
					else if(line.startsWith("[FULL_SCREEN]:"))
					{
						if( line.endsWith("TRUE") ) Video.setFullscreen();
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
		if(useDefault)
		{
			Log.message("Could not read configuration settings. Reverting to default settings.");
			Video.setVideoQuality(VideoQuality.VIDEO_NORMAL);
			Sound.enabled = true;
	        Video.setResolution(800, 600);
		}
	}
	
	/**
	 * JJ> Save current configuration settings
	 */
	private void exportSettings(){
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
				if( Video.getQualityMode() == VideoQuality.VIDEO_LOW ) save.write("LOW");
				else if( Video.getQualityMode() == VideoQuality.VIDEO_HIGH ) save.write("HIGH");
				else save.write("NORMAL");
				save.newLine();
				
				//Sound enabled
				save.write( "[SOUND]: " );
				if( Sound.enabled ) save.write("ON");
				else save.write("OFF");
				save.newLine();

				//Sound enabled
				save.write( "[MUSIC]: " );
				if( Music.enabled ) save.write("ON");
				else save.write("OFF");
				save.newLine();

				//Full screen
				save.write("[FULL_SCREEN]: ");
				if (Video.getDesktopResolution() == Video.getResolution())
					save.write("TRUE");
				else save.write("FALSE");
				save.newLine();
					
				//Resolution (overrides full screen
				save.write("[SCREEN_WIDTH]: ");
				save.write( "" + Video.getScreenWidth() );
				save.newLine();
				save.write("[SCREEN_HEIGHT]: ");
				save.write( "" + Video.getScreenHeight() );
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
	 * JJ> Free resources, save data and exit properly
	 * @param code The exit code used for terminating this process (0 for normal exit)
	 */
	private void exit(int code) {
		exportSettings();
		Log.message("Exiting the game the good way. Exit code: " + code);
	   	Log.close();		
	   	System.exit(code);
	}
	
	/**
	 * JJ> Paints every object of interest
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics rawGraphics) {
		painting = true;
		
		//Convert to the Graphics2D object which allows us more functions
		Graphics2D g = (Graphics2D) rawGraphics;
				
		//Set quality mode
		Video.getGraphicsSettings(g);
				
		if(state == gameState.GAME_PLAYING && theGame != null) theGame.draw(g);
		else if(state == gameState.GAME_MENU) theMenu.draw(g);
		else if(state == gameState.GAME_SELECT_SHIP && selectShip != null) selectShip.draw(g);

		//Done drawing this frame
		g.dispose();
		painting = false;
	}
	private boolean painting = false;
	
	//Functions handling input update
	public void keyPressed(KeyEvent key) {
		keys.update(key, true);
	}

	public void keyReleased(KeyEvent key) {
		keys.update(key, false);
	}

	public void keyTyped(KeyEvent key) {	
	}

	public void mouseReleased(MouseEvent mouse) {
		keys.update(mouse, false);
	}
	
	public void mousePressed(MouseEvent mouse) {
		keys.update(mouse, true);
	}

	public void mouseClicked(MouseEvent mouse) 	{}
	public void mouseEntered(MouseEvent mouse) 	{}
	public void mouseExited	(MouseEvent e) 		{}
}
