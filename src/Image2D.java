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
	private BufferedImage img;
	private BufferedImage original;
	
	//JJ> Constructor makes sure the image is correctly loaded
	public Image2D( String fileName ) {
		File f = new File( fileName );
		if( !f.exists() )
		{
			Log.warning( "Failed loading image, does not exist: " + f.getAbsolutePath() );
		}

		try {
			img = ImageIO.read(f);
			
			//This makes sure that the type of the image is valid so that it is safe to use
			if( img.getType() == BufferedImage.TYPE_CUSTOM )
			{
		        BufferedImage buffer = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);  
		        
		        Graphics2D g = buffer.createGraphics();  
		        g.drawImage(img, null, 0, 0 ); 
		        g.dispose();
		        
		        img = buffer;
			}
			original = img;
			
		} catch (IOException e) {
			Log.warning(e.toString());
		}
	}
	
	//JJ> Rotates an image with the specified degrees
	public void rotate(float angle) {  
        int w = img.getWidth();  
        int h = img.getHeight();
        
        BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffer.createGraphics();  
        g.rotate(Math.toRadians(angle), w/2, h/2);
        g.drawImage(img, null, 0, 0);
        
        //Make it so
        img = buffer; 
    }  
	
	//JJ> scaling a image using bilinear filtering
	public void resize(int newW, int newH) {  
        int w = img.getWidth();  
        int h = img.getHeight();  
        
        BufferedImage buffer = new BufferedImage(newW, newH, img.getType());  
        Graphics2D g = buffer.createGraphics();  
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);  
        g.dispose();
        
        //Now set this as the new image
        img = buffer;  
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
        for(int i = 0; i < dimg.getHeight(); i++) {  
            for(int j = 0; j < dimg.getWidth(); j++) {  
                if(dimg.getRGB(j, i) == color.getRGB()) {  
                dimg.setRGB(j, i, 0x8F1C1C);  
                }  
            }  
        }
        
        //Now set this as the new image
        img = dimg; 
    }
	
	//JJ> Makes the image transparent
	public void setAlpha(float transperancy) {  
        // Create the image using the   
        BufferedImage aimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TRANSLUCENT);  
        
        // Get the images graphics  
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
	public Image toImage()
	{
		return img;
	}
	
	//JJ> Get width and height for this buffered image
	public int getWidth() 
	{
		return img.getWidth();
	}
	public int getHeight() 
	{
		return img.getHeight();
	}
	
	//JJ> Returns the image to its original state when it was first loaded
	public void reset() {  
		img = original;
	}
}
