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

import gameEngine.Image2D;
import gameEngine.Log;
import gameEngine.Profiler;
import gameEngine.Vector;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;

import javax.swing.ImageIcon;

/**
 * JJ> This class generates a nice random background for us
 * @author Johan Jansen and Anders Eie
 *
 */
public class Universe {
	private static int universeSize;
	private ImageIcon[] stars;
	private ArrayList<Image2D> nebulaList;
	final private int WIDTH, HEIGHT;
	private Image[][] universe;
	private Vector[][] bgPos;

	/**
	 * JJ> Draw the entire scene on a BufferedImage so that we do not need to redraw and recalculate every
	 *     component every update. Instead we just draw the BufferedImage.
	 */
	public Universe(Dimension size){
		Profiler.begin("Initializing background");
		this.WIDTH = size.width;
		this.HEIGHT = size.height;
		loadNebulas();
		loadStars();
		Profiler.end("Initializing background");
	}
	
	//Draw a random nebula
	private void drawNebula(Random rand, Graphics2D g) {
		
		//Figure out what it looks like
		int whichNebula = rand.nextInt(nebulaList.size());
		nebulaList.get( whichNebula ).reset();
				
		//Make it unique
		nebulaList.get( whichNebula ).setAlpha( rand.nextFloat() );
		if( rand.nextBoolean() ) nebulaList.get( whichNebula ).horizontalFlip();
		if( rand.nextBoolean() ) nebulaList.get( whichNebula ).verticalFlip();
			
		//Now draw it
		g.drawImage(nebulaList.get( whichNebula ).toImage(), 0, 0, null);
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
			g.drawImage(stars[rand.nextInt(stars.length)].getImage(), rand.nextInt(WIDTH), rand.nextInt(HEIGHT), null);
		}		
	}
		
	//TODO: This function creates a 700 ms bottle neck
	private void loadNebulas(){
		File[] fileList = new File("data/nebula").listFiles();
				
		//Load nebulas into memory
		nebulaList = new ArrayList<Image2D>();
		for( File f : fileList )
		{
			if( !f.isFile() ) continue;
			nebulaList.add( new Image2D( f.toString() )) ;	
			nebulaList.get(nebulaList.size()-1).resize(WIDTH, HEIGHT);
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
				Log.message("Error reading star resource file, creating a new one.");
				if (!starFile.delete()) Log.warning("Could not delete old, corrupted star resource file.");
			}
		}
		else Log.message("Star resource file not found, creating it.");
		
		//Load Stars into memory
		Random rand = new Random();
		ArrayList<ImageIcon> tmpStars = new ArrayList<ImageIcon>();
		for (int s = 1; s < 40; ++s)
		{
			for (int c = 0; c < 256; c += 16)
			{
				BufferedImage star = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
				Graphics2D starGraph = star.createGraphics();
				
				Color col;
				int rareStar = rand.nextInt(20);	
				for (int k = 0; k < s*0.6; ++k)
				{
					if(rareStar == 1)		col = new Color(c, 0, 0, (int)(Math.pow(255, ((float)k/(float)(s*0.6)))));
					else					col = new Color((int)(64+((float)k/(float)s)*192f), 64+(int)(((float)k/(float)s)*192f), c, (int)(Math.pow(255, ((float)k/(float)(s*0.6)))));
					
					starGraph.setColor(col);
					starGraph.fillOval(k, k, s-(2*k), s-(2*k));
				}
				
				tmpStars.add(new ImageIcon(star));
				starGraph.dispose();
			}
		}
		
		stars = new ImageIcon[0];
		stars = tmpStars.toArray(stars);
		
		// Writing stars to file
		if (!starFile.exists())
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
				Log.warning("Error writing star resource file:");
				Log.warning(e);
			}
		}
	}
	
	public static int getUniverseSize()
	{
		return universeSize;
	}
				
	/**
	 * JJ> Draw the finished background to the Graphics specified in the parameter
	 * @param g What do we draw it on?
	 * @param pos Center position of the screen in space
	 */
	public void draw(Graphics g, Vector pos){
		int x, y;
				
		//TODO: optimize this
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
			{
				x = bgPos[i][j].getX()+pos.getX();
				y = bgPos[i][j].getY()+pos.getY();		
				g.drawImage( universe[i][j], x, y, null );
			}		
	}

	public void drawBounds(Graphics g, Vector pos){
		final int SCREEN_X = Game.getScreenWidth();			//Screen width
		final int SCREEN_Y = Game.getScreenHeight();		//Screen height
		
		//Make rectangles yellow
		g.setColor(Color.YELLOW);
		
		//TODO: Draw entire grid... this can be optimized
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
			{
				g.drawRect(i*SCREEN_X+pos.getX(), j*SCREEN_Y+pos.getY(), SCREEN_X, SCREEN_Y);
			}
		
	}

	/**
	 * Generates a new universe with bounds equal to 'size' X 'size'
	 * @param size how big?
	 * @param seed randomizer
	 */
	public void generateWorld(int size, long seed) {
		Profiler.begin("Generating World");

		Random rand = new Random(seed);
		universe = new Image[size][size];
		bgPos = new Vector[size][size];
		universeSize = size;
		
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
			{
				try
				{
			    	BufferedImage buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_USHORT_555_RGB);
		    		Graphics2D g = buffer.createGraphics();
		        	
		            //I: Nebula (10% chance) or Black background (90%)
		    		if( rand.nextInt(100) <= 10 ) drawNebula(rand, g);
		    		else 
		    		{
		    			g.setColor(Color.black);
		    			g.fillRect(0, 0, WIDTH, HEIGHT);
		    		}
	
		    		//II: Stars
		    		drawRandomStarfield(rand, g);
		    		
		    		//All done!
		    		g.dispose();
		    		universe[i][j] = buffer;
		    		bgPos[i][j] = new Vector(i*Game.getScreenWidth(), j*Game.getScreenHeight());
				}
	    	    catch (OutOfMemoryError e) 
	    	    {
    	        	//Ouch, we ran out of memory, the least we can do now is prevent it from crashing
    	        	Log.warning( e.toString() );
    	        	Profiler.memoryReport();
    	        	Runtime.getRuntime().runFinalization();
	    	    }
	    	   
			}
		//stars.clear(); TODO: free resources for the new array
		nebulaList.clear();
		Profiler.end("Generating World");
	}
	
}
