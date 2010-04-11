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


import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.ImageIcon;

/**
 * JJ> Helper class to make image handling easier to do
 * @author Johan Jansen and Anders Eie
 */
public class Image2D {
	final private static Kernel EMBOSS = new Kernel(3, 3,
		    new float[] {
		        -2, 0, 0,
		        0, 1, 0,
		        0, 0, 2});
	final private static Kernel BLUR = new Kernel(3, 3,
		    new float[] {
	        1f/9f, 1f/9f, 1f/9f,
	        1f/9f, 1f/9f, 1f/9f,
	        1f/9f, 1f/9f, 1f/9f});
	final private static int NO_RGB_TINT = 0xFFFFFFF; 
	
	private BufferedImage original;					//The image itself
	private BufferedImage processed;				//The image with effects added (rotation, alpha, etc.)
	private int width, height;

	private boolean flipHorizontal = false;
	private boolean flipVertical = false;
	private boolean blurEffect = false;
	private boolean embossEffect = false;
	private boolean noChange = false;
	private float currentAlpha = 1;
	private float currentAngle = 0;
	private int   colorTint = NO_RGB_TINT;
	
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
		processed = original = Video.createBufferedImage( load.getIconWidth(), load.getIconHeight() );
        Graphics2D g = original.createGraphics();
        Video.getGraphicsSettings(g);
        g.drawImage(load.getImage(), 0, 0, null ); 
        g.dispose();
        
		width = original.getWidth();
		height = original.getHeight();
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
		
		//Load the image into a BufferedImage
		original = Video.createBufferedImage(imgWidth, imgHeight);
        Graphics2D g = original.createGraphics();
        Video.getGraphicsSettings(g);
        g.drawImage(load.getImage(), 0, 0, imgWidth, imgHeight, null ); 
        g.dispose();
        
		width = original.getWidth();
		height = original.getHeight();
	}
	
	/**
	 * JJ> Constructor that turns a Image2D into a new Image2D
	 * @note: The Image2D reset state is the current state of the Image2D to be cloned
	 */
	private Image2D( Image2D clone ) {
		width = clone.getWidth();
		height = clone.getHeight();
		
		//Make a copy of the current state of the Image2D
		original = Video.createBufferedImage(width, height);
		Graphics2D g = original.createGraphics();
        Video.getGraphicsSettings(g);
        g.drawImage(clone.getSnapshot(), 0, 0, width, height, null ); 
        g.dispose();
	}
	
	/**
	 * JJ> Rotates an image with the specified degrees
	 * @param angle: how much to rotate by
	 */
	public void rotate(float angle) {
		if(angle == 0) return;
		currentAngle += angle;
	}

	/**
	 * JJ> Sets the image rotation to the specified degrees
	 * @param angle: the new direction
	 */
	public void setDirection(float angle) {  
		if(angle == 0) return;
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
        
        width = newW;
        height = newH;
		noChange = false;
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
	 * JJ> Enables or disables emboss effect when rendering this image 
     */
	public void embossImage() {
		embossEffect = !embossEffect;
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
	 * JJ> Returns a static snapshot image of this object. The Image returned is only 
	 * current with the Image2D at the time of the request and will not be updated 
	 * with any future changes to the Image2D. 
	 * @return Returns: a Image representation of this Image2D
	 */
	public Image getSnapshot() {
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
		colorTint = 0xFFFFFFFF;
		flipHorizontal = flipVertical = false;
		resize( original.getWidth(), original.getHeight() );
	}
	
	/**
	 * JJ> Flushes all reconstructible resources being used by this Image2D object. 
	 */
	public void dispose() {
		if(processed != null) 
		{
			processed.flush();
			processed = null;
		}
		original.flush();
	}
	
	/**
	 * JJ> Returns a new clone of this Image2D
	 */
	public Image2D clone() {
		return new Image2D(this);
	}

	/**
	 * JJ> This draws this Image2D to a Graphics2D
	 * @param g Which Graphics2D to draw on
	 * @param x X position of image
	 * @param y Y position of image
	 */
	public void draw(Graphics2D g, int x, int y) {
		
		//Don't draw invisible images
		if( currentAlpha == 0 ) return;
		
		//Re-render if there were changes on the image
		if( !noChange )
		{
			//Create a buffer from the original image			
			processed = Video.createBufferedImage(width, height);
			
			Graphics2D r = processed.createGraphics();
	        Video.getGraphicsSettings(r);
	        r.drawImage(original, 0, 0, width, height, null);
	
	        // Set the Graphics composite to Alpha
			if( currentAlpha < 1 ) r.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));  
			
			//Blur effect
			if( blurEffect )
			{
				BufferedImageOp op = new ConvolveOp( BLUR );
				processed = op.filter(processed, null);
			}
						
			//Emboss effect
			if( embossEffect )
			{
				BufferedImageOp op = new ConvolveOp( EMBOSS );
				processed = op.filter(processed, null);
			}
				
			//Color tint
			if( colorTint != NO_RGB_TINT )
				for(int i = 0; i < processed.getWidth(); i++)
					for(int j = 0; j < processed.getHeight(); j++)
						processed.setRGB( i, j, processed.getRGB(i, j) & colorTint );
	
			//All done! Free any resources...
			r.dispose();
	        noChange = true;
		}
		
		//Correct position
		AffineTransform xs = g.getTransform();
		xs.translate(x, y);
		
		//Image flipping
		if( flipHorizontal ) 
		{
			xs.scale(-1.0, 1.0);
			xs.translate(-width, 0);
		}
		if( flipVertical )
		{
			xs.scale(1.0, -1.0);
			xs.translate(0, -height);
		}
			
		//Rotate
		if(currentAngle != 0) xs.rotate(currentAngle, width/2, height/2);

		//Now do the actual drawing
		g.drawImage(processed, xs, null);
	}
}
