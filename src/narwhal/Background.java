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
package narwhal;

import gameEngine.*;
import gameEngine.Configuration.VideoQuality;

import java.awt.*;
import java.awt.image.VolatileImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;

/**
 * JJ> This class generates a nice random background for us
 * @author Johan Jansen and Anders Eie
 *
 */
public class Background {
	
	//Data that only need to be loaded once into memory
	private static ArrayList<Image2D> nebulaList;
	private static ImageIcon[] stars;
	
	private int universeSize;
	private Vector[][] bgPos;
	private Image[][] universe;
	
	//The resolution of the background
	//private static final Vector BG_SIZE = new Vector(800, 640);
	private static Vector BG_SIZE;
	
	/**
	 * JJ> Draw the entire scene on a BufferedImage so that we do not need to redraw and recalculate every
	 *     component every update. Instead we just draw the BufferedImage.
	 */
	public Background(int size, long seed){
		Profiler.begin("Initializing background");
		
		//Set background resolution and detail depending on video quality
		if( GameEngine.getConfig().getQualityMode()      == VideoQuality.VIDEO_LOW )  BG_SIZE = new Vector(480, 360);
		else if( GameEngine.getConfig().getQualityMode() == VideoQuality.VIDEO_HIGH ) BG_SIZE = new Vector(800, 600);
		else 														                  BG_SIZE = new Vector(640, 480);
		
		//Load resources
		if( nebulaList == null ) loadNebulas();
		if( stars == null )      loadStars();
		
		//Generate the backgrounds
		generateWorld(size, seed);
    	
		Profiler.end("Initializing background");
	}
	
	//Draw a random nebula
	private void drawNebula(Random rand, Graphics2D g) {
	
		//Figure out what it looks like
		Image2D nebula = nebulaList.get( rand.nextInt(nebulaList.size()) );

		//Make it unique
		nebula.setAlpha( Math.max(0.15f, rand.nextFloat()) );
		if( rand.nextBoolean() ) nebula.horizontalFlip();
		if( rand.nextBoolean() ) nebula.verticalFlip();

		//Now draw it
		nebula.draw(g, 0, 0);
	}
	
	/**
	 * JJ> Randomly draw the starfield
	 * @param rand
	 * @param g
	 */
	private void drawRandomStarfield(Random rand, Graphics2D g) {
		int numberOfStars = 125 + rand.nextInt(250);
		for (int i = 0; i < numberOfStars; ++i)
		{
			Image starImage = stars[rand.nextInt(stars.length)].getImage();
			int x = rand.nextInt(BG_SIZE.getX() - starImage.getWidth(null) );
			int y = rand.nextInt(BG_SIZE.getY() - starImage.getHeight(null));
			g.drawImage(starImage, x, y, null);
		}		
	}
		
	private void loadNebulas(){
		String[] fileList = ResourceMananger.getFileList("/data/nebula/");

		//Load nebulas into memory
		nebulaList = new ArrayList<Image2D>();
		for( String fileName : fileList )
		{
			Image2D load = new Image2D(fileName, BG_SIZE.getX(), BG_SIZE.getY() );
			nebulaList.add( load );
		}
	}
	
	/**
	 * AE> Predrawing the stars and placing them in the static ArrayList stars
	 */
	private void loadStars() {
		
		// Loading stars from resource file if possible
		File starFile = new File("stars.resource");
		if (starFile.exists())
		{
			try
			{
				FileInputStream starFileStream = new FileInputStream(starFile);
				ObjectInputStream starStream = new ObjectInputStream(starFileStream);
				stars = (ImageIcon[])starStream.readObject();
				starStream.close();
				Log.message("Sucessfully loaded stars from file");
				return;
			}
			catch (Exception e) 
			{
				Log.message("Error reading star resource file, creating a new one. (" + e.toString() + ")");
				if (!starFile.delete()) Log.warning("Could not delete old, corrupted star resource file.");
			}
		}
		else Log.message("Star resource file not found, creating it.");
		
		//Load Stars into memory
		Random rand = new Random();
		ArrayList<ImageIcon> tmpStars = new ArrayList<ImageIcon>();
		for (int s = 1; s < 40; ++s)
		{
			VolatileImage star = GameEngine.createVolatileImage(s, s);
			Graphics2D starGraph = star.createGraphics();
			for (int c = 0; c < 256; c += 16)
			{
				//Clear background
				starGraph.setBackground(new Color(0,0,0,0));
				starGraph.clearRect(0, 0, star.getWidth(), star.getHeight());
				
				Color col;
				int rareStar = rand.nextInt(20);	
				for (int k = 0; k < s*0.6; ++k)
				{
					if(rareStar == 1)		col = new Color(c, 0, 0, (int)(Math.pow(255, ((float)k/(float)(s*0.6)))));
					else					col = new Color((int)(64+((float)k/(float)s)*192f), 64+(int)(((float)k/(float)s)*192f), c, (int)(Math.pow(255, ((float)k/(float)(s*0.6)))));
					
					starGraph.setColor(col);
					starGraph.fillOval(k, k, s-(2*k), s-(2*k));
				}
				
				//TODO: bad! it seems 40*256=10240 elements are added to this ArrayList
				tmpStars.add(new ImageIcon(star.getSnapshot()));
			}
			
			//Release resources
			starGraph.dispose();
		}
				
		stars = new ImageIcon[0];
		stars = tmpStars.toArray(stars);
		
		// Writing stars to file
		if ( !starFile.exists() )
		{
			try
			{
				if (!starFile.createNewFile()) throw new Exception("Cannot create new star resource file");
				FileOutputStream starFileStream = new FileOutputStream(starFile);
				ObjectOutputStream starStream = new ObjectOutputStream(starFileStream);
				starStream.writeObject(stars);
				starStream.close();
			}
			catch (Exception e) 
			{
				Log.warning("Error writing star resource file: " + e.toString());
			}
		}
	}
	
