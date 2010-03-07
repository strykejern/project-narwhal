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
import gameEngine.Planet;
import gameEngine.Profiler;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;

/**
 * JJ> This class generates a nice random background for us
 * @author Johan Jansen and Anders Eie
 *
 */
public class Background {
	private HashMap<Long, Image> imageHashMap = new HashMap<Long, Image>(10, 0.75f);
	private long currentSeed;
	private ArrayList<BufferedImage> stars;
	private ArrayList<Image2D> nebulaList;
	private ArrayList<Planet> planetList;
	boolean initialized = false;
	final private int WIDTH, HEIGHT;
	
	/**
	 * JJ> Draw the entire scene on a BufferedImage so that we do not need to redraw and recalculate every
	 *     component every update. Instead we just draw the BufferedImage.
	 */
	public Background(int width, int height, long seed){
		Profiler.begin("Initializing background");
		this.WIDTH = width;
		this.HEIGHT = height;
		generate( seed );
		loadNebulas();
		loadPlanets();
		loadStars();
		initialized = true;
		Profiler.end("Initializing background");
	}
	
	public void generate(long seed) {
		if(!initialized) return;
		
		Profiler.begin("Generating Background");
		
		//Important, do first: generate the random seed
		Random rand = new Random (seed);
		currentSeed = seed;
				
		//Have we visited this place before? No need to continue then!
		if( imageHashMap.containsKey( currentSeed ) )
		{
			Profiler.end("Generating Background");
			return;
		}

		
		//Remove the oldest element every 10 screens to free memory
		if( imageHashMap.size() >= 10) 
		{
			imageHashMap.remove( imageHashMap.keySet().toArray()[0] );
		}

		//Draw everything to a buffer. First things that are drawn appear behind other things.
		//Begin drawing the  actual background and save it in memory
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
    		imageHashMap.put( currentSeed, buffer );
        }
        catch (OutOfMemoryError e) 
        {
        	//Ouch, we ran out of memory, the least we can do now is prevent it from crashing
        	Log.warning( e.toString() );
        	Profiler.memoryReport();
        	Runtime.getRuntime().runFinalization();
        }
        
		Profiler.end("Generating Background");
	}

	public Planet generatePlanet( long seed ) {
		Random rand = new Random(seed);	
		Planet planet = planetList.get( rand.nextInt(planetList.size()) );
		
		planet.getSprite().reset();
		
		//Physics
		planet.enableCollision();
		planet.anchored = true;
		
		//Make it unique
		int planetSize = rand.nextInt(WIDTH/2) + WIDTH/8;
		planet.resizeObject(planetSize, planetSize);
		if( rand.nextBoolean() ) planet.getSprite().horizontalFlip();
		if( rand.nextBoolean() ) planet.getSprite().verticalFlip();
		planet.rotate( rand.nextInt(360) );

		//Center the planet position on the screen
		planet.pos.x = (WIDTH/2);
		planet.pos.y = (HEIGHT/2);
		planet.update();
		return planet;
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
			g.drawImage(stars.get(rand.nextInt(stars.size())), rand.nextInt(WIDTH), rand.nextInt(HEIGHT), null);
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
	
	private void loadPlanets(){
		File[] fileList = new File("data/planets").listFiles();
		
		//Load planets into memory
		planetList = new ArrayList<Object>();
		for( File f : fileList )
		{
			if( !f.isFile() ) continue;
			planetList.add( new Object( f.toString(), 0, 0 )) ;
			
		}
	}

	
	/**
	 * AE> Predrawing the stars and placing them in the static ArrayList stars
	 */
	private void loadStars() {
		//Load Stars into memory
		Random rand = new Random();
		stars = new ArrayList<BufferedImage>();
		for (int s = 1; s < 40; ++s)
		{
			for (int c = 0; c < 255; c += 16)
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
				
				stars.add(star);
				starGraph.dispose();
			}
		}
	}
				
	/**
	 * JJ> Draw the finished background to the Graphics specified in the parameter
	 * @param g
	 */
	public void draw(Graphics g){
		g.drawImage(imageHashMap.get(currentSeed), 0, 0, null);
	}
	
}
