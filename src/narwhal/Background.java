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

import gameEngine.Profiler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * JJ> This class generates a nice random background for us
 * @author Johan Jansen and Anders Eie
 *
 */
public class Background {
	private Map<Long, BufferedImage> imageHashMap = new HashMap<Long, BufferedImage>(20, 0.5f);
	private long randomSeed;
	private boolean initialized = false;
	private ArrayList<BufferedImage> stars;
	private ArrayList<Object> nebulaList;
	private ArrayList<Object> planetList;
	
	private int WIDTH, HEIGHT;
	
	/**
	 * JJ> Draw the entire scene on a BufferedImage so that we do not need to redraw and recalculate every
	 *     component every update. Instead we just draw the BufferedImage.
	 */
	public Background(int width, int height){
		this.WIDTH = width;
		this.HEIGHT = height;
		generate(0);
	}
	
	public void generate(long seed) {
		Profiler.begin("Generating Background");
		
		//Important, do first: generate the random seed
		Random rand = new Random(seed);
		randomSeed = seed;
		
		//Predraw stars
		if (!initialized) init();

		//Have we visited this place before? No need to continue then!
		if( imageHashMap.containsKey(seed) )
		{
			Profiler.end("Generating Background");
			return;
		}
		//Draw everything to a buffer. First things that are drawn appear behind other things.
        if( imageHashMap.size() == 20) imageHashMap.clear();									//Clear the entire hash map every 10 screens so we do not clutter memory        
        imageHashMap.put( seed,  new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB) ); 	//No need for alpha on the background			
        Graphics2D g = imageHashMap.get(seed).createGraphics();
		
        //I: Nebula (10% chance) or Black background (90%)
		if( rand.nextInt(100) <= 10 ) drawNebula(rand, g);
		else 
		{
			g.setColor(Color.black);
			g.fillRect(0, 0, WIDTH, HEIGHT);		
		}

		//II: Stars
		drawRandomStarfield(rand, g);
		
		//III: Planets (25% chance)
		if( rand.nextInt(100) <= 25 ) drawPlanet(rand, g);
		        		
		Profiler.end("Generating Background");
	}

	private void drawPlanet( Random rand, Graphics2D g ) {
		Object planet = planetList.get( rand.nextInt(planetList.size()) );
		planet.sprite.reset();
		
		//Make it unique
		int planetSize = rand.nextInt(WIDTH/2) + WIDTH/8;
		planet.sprite.resize(planetSize, planetSize);			
		if( rand.nextBoolean() ) planet.sprite.horizontalflip();
		if( rand.nextBoolean() ) planet.sprite.verticalflip();
		planet.sprite.setDirection( rand.nextInt(360) );

		//Center the planet position on the screen
		planet.pos.x = (WIDTH/2) - planet.sprite.getWidth()/2;
		planet.pos.y = (HEIGHT/2) - planet.sprite.getHeight()/2;
		
		g.drawImage(planet.sprite.toImage(), planet.pos.getX(), planet.pos.getY(), null);
	}
	
	//Draw a random nebula
	private void drawNebula(Random rand, Graphics2D g) {
		Object nebula = nebulaList.get( rand.nextInt(nebulaList.size()) ) ;
		nebula.sprite.reset();
		
		//Make it unique
		nebula.sprite.resize(WIDTH, HEIGHT);
		nebula.sprite.setAlpha( rand.nextFloat() );
		if( rand.nextBoolean() ) nebula.sprite.horizontalflip();
		if( rand.nextBoolean() ) nebula.sprite.verticalflip();

		//Center the planet position on the screen
		nebula.pos.x = (WIDTH/2) - nebula.sprite.getWidth()/2;
		nebula.pos.y = (HEIGHT/2) - nebula.sprite.getHeight()/2;
		
		//Now draw it
		g.drawImage(nebula.sprite.toImage(), nebula.pos.getX(), nebula.pos.getY(), null);
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
	
	private void init(){
		initialized = true;
		loadNebulas();
		loadPlanets();
		loadStars();
	}
	
	private void loadNebulas(){
		//Load nebulas into memory
		nebulaList = new ArrayList<Object>();
		for(int i = 0; i < 6; i++) nebulaList.add( new Object( "data/nebula/nebula" + i + ".jpg", 0, 0));
	}
	
	private void loadPlanets(){
		//Load all planets as well
		planetList = new ArrayList<Object>();
		planetList.add( new Object( "data/planet/planet.png", 0, 0) );
		planetList.add( new Object( "data/planet/venus.png", 0, 0) );
		planetList.add( new Object( "data/planet/exoplanet.png", 0, 0) );
		planetList.add( new Object( "data/planet/mineral.png", 0, 0) );
		planetList.add( new Object( "data/planet/jupiter.png", 0, 0) );	
	}

	/**
	 * AE> Predrawing the stars and placing them in the static ArrayList stars
	 */
	private void loadStars(){
		//Load Stars into memory
		Random rand = new Random();
		stars = new ArrayList<BufferedImage>();
		for (int s = 1; s < 30; ++s)
		{
			for (int c = 0; c < 255; c += 16)
			{
				BufferedImage star = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
				Graphics2D starGraph = star.createGraphics();
				
				Color col;
				int rareStar = rand.nextInt(20);				
				for (int k = 0; k < s; ++k)
				{
					if(rareStar == 1)		col = new Color(c, 0, 0, (int)(((float)k/(float)s)*255f));
					else					col = new Color(255, 255, c, (int)(((float)k/(float)s)*255f));
					
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
		g.drawImage(imageHashMap.get(randomSeed), 0, 0, null);
	}
	
}
