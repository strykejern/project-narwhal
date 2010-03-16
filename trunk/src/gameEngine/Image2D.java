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
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.VolatileImage;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * JJ> Helper class to make image handling easier to do
 * @author Johan Jansen and Anders Eie
 */
public class Image2D {
	final private static Kernel blur = new Kernel(3, 3,
		    new float[] {
	        1f/9f, 1f/9f, 1f/9f,
	        1f/9f, 1f/9f, 1f/9f,
	        1f/9f, 1f/9f, 1f/9f});
	
	private BufferedImage original;					//The image itself
	private VolatileImage processed;				//The image with effects added (rotation, alpha, etc.)
	private int width, height;
	private int baseWidth, baseHeight;

	private boolean flipHorizontal = false;
	private boolean flipVertical = false;
	private boolean blurEffect = false;
	private boolean noChange = false;
	private float currentAlpha = 1;
	private float currentAngle = 0;
	private int   colorTint = 0xFFFFFFFF;
	
	/**
	 * JJ> Constructor makes sure the image is correctly loaded
	 * @param fileName: the path and name of the file to load
	 */
	public Image2D( String fileName ) {
		
		//First make sure the file actually exists
		if( !ResourceMananger.fileExists(fileName) )
		{
			Log.error( "Failed loading image: " + fileName );
		}
		ImageIcon load = new ImageIcon(ResourceMananger.getFilePath(fileName));

		//Load the image into a BufferedImage
		original = new BufferedImage( load.getIconWidth(), load.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = original.createGraphics();
        Video.getGraphicsSettings(g);
        g.drawImage(load.getImage(), 0, 0, null ); 
        g.dispose();
        
		baseWidth = load.getIconWidth();
		width = (int)(baseWidth*1.20f);
		baseHeight = load.getIconHeight();
		height = (int)(baseHeight*1.20f);
	}
	
	/**
	 * JJ> Constructor makes sure the image is correctly loaded
	 * @param copyImg: which BufferedImage to use as the sprite
	 */
	public Image2D( String fileName, int imgWidth, int imgHeight ) {
		
		//First make sure the file actually exists
		if( !ResourceMananger.fileExists(fileName) )
		{
			Log.error( "Failed loading image: " + fileName );
		}
		ImageIcon load = new ImageIcon(ResourceMananger.getFilePath(fileName));
		
		
		//First make sure the file actually exists
		if( load.getIconWidth() < 0 )
		{
			Log.error( "Failed loading image: " + fileName );
		}

		//Load the image into a BufferedImage
		original = new BufferedImage( imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = original.createGraphics();
        Video.getGraphicsSettings(g);
        g.drawImage(load.getImage(), 0, 0, imgWidth, imgHeight, null ); 
        g.dispose();
        
		baseWidth = imgWidth;
		width = (int)(baseWidth*1.20f);
		baseHeight = imgHeight;
		height = (int)(baseHeight*1.20f);
	}
		
	/**
	 * JJ> Makes sure we have a VolatileImage object to draw on
	 *     If it got lost, it creates a new one.
	 */
	private Graphics2D getVolatileMemory() {
		if( processed == null || processed.contentsLost() || processed.getWidth() != width || processed.getHeight() != height )
		{
			processed = Video.createVolatileImage(width, height);
		}

        Graphics2D g = processed.createGraphics();
        Video.getGraphicsSettings(g);

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
		if(angle == 0) return;
		currentAngle += angle;
		noChange = false;
	}

	/**
	 * JJ> Sets the image rotation to the specified degrees
	 * @param angle: the new direction
	 */
	public void setDirection(float angle) {  
		if(angle == 0) return;
		currentAngle = angle;
		noChange = false;
    }  
	
	/**
	 * JJ> direct scaling of a image using the resize method
	 */
	public void scale(float multiplier) {   
        resize( (int)(width * multiplier), (int)(height * multiplier) );	
		noChange = false;
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
		noChange = false;
    }
	
	/**
	 * JJ> Flips the image horizontally
	 */
	public void horizontalFlip() {
		flipHorizontal = !flipHorizontal;
		noChange = false;
	}
	
	/**
	 * JJ> Flips the image vertically
	 */
	public void verticalFlip() {  
		flipVertical = !flipVertical;
		noChange = false;
    } 
		
	/**
	 * JJ> Makes the image partially or fully transparent 
	 * @param transperancy value should be between 0.00 (completely transparent) and 1.00 (normal)
     */
	public void setAlpha(float transperancy) { 
		//Clip the parameter to a valid value so that we do not get an error message
		currentAlpha = Math.min( 1.00f, Math.max(0.00f, transperancy) );        
		noChange = false;
    }
	
	/**
	 * JJ> Enables or disables blur effect when rendering this image 
     */
	public void blurImage() {
		blurEffect = !blurEffect;
		noChange = false;
	}

	/**
	 * JJ> Adjusts the RGB channels for this image, allowing to tint it a specific color
	 */
	public void setColorTint(int r, int g, int b) {
		
		//Clip the RGB to valid values
		r = Math.min(255, Math.max(0, r));
		g = Math.min(255, Math.max(0, g));
		b = Math.min(255, Math.max(0, b));
		
		//Set the RGB channels, but leave the alpha alone
		colorTint = 0xFF000000 | r << 16 | g << 8 | b << 0;
		noChange = false;
	}
		
	/**
	 * JJ> Returns this Image2D as a Image instance
	 * @return the image ready to be drawn with proper rotation and all
	 */
	public Image toImage() {
		
		//If there are no changes, try to get the old image from memory first
		if( noChange && processed != null && !processed.contentsLost() )
		{
			return processed;
		}
		
		//Draw the image with all filters
		Graphics2D g = getVolatileMemory();
		
		//To make life easier
		BufferedImage draw = original.getSubimage(0, 0, original.getWidth(), original.getHeight());
		
        // Set the Graphics composite to Alpha
		if( currentAlpha < 1 ) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));  
        if( currentAlpha == 0 ) return processed;
		
		//Do rotation
		if(currentAngle != 0) g.rotate(currentAngle, width/2.0, height/2.0);
		
		//Blur effect
		//TODO: does this actually work?
		if(blurEffect||true)
		{
			BufferedImageOp op = new ConvolveOp(blur);
			draw = op.filter(draw, null);
		}
		
		// Flip the image vertically or horizontally
		if( flipHorizontal && flipVertical )
		{
			Graphics2D flip = draw.createGraphics();
			flip.drawImage (draw, 
		             0, draw.getHeight(), draw.getWidth(), 0,
		             0, 0, draw.getWidth(), draw.getHeight(),
		             null);
			flip.dispose();
		}
		else if( flipHorizontal )
		{
			Graphics2D flip = draw.createGraphics();
			flip.drawImage (draw, 
		             0, draw.getHeight(), draw.getWidth(), 0,
		             0, 0, draw.getWidth(), draw.getHeight(),
		             null );
			flip.dispose();
		}
		else if( flipVertical )
		{
			Graphics2D flip = draw.createGraphics();
			flip.drawImage (draw, 
		             draw.getWidth(), 0, 0, draw.getHeight(),
		             0, 0, draw.getWidth(), draw.getHeight(),
		             null);
			flip.dispose();
		}
		
		//Color tint
		if( colorTint != 0xFFFFFFFF )
			for(int x = 0; x < draw.getWidth(); x++)
				for(int y = 0; y < draw.getHeight(); y++)
					draw.setRGB( x, y, draw.getRGB(x, y) & colorTint );
		
		int offsetX = 0;
		int offsetY = 0;
		if(currentAngle != 0) 
		{
			offsetX = (width-baseWidth)/2;
			offsetY = (height-baseHeight)/2;
		}
		//Now actually draw the image
		g.drawImage(
				draw,							//Draw the base image (possibly with blur)
				offsetX, 						//X offset
				offsetY, 						//Y offset
				baseWidth, 						//How much to draw
				baseHeight,								
				null);
		
		//All done!
        g.dispose();
        draw.flush();
        noChange = true;
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
		if(processed != null) processed.flush();
		noChange = false;
		currentAlpha = 1;
		currentAngle = 0;
		flipHorizontal = flipVertical = false;
		resize( original.getWidth(), original.getHeight() );
	}
	
	public void dispose() {
		if(processed != null) processed.flush();
		original.flush();
	}
}
