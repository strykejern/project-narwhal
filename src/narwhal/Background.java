package narwhal;

import java.awt.*;
import java.util.Random;

public class Background {
	private int[] x, y, size;
	Random rand = new Random();
	Object planet = null;
	Object nebula = null;
	
	public Background(int width, int height, int seed){
		
		//Important, do first: generate the random seed
		rand = new Random(seed);
		
		//Randomize the starfield
		int numberOfStars = 125 + rand.nextInt(250);
		x = new int[numberOfStars];
		y = new int[numberOfStars];
		size = new int[numberOfStars];
		for (int i = 0; i < numberOfStars; ++i)
		{
			x[i] = rand.nextInt(width);
			y[i] = rand.nextInt(height);
			
			size[i] = rand.nextInt(30)+1;
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
	
		//	nebula.sprite.setDirection( rand.nextInt(360) );
	
			//Center the planet position on the screen
			nebula.pos.x = 400 - nebula.sprite.getWidth()/2;
			nebula.pos.y = 300 - nebula.sprite.getHeight()/2;
		}

		//Do we want a planet as well? (25% chance)
		if( rand.nextInt(100) <= 25 )
		{
			String whichPlanet;
			
			//Randomize the planet
			switch( rand.nextInt(3) )
			{
				default: case 0: whichPlanet = "planet.png"; break;
				case 1: 		 whichPlanet = "venus.png"; break;
				case 2: 		 whichPlanet = "exoplanet.png"; break;
			}
			planet = new Object( "data/planet/" + whichPlanet, 0, 0);
			
			int size = rand.nextInt(400) + 100;
			planet.sprite.resize(size, size);
			
			if( rand.nextBoolean() ) planet.sprite.horizontalflip();
			if( rand.nextBoolean() ) planet.sprite.verticalflip();
	
			planet.sprite.setDirection( rand.nextInt(360) );
	
			//Center the planet position on the screen
			planet.pos.x = 400 - planet.sprite.getWidth()/2;
			planet.pos.y = 300 - planet.sprite.getHeight()/2;
		}
		
	}
	
	public void update(){
		
	}
	
	public void draw(Graphics g){
		
		//I: Black background
		g.setColor(Color.black);
		g.fillRect(0, 0, 800, 600);
		
		//II: Draw any nebula
		if( nebula != null )
		{
			g.drawImage(nebula.sprite.toImage(), nebula.pos.getX(), nebula.pos.getY(), null);
		}
	
		//III: Draw each star
		for (int i = 0; i < size.length; ++i)
		{
			Color col = new Color(1f, 1f, 1f, (float)((float)(0.5f)/(float)(size[i]+1)));
			for (int k = 0; k < size[i]; ++k)
			{
				g.setColor(col);
				g.fillOval(x[i]+k, y[i]+k, size[i]-(2*k), size[i]-(2*k));
				col = new Color(1f, 1f, ((float)i/(float)size.length), (float)((float)(k+1)/(float)(size[i]+1)));
			}
			
			/*
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(x[i], y[i], x[i]+size[i], y[i]+size[i]);
			g.drawLine(x[i]+size[i], y[i], x[i], y[i]+size[i]);
			g.drawLine(x[i]+(size[i]/2), y[i], x[i]+(size[i]/2), y[i]+size[i]);
			g.drawLine(x[i], y[i]+(size[i]/2), x[i]+size[i], y[i]+(size[i]/2));
			*/
		}

		//IV: Draw any planets
		if( planet != null )
		{
			g.drawImage(planet.sprite.toImage(), planet.pos.getX(), planet.pos.getY(), null);
		}

	}
}
