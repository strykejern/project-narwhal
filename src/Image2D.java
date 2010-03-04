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
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

//JJ> Helper class to make image handling easier to do
public class Image2D
{
	private BufferedImage img;			//The image itself
	private BufferedImage rotated;		//The image with effects added (rotation, alpha, etc.)
	private BufferedImage original;		//The image when it was first loaded
	private int currentAngle;
		
	//JJ> Constructor makes sure the image is correctly loaded
	public Image2D( String fileName ) {
		
		//First make sure the file actually exists
		File f = new File( fileName );
		if( !f.exists() ) 
		{
			Log.warning( "Failed loading image, does not exist: " + f.getAbsolutePath() );
		}

		//Now try reading it
		try 
		{
			img = ImageIO.read(f);
			this.makeValid();
			rotated = original = img;
			currentAngle = 0;
			
		} 		
		catch (IOException e) 
		{
			Log.warning(e.toString());
		}
	}
	
	public Image2D( BufferedImage copyImg ) {
		img = copyImg;
		this.makeValid();
		rotated = original = img;
		currentAngle = 0;
	}
	
	//JJ> This makes sure that the type of the image is valid so that it is safe to use
	private void makeValid() {
		if( img.getType() == BufferedImage.TYPE_CUSTOM ) 
		{
			Log.message("Unknown image format. Converting to TYPE_INT_ARGB to prevent errors.");
	        BufferedImage buffer = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);  
	        
	        Graphics2D g = buffer.createGraphics();  
	        g.drawImage(img, null, 0, 0 ); 
	        g.dispose();
	        
	        img = buffer;
		}	
	}

	//JJ> Rotates an image with the specified degrees
	public void rotate(float angle) {  
		setDirection(currentAngle + angle);
	}
	
	//JJ> Sets the image rotation to the specified degrees
	public void setDirection(float angle) {  
		
		//Limit the angles
		while(angle > 360) angle -= 360;
		while(angle < 0) angle += 360;
		
        int w = img.getWidth();  
        int h = img.getHeight();
        
        BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffer.createGraphics();  
    	//g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    	//g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
        g.rotate(Math.toRadians(angle), w/2, h/2);
        g.drawImage(img, null, 0, 0);
        
        //Make it so
        currentAngle = (int)angle;
        rotated = buffer; 
    }  
	
	//JJ> direct scaling of a image using the resize method
	public void scale(float multiplier) {   
        resize( (int)(img.getWidth() * multiplier), (int)(img.getHeight() * multiplier) );	
	}
	
	//JJ> resizing a image using bilinear filtering
	public void resize(int newW, int newH) {  
        int w = img.getWidth();  
        int h = img.getHeight();  
        boolean highQuality = false;
        
        BufferedImage buffer = new BufferedImage(newW, newH, img.getType());  
        Graphics2D g = buffer.createGraphics();
        
        if( highQuality )
        {
        	g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        	g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
        }
        else g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );  
        
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);  
        g.dispose();

        //Now set this as the new image
        img = rotated = buffer;  
    }
	
	//JJ> Flips the image horizontally
	public void horizontalflip() {  
        int w = img.getWidth();  
        int h = img.getHeight();  
        BufferedImage dimg = new BufferedImage(w, h, img.getType());  
        Graphics2D g = dimg.createGraphics();  
        g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);  
        g.dispose();  
        
        //Now set this as the new image
        img = dimg;
	}
	
	//JJ> Flips the image vertically
	public void verticalflip() {  
        int w = img.getWidth();  
        int h = img.getHeight();  
        BufferedImage dimg = new BufferedImage(w, h, img.getColorModel().getTransparency());  
        Graphics2D g = dimg.createGraphics();  
        g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);  
        g.dispose();  
        
        //Now set this as the new image
        img = dimg;  
    } 
	
	//JJ> Makes a certain color transparent
	public void makeColorTransparent(Color color) {  
        BufferedImage dimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);  
        
        //Create a buffer copy of the original
        Graphics2D g = dimg.createGraphics();  
        g.setComposite(AlphaComposite.Src);  
        g.drawImage(img, null, 0, 0);  
        g.dispose();
        
        //This does all the work and makes the correct pixels transparent
        for(int i = 0; i < dimg.getHeight(); i++) 
        {  
            for(int j = 0; j < dimg.getWidth(); j++) 
            {  
                if( dimg.getRGB(j, i) == color.getRGB() ) 
                {  
                	dimg.setRGB(j, i, 0x8F1C1C);  
                }  
            }  
        }
        
        //Now set this as the new image
        img = dimg; 
    }
	
	//JJ> Makes the image transparent, value should be between 0.00 (completely 
	//    transparent) and 1.00 (normal)
	public void setAlpha(float transperancy) { 
		
		//Clip the parameter to a valid value so that we do not get an error message
		transperancy = Math.min( 1.00f, Math.max(0.00f, transperancy) );
        
        // Get the images graphics  
		BufferedImage aimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TRANSLUCENT);  
        Graphics2D g = aimg.createGraphics();  
        
        // Set the Graphics composite to Alpha  
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transperancy));  
        
        // Draw the original img into the prepared receiver image  
        g.drawImage(img, null, 0, 0);  
        
        // let go of all system resources in this Graphics  
        g.dispose();  
        
        //Now set this as the new image
        img = aimg;  
    }  
		
	//JJ> Returns this Image2D as a Image instance
	public Image toImage(){
		return rotated;
	}
	
	//JJ> Get width and height for this buffered image
	public int getWidth() {
		return img.getWidth();
	}
	public int getHeight() {
		return img.getHeight();
	}

	//JJ> Returns the current angle for this image
	int getAngle(){
		return currentAngle;
	}

	//JJ> Returns the image to its original state when it was first loaded
	public void reset() {  
		img = original;
	}
}
