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
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;

/**
 * JJ> This class generates a nice random background for us
 * @author Johan Jansen and Anders Eie
 *
 */
public class Universe {
	private static int universeSize;
	final private int WIDTH, HEIGHT;
	private Vector[][] bgPos;
	private ArrayList<Planet> planetList;
	private ArrayList<Image2D> planetImages;
	private ArrayList<Image2D> nebulaList;
	private ImageIcon[] stars;
	private Image[][] universe;
	
	/**
	 * JJ> Draw the entire scene on a BufferedImage so that we do not need to redraw and recalculate every
	 *     component every update. Instead we just draw the BufferedImage.
	 */
	public Universe(Dimension resolution, int size, long seed){
		Profiler.begin("Initializing background");
		WIDTH = resolution.width;
		HEIGHT = resolution.height;
		loadNebulas();
		loadStars();
		
		//Generate the backgrounds
		generateWorld(size, seed);
    	
		//Generate the planets
    	planetList = generateRandomPlanets( System.currentTimeMillis() );
    	
		Profiler.end("Initializing background");
	}
	
	public ArrayList<Planet> getPlanetList() {
		return planetList;
	}
	
	private ArrayList<Planet> generateRandomPlanets(long seed) {
		Random rand = new Random(seed);
		ArrayList<Planet> planetList = new ArrayList<Planet>();
		
		//Load planet images into memory
		File[] fileList = new File("data/planets").listFiles();
		planetImages = new ArrayList<Image2D>();
		for( File f : fileList )
		{
			if( !f.isFile() ) continue;
			planetImages.add( new Image2D( f.toString()) ) ;
		}
		
		//Randomly generate for every screen
		for(int i = 0; i < Universe.getUniverseSize(); i++)
			for(int j = 0; j < Universe.getUniverseSize(); j++)
			if( rand.nextInt(100) <= 25 )
			{
				planetList.add(new Planet(new Vector(Game.getScreenWidth()/2*i, Game.getScreenHeight()/2*j), planetImages, rand));
			}
		
		//All done!
		planetImages.clear();
    	return planetList;
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
		
	private void loadNebulas(){
		File[] fileList = new File("data/nebula").listFiles();

		//Load nebulas into memory
		nebulaList = new ArrayList<Image2D>();
		for( File f : fileList )
		{
			if( f.isFile() )
			{
				Image2D load = new Image2D( f.toString() );
				load.resize(WIDTH, HEIGHT);
				nebulaList.add( load );	
			}
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
				Log.warning("Error writing star resource file: " + e.toString());
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
	private void drawSingleBackground(Graphics2D g, Vector pos){
		int x, y;
				
		//TODO: optimize this
		for(int i = 0; i < universeSize; i++)
			for(int j = 0; j < universeSize; j++)
			{
				x = bgPos[i][j].getX()+pos.getX();
				y = bgPos[i][j].getY()+pos.getY();		
				g.drawImage( universe[i][j], x, y, null );
			}	
		drawBounds(g, pos);
	}

	public void drawBounds(Graphics2D g, Vector pos){
		final int SCREEN_X = Game.getScreenWidth();			//Screen width
		final int SCREEN_Y = Game.getScreenHeight();		//Screen height
		
		//Make rectangles yellow
		g.setColor(Color.YELLOW);
		
		//TODO: Draw entire grid... this can be optimized
		for(int i = 0; i < universeSize; i++)
			for(int j = 0; j < universeSize; j++)
			{
				g.drawRect(i*SCREEN_X+pos.getX(), j*SCREEN_Y+pos.getY(), SCREEN_X, SCREEN_Y);
			}
		
	}

	/**
	 * Generates a new universe with bounds equal to 'size' X 'size'
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
			    	BufferedImage buffer;
					if( Image2D.isHighQualityMode() )  buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);		
					else 							   buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_USHORT_555_RGB);
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
		    		bgPos[i][j] = new Vector(i*WIDTH, j*HEIGHT);
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
	}

	public void drawBackground(Graphics2D g, Vector position) {
		float uniX = WIDTH*universeSize;
		float uniY = HEIGHT*universeSize;
		Vector pos = position.clone();
		pos.negate();
		
		boolean u = false;
		boolean d = false;
		boolean l = false;
		boolean r = false;
		
		if 		(pos.x < 0) 		  	l = true;
		else if (pos.x > uniX - WIDTH) 	r = true;
		
		if		(pos.y < 0)				u = true;
		else if (pos.y > uniY - HEIGHT)	d = true;

		pos.negate();
		
		drawSingleBackground(g, pos);
		
		if 		(l) drawSingleBackground(g, pos.plus(new Vector(-uniX,0)));
		else if (r) drawSingleBackground(g, pos.plus(new Vector( uniX,0)));

		if 		(u) drawSingleBackground(g, pos.plus(new Vector(0,-uniY)));
		else if (d) drawSingleBackground(g, pos.plus(new Vector(0, uniY)));
		
		if 		(u && l) drawSingleBackground(g, pos.plus(new Vector(-uniX,-uniY)));
		else if (u && r) drawSingleBackground(g, pos.plus(new Vector( uniX,-uniY)));
		else if (d && l) drawSingleBackground(g, pos.plus(new Vector(-uniX, uniY)));
		else if (d && r) drawSingleBackground(g, pos.plus(new Vector( uniX, uniY)));

	}
	
}