	public Vector getUniverseSize() {
		return new Vector( universeSize * GameEngine.getScreenWidth(), universeSize * GameEngine.getScreenHeight() );
	}
				
	public void drawBounds(Graphics2D g, Vector pos){
		
		if( !GameEngine.getConfig().debugMode ) return;
				
		final int SCREEN_X = GameEngine.getScreenWidth();		//Screen width
		final int SCREEN_Y = GameEngine.getScreenHeight();		//Screen height
		
		//Make rectangles yellow
		g.setColor(Color.YELLOW);
		
		for(int i = 0; i < universeSize; i++)
			for(int j = 0; j < universeSize; j++)
			{
				g.drawRect(i*SCREEN_X + pos.getX(), j*SCREEN_Y + pos.getY(), SCREEN_X, SCREEN_Y);
			}
		
	}

	/**
	 * Generates a new universe with bounds equal to (size*size)
	 * @param size how big?
	 * @param seed randomizer
	 */
	private void generateWorld(int size, long seed) {
		Random rand = new Random(seed);
		universe = new Image[size][size];
		bgPos = new Vector[size][size];
		universeSize = size;
		
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
			{
				try
				{
					Image buffer = GameEngine.createVolatileImage(BG_SIZE.getX(), BG_SIZE.getY());
		    		Graphics2D g = (Graphics2D)buffer.getGraphics();
		        	
		            //I: Nebula (10% chance) or Black background (90%)
	    			g.setColor(Color.BLACK);
	    			g.fillRect(0, 0, BG_SIZE.getX(), BG_SIZE.getY());
	    			if( rand.nextInt(100) <= 10 ) drawNebula(rand, g);
	
		    		//II: Stars
		    		drawRandomStarfield(rand, g);
		    				    		
		    		universe[i][j] = buffer;
		    		bgPos[i][j] = new Vector(i*GameEngine.getScreenWidth(), j*GameEngine.getScreenHeight());

		    		//All done! Free any resources we have used
		    		g.dispose();
				}
	    	    catch (OutOfMemoryError e) 
	    	    {
    	        	//Ouch, we ran out of memory, the least we can do now is prevent it from crashing
    	        	Log.warning( e.toString() );
    	        	Profiler.memoryReport();
    	        	Runtime.getRuntime().runFinalization();
	    	    }	    	   
			}
	}

	public void drawBackground(Graphics2D g, Vector position, Vector speed) {
		int bg = 0;
		Composite reset = g.getComposite();
		
		if( GameEngine.getConfig().getQualityMode() == VideoQuality.VIDEO_LOW )
		{
			//Without motion blur
			g.setBackground(Color.BLACK);
			g.clearRect(0, 0, GameEngine.getScreenWidth(), GameEngine.getScreenHeight());
		}
		else
		{
			//With motion blur
			float blur = 1f / Math.max(1, speed.length()*0.125f);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blur));
		}


		for(int i = 0; i < universeSize; i++)
			for(int j = 0; j < universeSize; j++)
			{
				int x = bgPos[i][j].getX()-position.getX();
				int y = bgPos[i][j].getY()-position.getY();
				
				//Only draw whatever we need to draw
				if(x < -GameEngine.getScreenWidth() || x > GameEngine.getScreenWidth()) break;
				if(++y > GameEngine.getScreenHeight()) break;
				if(y < -GameEngine.getScreenHeight()) continue;
				
				g.drawImage( universe[i][j], x, y, GameEngine.getScreenWidth(), GameEngine.getScreenHeight(), null );
				bg++;
			}	

		//Remove any blur effect
		g.setComposite(reset);

		//Debug info
		if( GameEngine.getConfig().debugMode )
		{
			g.setColor(Color.WHITE);
			g.drawString("Backgrounds drawn: " + bg, 5, 80);
		}
	}
}
