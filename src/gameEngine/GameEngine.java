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

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import javax.swing.JFrame;

/**
 * JJ> A static helper Video class for handling graphical stuff.
 * @author Johan Jansen and Anders Eie
 *
 */
public abstract class GameEngine {
	private static JFrame window;
	private static Camera viewPort = null;
	private static ParticleEngine particleEngine = null;
	public static Configuration config;

	// Create a new blank cursor.
	public static final Cursor BLANK_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(
			new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

	//Hardware graphic stuff
	private static GraphicsEnvironment 	 graphEnv;
	private static GraphicsDevice 		 graphDevice;
	private static GraphicsConfiguration graphicConf;
			
	/**
	 * JJ> The main game function, here is where everything starts
	 * @param args
	 */
	public static void main(String[] args) {
    	//Initialize the logging system, do this first so that error logging happens correctly.
    	Log.initialize();
    	    	
    	//Load settings
    	config = new Configuration("config.ini");
    	
		//Prepare particle engine
		particleEngine = new ParticleEngine();     
    		
    	//Acquiring the current Graphics Device and Graphics Configuration
        //This ensures us proper hardware acceleration
 		graphEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		graphDevice = graphEnv.getDefaultScreenDevice();
		graphicConf = graphDevice.getDefaultConfiguration();

		//Force proper graphics acceleration
		if(System.getProperty("os.name").contains("Linux") )
		{
			//Linux OpenGL acceleration
			//TODO
		}
		else
		{
			//Windows DirectDraw acceleration
			System.setProperty("sun.java2d.translaccel", "true");
			System.setProperty("sun.java2d.ddforcevram", "true");
		}   
		
		//Initialize the frame window where we draw stuff
    	GameEngine.startNewGame("Project Narwhal");		
  	}
	
	/**
	 * JJ> Modifies the Graphics2D to use the current video settings
	 */
	public static void getGraphicsSettings(Graphics2D g){
		config.getGraphicsSettings(g);
	}
	
	/**
	 * JJ> Used for constructing a VolatileImage
	 */
	public static VolatileImage createVolatileImage( int width, int height ) {
		return graphicConf.createCompatibleVolatileImage(width, height, VolatileImage.TRANSLUCENT );
	}

	/**
	 * JJ> Used for constructing a BufferedImage
	 */
	public static BufferedImage createBufferedImage( int width, int height ) {
		return graphicConf.createCompatibleImage( width, height, VolatileImage.TRANSLUCENT );
	}

	public static Vector getResolutionVector(){
		return new Vector(window.getWidth(), window.getHeight());
	}
	
	public static int getScreenWidth()	{
		return window.getWidth();
	}
	
	public static int getScreenHeight()	{
		return window.getHeight();
	}
	
	public static void startNewGame(String name) {
		if( window != null ) window.dispose();
		window = new JFrame(name, graphicConf);

		//Set fullscreen?
		if( config.fullScreen )
		{
			window.setSize(Toolkit.getDefaultToolkit().getScreenSize());
			window.setUndecorated(true);
		}
		else window.setSize(800, 600);
		
    	window.getContentPane().add(new GameWindow(window));
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible( true );
		window.setIgnoreRepaint( true );								//This ensures there is no flickering       	
	}
	
	public static void setCamera(Camera setViewPort) {
		viewPort = setViewPort;
	}
	
	public static Vector getCameraPos() {
		return viewPort.getCameraPos();
	}

	public static boolean isInFrame(Vector pos, Vector tolerance) {
		return viewPort.isInFrame(pos, tolerance);
	}
	
	public static boolean isInFrame(GameObject object) {
		return viewPort.isInFrame(object);
	}

	public static ParticleEngine getParticleEngine() {
		return particleEngine;
	}

	/**
	 * JJ> Free resources, save data and exit properly
	 * @param code The exit code used for terminating this process (0 for normal exit)
	 */
	public static void exitGame(int code) {
		config.exportSettings();
		Log.message("Exiting the game the good way. Exit code: " + code);
	   	Log.close();		
	   	System.exit(code);
	}
	
	public static Configuration getConfig() {
		return config;
	}
}
