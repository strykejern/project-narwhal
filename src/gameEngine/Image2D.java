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


import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import javax.swing.ImageIcon;

import narwhal.Game;

/**
 * JJ> Helper class to make image handling easier to do
 * @todo Performance for this class can be vastly improved by implementing VolatileImage
 *       rather than BufferedImage.
 * @author Johan Jansen and Anders Eie
 */
public class Image2D {
	
	/*************************************************************************
	 * JJ> Static version of this class to handle HQ graphics mode
	 ************************************************************************/
	private static boolean highQuality = false;		//Draw everything in HQ gfx?
	
	public static void enableHighQualityGraphics() {
		highQuality = true;
	}
	public static void disableHighQualityGraphics() {
		highQuality = false;
	}
	public static boolean isHighQualityMode() {
		return highQuality;
	}
	
	public static void getGraphicsSettings(Graphics2D g){
		if( highQuality )
		{
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	    	g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
			g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		   	g.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE );
		}
		else
		{
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
			g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
			g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
		   	g.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE );
		}
	}


	/*************************************************************************
	 * JJ> The Private instanced version
	 ************************************************************************/
	private BufferedImage original;					//The image itself
	private VolatileImage processed;				//The image with effects added (rotation, alpha, etc.)
	private int width, height;
	private int baseWidth, baseHeight;

	private boolean flipHorizontal = false;
	private boolean flipVertical = false;
	private float currentAlpha = 1;
	private float currentAngle = 0;
	
	/**
	 * JJ> Constructor makes sure the image is correctly loaded
	 * @param fileName: the path and name of the file to load
	 */
	public Image2D( String fileName ) {
		
		ImageIcon load = new ImageIcon(fileName);
		
		//First make sure the file actually exists
		if( load.getIconWidth() < 0 )
		{
			Log.error( "Failed loading image: " + fileName );
		}

		//Load the image into a BufferedImage
		original = new BufferedImage( load.getIconWidth(), load.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = original.createGraphics();
        getGraphicsSettings(g);
        g.drawImage(load.getImage(), 0, 0, null ); 
        g.dispose();
        
		width = load.getIconWidth();
		height = load.getIconHeight();
	}

	/**
	 * JJ> Constructor makes sure the image is correctly loaded
	 * @param copyImg: which BufferedImage to use as the sprite
	 */
	public Image2D( BufferedImage copyImg ) {
		
		//Ensure it is the correct format before loading it
		BufferedImage buffer = new BufferedImage( copyImg.getWidth(), copyImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffer.createGraphics();
        getGraphicsSettings(g);
        g.drawImage(copyImg, 0, 0, null ); 
        g.dispose();
        
        original = buffer;
		width = buffer.getWidth();
		height = buffer.getHeight();
	}

		
	/**
	 * JJ> Makes sure we have a VolatileImage object to draw on
	 *     If it got lost, it creates a new one.
	 */
	private Graphics2D getVolatileMemory() {
		if( processed == null || processed.contentsLost() || processed.getWidth() != width || processed.getHeight() != height )
		{
			processed = Game.getGraphicsConf().createCompatibleVolatileImage(width, height, VolatileImage.TRANSLUCENT);
		}

        Graphics2D g = processed.createGraphics();
        getGraphicsSettings(g);

		//Clear the background
		g.setBackground(new Color(0,0,0,0));
		g.clearRect(0, 0, width, height);
		
		return g;
	}
	
	/**
	 * JJ> Rotates an image with the specified degrees
	 * @param angle: how much to rotate by
	 */
	public void rotate(float angle) {  
		currentAngle += angle;
	}

	/**
	 * JJ> Sets the image rotation to the specified degrees
	 * @param angle: the new direction
	 */
	public void setDirection(float angle) {  
		currentAngle = angle;
    }  
	
	/**
	 * JJ> direct scaling of a image using the resize method
	 */
	public void scale(float multiplier) {   
        resize( (int)(width * multiplier), (int)(height * multiplier) );	
	}
	
	/**
	 * JJ> resizing an image
	 */
	public void resize(int newW, int newH) {
	    
        //Valid resize?
        if( newW <= 0 || newH <= 0 ) return;
        
        baseWidth = newW;
        baseHeight = newH;
        width = (int)(baseWidth*1.20f);
        height = (int)(baseHeight*1.20f);         
    }
	
	/**
	 * JJ> Flips the image horizontally
	 */
	public void horizontalFlip() {
		flipHorizontal = !flipHorizontal;
	}
	
	/**
	 * JJ> Flips the image vertically
	 */
	public void verticalFlip() {  
		flipVertical = !flipVertical;
    } 
		
	/**
	 * JJ> Makes the image partially or fully transparent 
	 * @param transperancy: value should be between 0.00 (completely transparent) and 1.00 (normal)
     */
	public void setAlpha(float transperancy) { 
		//Clip the parameter to a valid value so that we do not get an error message
		currentAlpha = Math.min( 1.00f, Math.max(0.00f, transperancy) );        
    }  
		
	/**
	 * JJ> Returns this Image2D as a Image instance
	 * @return the image ready to be drawn with proper rotation and all
	 */
	public Image toImage(){
		Graphics2D g = getVolatileMemory();
		
		//To make life easier
		final int w = baseWidth;
		final int h = baseHeight;
		
        // Set the Graphics composite to Alpha
		if(currentAlpha < 1) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));  
        
		//Do rotation
		if(currentAngle != 0) g.rotate(currentAngle, width/2.0, height/2.0);
		
		//TODO: Do vertical and horizontal flips
		g.drawImage(
				original,						//Draw the base image 
				(width-w)/2, 					//X offset
				(height-h)/2, 					//Y offset
				w, 								//How much to draw
				h,
				null);

		//All done!
        g.dispose();
		return processed;
	}
	
	/**
	 * JJ> Get width for this buffered image
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * JJ> Get height for this buffered image
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * JJ> Returns the current angle for this image
	 */
	public float getAngle() {
		return currentAngle;
	}

	/**
	 * JJ> Returns the image to its original state when it was first loaded
	 * @warning: All changes are permanently lost!
	 */
	public void reset() {
		currentAlpha = 1;
		currentAngle = 0;
		flipHorizontal = flipVertical = false;
		resize( original.getWidth(), original.getHeight() );
	}
}
