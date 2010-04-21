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
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import javax.swing.JFrame;

/**
 * JJ> A static helper Video class for handling graphical stuff.
 * @author Johan Jansen and Anders Eie
 *
 */
public class Video {
	private static JFrame window;
	private static VideoQuality videoQuality = VideoQuality.VIDEO_NORMAL;
	private static RenderingHints quality = new RenderingHints(null);
	public  static boolean fullScreen = false;
	private static Camera viewPort = null;
	
	// Create a new blank cursor.
	public static final Cursor BLANK_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(
			new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

	//Hardware graphic stuff
	private static GraphicsEnvironment 	 graphEnv;
	private static GraphicsDevice 		 graphDevice;
	private static GraphicsConfiguration graphicConf;
		
	public static enum VideoQuality {
		VIDEO_LOW,
		VIDEO_NORMAL,
		VIDEO_HIGH
	}
	
	/**
	 * JJ> Acquiring the current Graphics Device and Graphics Configuration
	 * 	   This ensures us proper hardware acceleration
	 */
	public static void initialize() {

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

	/**
	 * JJ> Sets the graphics quality for all rendering processes.
	 *     Can be either VIDEO_LOW, VIDEO_NORMAL or VIDEO_HIGH
	 */
	public static void setVideoQuality( VideoQuality setQuality) {
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
			   	quality.add( new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT ) );
			   	quality.add( new RenderingHints( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT ) );
				break;
			}
		}

	}
			
	/**
	 * JJ> Modifies the Graphics2D to use the current video settings
	 */
	public static void getGraphicsSettings(Graphics2D g){
		g.addRenderingHints( quality );
	}
		
	public static GraphicsConfiguration getGraphicsConf() {
		return graphicConf;		
	}
	
	public static VideoQuality getQualityMode() {
		return videoQuality;
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
	
	public static JFrame createWindow(String name) {
		if( window != null ) window.dispose();
		window = new JFrame(name, graphicConf);

		//Set fullscreen?
		if( fullScreen )
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

		return window;
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
}
