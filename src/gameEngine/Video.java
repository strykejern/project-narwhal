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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

/**
 * JJ> A static helper Video class for handling graphical stuff.
 * @author Johan Jansen and Anders Eie
 *
 */
public class Video {
	private static Dimension resolution = new Dimension();
	private static boolean highQuality = false;							//Draw everything in HQ gfx?
	private static RenderingHints quality = new RenderingHints(null);
	
	// Create a new blank cursor.
	public static final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
			new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

	//Hardware graphic stuff
	private static GraphicsEnvironment graphEnv;
	private static GraphicsDevice graphDevice;
	private static GraphicsConfiguration graphicConf;
		
	/**
	 * JJ> Acquiring the current Graphics Device and Graphics Configuration
	 * 	   This ensures us proper hardware acceleration
	 */
	public static void initialize() {

		graphEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		graphDevice = graphEnv.getDefaultScreenDevice();
		graphicConf = graphDevice.getDefaultConfiguration();
	}
	
	/**
	 * JJ> Used for constructing a VolatileImage
	 */
	public static VolatileImage createVolatileImage( int width, int height ) {
		return graphicConf.createCompatibleVolatileImage(width, height, VolatileImage.TRANSLUCENT );
	}

	/**
	 * JJ> Sets all graphics settings to nice
	 */
	public static void enableHighQualityGraphics() {
		highQuality = true;
		quality.clear();
		quality.add( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) );
		quality.add( new RenderingHints( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC ) );
		quality.add( new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY ) );
	   	quality.add( new RenderingHints( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE ) );
	   	quality.add( new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) );
	   	quality.add( new RenderingHints( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON ) );
	   	quality.add( new RenderingHints( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY ) );
	}
	
	/**
	 * JJ> Sets all graphics settings to ugly, but fast
	 */
	public static void disableHighQualityGraphics() {
		highQuality = false;
		quality.clear();
		quality.add( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF ) );
		quality.add( new RenderingHints( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR ) );
		quality.add( new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED ) );
	   	quality.add( new RenderingHints( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE ) );
	   	quality.add( new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF ) );
	   	quality.add( new RenderingHints( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF ) );
	   	quality.add( new RenderingHints( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED ) );
	}
		
	/**
	 * JJ> Modifies the Graphics2D to use the current video settings
	 */
	public static void getGraphicsSettings(Graphics2D g){
		g.addRenderingHints( quality );
	}
	
	/**
	 * JJ> Returns the resolution of the Operating System 
	 */
	public static Dimension getDesktopResolution() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
	
	public static GraphicsConfiguration getGraphicsConf() {
		return graphicConf;		
	}
	
	public static boolean isHighQualityMode() {
		return highQuality;
	}
	
	public static void setResolution(int width, int height) {
		resolution.setSize(width, height);
	}
	
	public static Dimension getResolution() {
		return  resolution;
	}
	
	public static Vector getResolutionVector(){
		return new Vector(resolution.width, resolution.height);
	}
	
	public static void setResolution(Dimension res) {
		resolution.setSize(res);
	}
	
	public static void setFullscreen() {
		resolution.setSize(Toolkit.getDefaultToolkit().getScreenSize());
	}
	
	public static int getScreenWidth()	{
		return resolution.width;
	}
	
	public static int getScreenHeight()	{
		return resolution.height;
	}
}
