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
import javax.swing.ImageIcon;

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


	/*************************************************************************
	 * JJ> The Private instanced version
	 ************************************************************************/
	private BufferedImage original;					//The image itself
	private BufferedImage processed;				//The image with effects added (rotation, alpha, etc.)
	private float currentAngle;

	/**
	 * JJ> Constructor makes sure the image is correctly loaded
	 * @param fileName: the path and name of the file to load
	 */
	public Image2D( String fileName ) {
		
		ImageIcon load = new ImageIcon(fileName);
		
		//TODO: First make sure the file actually exists
		if( load.getIconWidth() < 0 )
		{
			Log.error( "Failed loading image: " + fileName );
		}

		//Load the image into a BufferedImage
		BufferedImage buffer = new BufferedImage( load.getIconWidth(), load.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = getGraphics(buffer);  
        g.drawImage(load.getImage(), 0, 0, null ); 
        g.dispose();
        
        processed = original = buffer;
		currentAngle = 0;
	}

	/**
	 * JJ> Constructor makes sure the image is correctly loaded
	 * @param copyImg: which BufferedImage to use as the sprite
	 */
	public Image2D( BufferedImage copyImg ) {
		
		//Ensure it is the correct format before loading it
		BufferedImage buffer = new BufferedImage( copyImg.getWidth(), copyImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = getGraphics(buffer);  
        g.drawImage(copyImg, 0, 0, null ); 
        g.dispose();
        
        processed = original = buffer;
		currentAngle = 0;
	}

	
	/**
	 * JJ> This gets the Graphics2D unit for the specified BufferedImage and also sets all the
	 *     graphics quality render parameters automatically.
	 * @param gfx The BufferedImage to build the Graphics2D from
	 * @return Graphics2D for gfx
	 */
	private Graphics2D getGraphics(BufferedImage gfx) {
		Graphics2D g = gfx.createGraphics();  
    	
		if( highQuality )
		{
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	    	g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		   	g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		}
		else
		{
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
			g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		   	g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		}
		
    	return g;
	}

	/**
	 * JJ> Rotates an image with the specified degrees
	 * @param angle: how much to rotate by
	 */
	public void rotate(float angle) {  
		setDirection(currentAngle + angle);
	}
	
	/**
	 * JJ> Sets the image rotation to the specified degrees
	 * @param angle: the new direction
	 */
	public void setDirection(float angle) {  
		
		//No change
		if( angle == currentAngle ) return;
		
        int w = (int) (original.getWidth()*1.42);  
        int h = (int) (original.getHeight()*1.42);
        
        BufferedImage buffer = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = getGraphics(buffer);  
    	        
    	g.rotate(angle, w/2.0, h/2.0);
        g.drawImage(original, null, (w-original.getWidth())/2, (h-original.getHeight())/2);
                
        //Make it so
        currentAngle = angle;
        processed = buffer; 
    }  
	
	/**
	 * JJ> direct scaling of a image using the resize method
	 */
	public void scale(float multiplier) {   
        resize( (int)(original.getWidth() * multiplier), (int)(original.getHeight() * multiplier) );	
	}
	
	/**
	 * JJ> resizing a image using bilinear filtering
	 */
	public void resize(int newW, int newH) {
        int w = original.getWidth();  
        int h = original.getHeight();
        
        //Valid resize?
        if( newW <= 0 || newH <= 0 ) return;
        
        BufferedImage buffer = new BufferedImage(newW, newH, original.getType());  
        Graphics2D g = getGraphics(buffer);          
        g.drawImage(original, 0, 0, newW, newH, 0, 0, w, h, null);  
        g.dispose();

        //Now set this as the new image
        original = processed = buffer;  
    }
	
	/**
	 * JJ> Flips the image horizontally
	 */
	public void horizontalFlip() {  
        int w = original.getWidth();  
        int h = original.getHeight();  
        BufferedImage buffer = new BufferedImage(w, h, original.getType());  
        
        Graphics2D g = getGraphics(buffer);  
        g.drawImage(original, 0, 0, w, h, w, 0, 0, h, null);  
        
        //Now set this as the new image
        g.dispose();  
        original = buffer;
	}
	
	/**
	 * JJ> Flips the image vertically
	 */
	public void verticalFlip() {  
        int w = original.getWidth();  
        int h = original.getHeight();  
        BufferedImage buffer = new BufferedImage(w, h, original.getColorModel().getTransparency());  
        
        Graphics2D g = getGraphics(buffer);
        g.drawImage(original, 0, 0, w, h, 0, h, w, 0, null);  
        
        //Now set this as the new image
        g.dispose();  
        original = buffer;  
    } 
	
	/**
	 * JJ> Makes a certain color transparent
	 * @param color: Which color to make transparent
	 */
	public void makeColorTransparent(Color color) {  
        BufferedImage buffer = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);  
        
        //Create a buffer copy of the original
        Graphics2D g = getGraphics(buffer);
        g.setComposite(AlphaComposite.Src);  
        g.drawImage(original, null, 0, 0);  
        g.dispose();
        
        //This does all the work and makes the correct pixels transparent
        for(int i = 0; i < buffer.getHeight(); i++) 
        {  
            for(int j = 0; j < buffer.getWidth(); j++) 
            {  
                if( buffer.getRGB(j, i) == color.getRGB() ) 
                {  
                	buffer.setRGB(j, i, 0x8F1C1C);  
                }  
            }  
        }
        
        //Now set this as the new image
        original = buffer; 
    }
	
	/**
	 * JJ> Makes the image partially or fully transparent 
	 * @param transperancy: value should be between 0.00 (completely transparent) and 1.00 (normal)
     */
	public void setAlpha(float transperancy) { 
		
		//Clip the parameter to a valid value so that we do not get an error message
		transperancy = Math.min( 1.00f, Math.max(0.00f, transperancy) );
        
        // Get the images graphics  
		BufferedImage buffer = new BufferedImage( original.getWidth(), original.getHeight(), BufferedImage.TRANSLUCENT );  
        Graphics2D g = getGraphics(buffer);  
        
        // Set the Graphics composite to Alpha  
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transperancy));  
        
        // Draw the original img into the prepared receiver image  
        g.drawImage(original, null, 0, 0);  
        
        //Now set this as the new image
        processed = buffer;  
        
        // let go of all system resources in this Graphics  
        g.dispose();  
    }  
		
	/**
	 * JJ> Returns this Image2D as a Image instance
	 * @return the image ready to be drawn with proper rotation and all
	 */
	public Image toImage(){
		//return Toolkit.getDefaultToolkit().createImage(rotated.getSource());
		return processed;
	}
	
	/**
	 * JJ> Get width for this buffered image
	 */
	public int getWidth() {
		return processed.getWidth();
	}
	
	/**
	 * JJ> Get height for this buffered image
	 */
	public int getHeight() {
		return processed.getHeight();
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
		processed = original;
	}
}
