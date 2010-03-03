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

public class Image2D
{
	private BufferedImage img;
	
	//JJ> Rotates an image with the specified degrees
	public void rotate(float angle) {  
        int w = img.getWidth();  
        int h = img.getHeight();
        
        BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffer.createGraphics();  
        g.rotate(Math.toRadians(angle), w/2, h/2);
        g.drawImage(img, null, 0, 0);
        
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
        
        img = buffer;  
    }
	
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
		        BufferedImage buffer = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);  
		        
		        Graphics2D g = buffer.createGraphics();  
		        g.drawImage(img, null, 0, 0 ); 
		        g.dispose();
		        
		        img = buffer; 				
			}
			
			
		} catch (IOException e) {
			Log.warning(e.toString());
		}
	}
	
	//JJ> Returns this Image2D as a Image
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
}
