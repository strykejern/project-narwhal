import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;

import javax.swing.ImageIcon;

public class Image2D 
{
	private Image img;
	
	//JJ> Constructor makes sure the image is correctly loaded
	public Image2D( String fileName ) {
		File f = new File( fileName );
		if( !f.exists() )
		{
			Log.warning( "Failed loading image, does not exist: " + f.getAbsolutePath() );
		}
		img = Toolkit.getDefaultToolkit().getImage( fileName );
	}
	
	//JJ> scaling a image @TODO: doesn't work yet!
	public Image scale(int width, int height){
		return img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	}
	
	//JJ> Returns this Image2D as a normal image so that it can be drawn
	public Image toImage()
	{
		return img;
	}
	

	//JJ> Rotates a Image2D to the specified degrees
	public void rotate(float degree){
	    img = tilt( this.toBufferedImage(), Math.toRadians(degree) );
	}

	//JJ> The raw rotation code uses a temporary instance of a BufferedImage
	private BufferedImage tilt(BufferedImage image, double angle) {
	    double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
	    int w = image.getWidth(), h = image.getHeight();
	    int neww = (int)Math.floor(w*cos+h*sin), newh = (int)Math.floor(h*cos+w*sin);
	    GraphicsConfiguration gc = getDefaultConfiguration();
	    BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.TRANSLUCENT);
	    Graphics2D g = result.createGraphics();
	    g.translate((neww-w)/2, (newh-h)/2);
	    g.rotate(angle, w/2, h/2);
	    g.drawRenderedImage(image, null);
	    g.dispose();
	    
	    return result;
	}

    public static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }

    // An Image object cannot be converted to a BufferedImage object.
    // The closest equivalent is to create a buffered image and then draw the image on the buffered image.
    // This example defines a method that does this.
    // This method returns a buffered image with the contents of an image
    public BufferedImage toBufferedImage() {
        if (img instanceof BufferedImage) {
            return (BufferedImage)img;
        }

        // This code ensures that all the pixels in the image are loaded
        img = new ImageIcon(img).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        boolean hasAlpha = this.hasAlpha();

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                img.getWidth(null), img.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return bimage;
    }

    // This method returns true if the specified image has transparent pixels
    public boolean hasAlpha() {
        // If buffered image, the color model is readily available
        if (img instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage)img;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
         PixelGrabber pg = new PixelGrabber(img, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        	//todo
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }

	public int getWidth() 
	{
		return img.getWidth(null);
	}
	public int getHeight() 
	{
		return img.getHeight(null);
	}
}
