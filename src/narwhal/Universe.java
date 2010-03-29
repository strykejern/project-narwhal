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
public class Universe {
	private int universeSize;
	private Vector[][] bgPos;
	private ArrayList<Image2D> nebulaList;
	private ImageIcon[] stars;
	private Image[][] universe;
	
	/**
	 * JJ> Draw the entire scene on a BufferedImage so that we do not need to redraw and recalculate every
	 *     component every update. Instead we just draw the BufferedImage.
	 */
	public Universe(int size, long seed){
		Profiler.begin("Initializing background");
		loadNebulas();
		loadStars();
		
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
		g.drawImage( nebula.toImage(), 0, 0, null);
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
			g.drawImage(stars[rand.nextInt(stars.length)].getImage(), rand.nextInt(Video.getScreenWidth()), rand.nextInt(Video.getScreenHeight()), null);
		}		
	}
		
	private void loadNebulas(){
		String[] fileList = ResourceMananger.getFileList("/data/nebula/");

		//Load nebulas into memory
		nebulaList = new ArrayList<Image2D>();
		for( String fileName : fileList )
		{
			Image2D load = new Image2D(fileName, Video.getScreenWidth(), Video.getScreenHeight());
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
			VolatileImage star = Video.createVolatileImage(s, s);
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
				
				//TODO: bad! it seems 55*256 elements are added to this ArrayList
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
		return new Vector( universeSize * Video.getScreenWidth(), universeSize * Video.getScreenHeight() );
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
		boolean debug = false;
		
		if(!debug) return;
				
		final int SCREEN_X = Video.getScreenWidth();			//Screen width
		final int SCREEN_Y = Video.getScreenHeight();		//Screen height
		
		//Make rectangles yellow
		g.setColor(Color.YELLOW);
		
		//TODO: Draw entire grid... this can be optimized
		for(int i = 0; i < universeSize; i++)
			for(int j = 0; j < universeSize; j++)
			{
				g.drawRect(i*SCREEN_X + pos.getX(), j*SCREEN_Y + pos.getY(), SCREEN_X, SCREEN_Y);
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
					Image buffer = Video.createVolatileImage(Video.getScreenWidth(), Video.getScreenHeight());
					
					/*if( Video.getQualityMode() == VideoQuality.VIDEO_HIGH )  
						buffer = new BufferedImage(Video.getScreenWidth(), Video.getScreenHeight(), BufferedImage.TYPE_INT_ARGB);		
					else 							   
						buffer = new BufferedImage(Video.getScreenWidth(), Video.getScreenHeight(), BufferedImage.TYPE_USHORT_555_RGB);
		    		*/
					
		    		Graphics2D g = (Graphics2D)buffer.getGraphics();
		        	
		            //I: Nebula (10% chance) or Black background (90%)
	    			g.setColor(Color.BLACK);
	    			g.fillRect(0, 0, Video.getScreenWidth(), Video.getScreenHeight());
	    			if( rand.nextInt(100) <= 10 ) drawNebula(rand, g);
	
		    		//II: Stars
		    		drawRandomStarfield(rand, g);
		    				    		
		    		universe[i][j] = buffer;
		    		bgPos[i][j] = new Vector(i*Video.getScreenWidth(), j*Video.getScreenHeight());

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

		//Make sure everything is freed from memory
		stars = null;
		for( Image2D img : nebulaList )
		{
			img.dispose();
			img = null;
		}
		nebulaList.clear();
		nebulaList = null;		
	}

	public void drawBackground(Graphics2D g, Vector position) {

		Vector pos = position.clone();
		for(int i = 0; i < universeSize; i++)
			for(int j = 0; j < universeSize; j++)
			{
				int x = bgPos[i][j].getX()-pos.getX();
				int y = bgPos[i][j].getY()-pos.getY();
				g.drawImage( universe[i][j], x, y, null );
			}	
	}
}
