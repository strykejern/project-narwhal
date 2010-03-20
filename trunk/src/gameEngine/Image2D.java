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
	final private static Kernel blur = new Kernel(3, 3,
		    new float[] {
	        1f/9f, 1f/9f, 1f/9f,
	        1f/9f, 1f/9f, 1f/9,
	        1f/9f, 1f/9f, 1f/9f});
	
	private BufferedImage original;					//The image itself
	private BufferedImage processed;				//The image with effects added (rotation, alpha, etc.)
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
	 * JJ> Makes sure we have a proper place in memory to store our processed image
	 */
	private Graphics2D getBufferMemory() {
		if( processed == null || processed.getWidth() != width || processed.getHeight() != height )
		{
			processed = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
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
	final public Image toImage() {
				
		//If there are no changes, try to get the old image from memory first
		if( noChange && processed != null )
		{
			return processed;
		}
		
		//Create a buffer from the original image
		BufferedImage buffer = original.getSubimage(0, 0, original.getWidth(), original.getHeight());
		Graphics2D g = buffer.createGraphics();
        Video.getGraphicsSettings(g);
				
        // Set the Graphics composite to Alpha
		if( currentAlpha < 1 ) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));  
		//TODO: alpha == 0 optimization
						
		// Flip the image vertically or horizontally
		if( flipHorizontal && flipVertical )
		{
			g.drawImage (buffer, 
		             0, buffer.getHeight(), buffer.getWidth(), 0,
		             0, 0, buffer.getWidth(), buffer.getHeight(),
		             null);
		}
		else if( flipHorizontal )
		{
			g.drawImage (buffer, 
		             0, buffer.getHeight(), buffer.getWidth(), 0,
		             0, 0, buffer.getWidth(), buffer.getHeight(),
		             null );
		}
		else if( flipVertical )
		{
			g.drawImage (buffer, 
		             buffer.getWidth(), 0, 0, buffer.getHeight(),
		             0, 0, buffer.getWidth(), buffer.getHeight(),
		             null);
		}
		
		//Blur effect
		if( blurEffect )
		{
			BufferedImageOp op = new ConvolveOp( blur, ConvolveOp.EDGE_ZERO_FILL, null);
			buffer = op.filter(buffer, null);
		}
		
		//Color tint
		if( colorTint != 0xFFFFFFFF )
			for(int x = 0; x < buffer.getWidth(); x++)
				for(int y = 0; y < buffer.getHeight(); y++)
					buffer.setRGB( x, y, buffer.getRGB(x, y) & colorTint );
		
		//Now actually store the buffered instance in memory
		//And apply rotation and resizing (done last)
		Graphics2D memory = getBufferMemory();
		
		//Do rotation
		int offsetX = 0;
		int offsetY = 0;
		if(currentAngle != 0)
		{
			memory.rotate(currentAngle, width/2.0, height/2.0);
			offsetX = (width-baseWidth)/2;
			offsetY = (height-baseHeight)/2;
		}
		
		//Resize
		memory.drawImage(
				buffer,						    //Draw the base image
				offsetX, 						//X offset
				offsetY, 						//Y offset
				baseWidth, 						//How much to draw
				baseHeight,								
				null);
		
		//All done! Free any resources...
		memory.dispose();
		g.dispose();
		buffer.flush();
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
}
