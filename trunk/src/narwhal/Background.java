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
	private static Map<Long, BufferedImage> imageHashMap = new HashMap<Long, BufferedImage>(20, 0.5f);
	private static long randomSeed;
	private static boolean initialized = false;
	private static ArrayList<BufferedImage> stars;
	
	/**
	 * JJ> Draw the entire scene on a BufferedImage so that we do not need to redraw and recalculate every
	 *     component every update. Instead we just draw the BufferedImage.
	 */
	public Background(int width, int height, long seed){
		if (!initialized) init(); //Predraw stars
		
		Object nebula = null;
		Object planet = null;
		Random rand = new Random();
		int[] x, y, type;
		
		//Keep track of how much processing time this function uses
		Profiler.begin("Generating Background");
		
		//PART 1: Initialization
		//Important, do first: generate the random seed
		rand = new Random(seed);
		randomSeed = seed;
		
		//Have we visited this place before? No need to continue then!
		if( imageHashMap.containsKey(seed) )
		{
			Profiler.end("Generating Background");
			return;
		}
		
		//PART 2: Randomize the elements and effects
		
		//Randomize the starfield
		int numberOfStars = 125 + rand.nextInt(250);
		x 	 = new int[numberOfStars];
		y 	 = new int[numberOfStars];
		type = new int[numberOfStars];
		for (int i = 0; i < numberOfStars; ++i)
		{
			x[i] = rand.nextInt(width);
			y[i] = rand.nextInt(height);
			
			type[i] = rand.nextInt(stars.size());
		}

		//Are we inside a nebula? (10% chance)
		if( rand.nextInt(100) <= 10 )
		{
			//Randomize the nebula
			nebula = new Object( "data/nebula/nebula" + rand.nextInt(6) + ".jpg", 0, 0);
			nebula.sprite.resize(800, 600);
			nebula.sprite.setAlpha( rand.nextFloat() );
			
			if( rand.nextBoolean() ) nebula.sprite.horizontalflip();
			if( rand.nextBoolean() ) nebula.sprite.verticalflip();
	
		    //nebula.sprite.setDirection( rand.nextInt(360) );
	
			//Center the planet position on the screen
			nebula.pos.x = 400 - nebula.sprite.getWidth()/2;
			nebula.pos.y = 300 - nebula.sprite.getHeight()/2;
		}

		//Do we want a planet as well? (25% chance)
		if( rand.nextInt(100) <= 25 )
		{
			String whichPlanet;
			
			//Randomize the planet
			switch( rand.nextInt(5) )
			{
				default: case 0: whichPlanet = "planet.png"; break;
				case 1: 		 whichPlanet = "venus.png"; break;
				case 2: 		 whichPlanet = "exoplanet.png"; break;
				case 3: 		 whichPlanet = "mineral.png"; break;
				case 4: 		 whichPlanet = "jupiter.png"; break;
			}
			planet = new Object( "data/planet/" + whichPlanet, 0, 0);
			
			int planetSize = rand.nextInt(400) + 100;
			planet.sprite.resize(planetSize, planetSize);
			
			if( rand.nextBoolean() ) planet.sprite.horizontalflip();
			if( rand.nextBoolean() ) planet.sprite.verticalflip();
	
			planet.sprite.setDirection( rand.nextInt(360) );
	
			//Center the planet position on the screen
			planet.pos.x = 400 - planet.sprite.getWidth()/2;
			planet.pos.y = 300 - planet.sprite.getHeight()/2;
		}

		//PART 3: Draw everything to a buffer. First things that are drawn appear behind other things.
        if( imageHashMap.size() == 20) imageHashMap.clear();									//Clear the entire hash map every 10 screens so we do not clutter memory        
        imageHashMap.put( seed,  new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB) ); 	//No need for alpha on the background			
        Graphics2D g = imageHashMap.get(seed).createGraphics();
    	
        //Buff up the gfx =)
/*        g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
    	g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
       	g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
*/
       	g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
    	g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

       	//I: Black background
		g.setColor(Color.black);
		g.fillRect(0, 0, 800, 600);		
		
		//II: Draw any nebula
		if( nebula != null )
		{
			g.drawImage(nebula.sprite.toImage(), nebula.pos.getX(), nebula.pos.getY(), null);
		}
	
		//III: Draw each star						
		for (int i = 0; i < type.length; ++i)
		{
			g.drawImage(stars.get(type[i]), x[i], y[i], null);
		}
		
		//IV: Draw any planets
		if( planet != null )
		{
			g.drawImage(planet.sprite.toImage(), planet.pos.getX(), planet.pos.getY(), null);
		}				
		Profiler.end("Generating Background");
	}
	
	private void init(){
		Profiler.begin("Predrawing all stars");
		initialized = true;
		stars = new ArrayList<BufferedImage>();
		for (int s = 1; s < 30; ++s)
		{
			for (int c = 0; c < 255; c += 16)
			{
				BufferedImage star = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
				Graphics2D starGraph = star.createGraphics();
				
				Color col;
				for (int k = 0; k < s; ++k)
				{
					col = new Color(255, 255, c, (int)(((float)k/(float)s)*255f));
					starGraph.setColor(col);
					starGraph.fillOval(k, k, s-(2*k), s-(2*k));
				}
				
				stars.add(star);
				starGraph.dispose();
			}
		}
		Profiler.end("Predrawing all stars");
	}
			
	/**
	 * JJ> Draw the finished background to the Graphics specified in the parameter
	 * @param g
	 */
	public void draw(Graphics g){
		g.drawImage(imageHashMap.get(randomSeed), 0, 0, null);
	}
	
}
