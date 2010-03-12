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
package gameEngine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.VolatileImage;
import java.io.File;
import java.util.HashMap;

import javax.swing.ImageIcon;

import narwhal.Game;

public class Particle {	
	public static final int MAX_PARTICLES = 256;	
	private static HashMap<Integer, ImageIcon> particleMap;
	
	/**
	 * JJ> Loads all particle images into a hash map for later use
	 */
	static public void loadParticles() {
		File[] fileList = new File("data/particles").listFiles();
		
		//Make sure it is removed from memory
		if( particleMap != null ) particleMap.clear();
		
		//Load all particles into the hash map
		particleMap = new HashMap<Integer, ImageIcon>();
		for( File f : fileList )
		{
			if( !f.isFile() ) continue;
			ImageIcon loadGraphic = new ImageIcon( f.toString() );
			
			//Trim away the file extension (.jpg, .png, etc.) and the file path
			String fileName = f.toString();
			fileName = fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length()-4 );

			//Put the image into a hash map
			particleMap.put( fileName.hashCode(), loadGraphic );
		}
	}
	
	//Particle variables
	private int hashCode;				//Unique hash code to identify which image to draw
	private int time;					//How many frames it has to live
	private Vector pos;					//Position
	private boolean requestDelete;		//Remove me?
	private boolean onScreen;			//Was it on the screen this update?
	private VolatileImage memoryImg;
	
	private float alpha;
	private float alphaAdd;
	
	private float angle;
	private float angleAdd;
	
	private float size;
	private float sizeAdd;
	
	//JJ> Most simple form
	public Particle( Vector spawnPos, String hash, int lifeTime ) {
		init(spawnPos, hash.hashCode(), lifeTime, 1, 0, 0, 0, 1, 0);
	}
	
	//JJ> With alpha
	public Particle( Vector spawnPos, String hash, int lifeTime, float trans, float transAdd ) {
		init(spawnPos, hash.hashCode(), lifeTime, trans, transAdd, 0, 0, 1, 0);
	}

	//JJ> With alpha and rotation
	public Particle( Vector pos, String hash, int time, float alpha, float alphaAdd, 
			float angle, float angleAdd ) {
		init(pos, hash.hashCode(), time, alpha, alphaAdd, angle, angleAdd, 1, 0);
	}
	
	private void init( Vector setPos, int setHash, int setTime, float setAlpha, float setAlphaAdd, 
			float setAngle, float setAngleAdd, float setSize, float setSizeAdd ) {
		requestDelete = false;
		onScreen = true;
		pos = setPos;
		time = setTime;
		hashCode = setHash;
		alpha = setAlpha;
		alphaAdd = setAlphaAdd;
		angle = setAngle;
		angleAdd = setAngleAdd;
		size = setSize;
		sizeAdd = setSizeAdd;		
	}
	
	private void createMemoryImage(int width, int height){
		if( memoryImg == null || memoryImg.contentsLost() || width != memoryImg.getWidth()
				|| height != memoryImg.getHeight() )
		{
			memoryImg = Video.createVolatileImage(width, height, VolatileImage.TRANSLUCENT);
		}
	}
	
	/**
	 * JJ> Manually marks this particle for removal
	 */
	public void delete() {
		requestDelete = true;
	}
	
	/**
	 * @return true if this particle is marked for removal
	 */
	public boolean requestsDelete() {
		return requestDelete;
	}

	/**
	 * @return true if this particle is marked for removal
	 */
	public boolean isOnScreen() {
		return onScreen;
	}

	public void update() {

		//Don't update if we do not exist
		if(requestDelete) return;
		
		//Uh oh, we have an invalid hash code
		if( particleMap.get(hashCode) == null )
		{
			requestDelete = true;
			onScreen = false;
			Log.warning("Invalid particle spawn: " + hashCode);
			return;
		}

		//Update effects for next frame
		alpha += alphaAdd;
		angle += angleAdd;
		size  += sizeAdd;
		
		//Figure out if we are inside the screen or not
		onScreen = false;
		int w = (int) (particleMap.get(hashCode).getIconWidth()  * size);
		int h = (int) (particleMap.get(hashCode).getIconHeight() * size);
		int xPos = pos.getX() - this.pos.getX() - w/2;
		int yPos = pos.getY() - this.pos.getY() - h/2;
		onScreen = Game.isInScreen(new Rectangle( xPos, yPos, w, h ));
		
		//Only do image operations if the image is actually on the screen
		if( onScreen )
		{
			//Make sure the VolatileImage exists
			createMemoryImage(w, h);
			Graphics2D g = memoryImg.createGraphics();

			//Make it fast!
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
			g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
			g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
		   	g.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE );
		   	g.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
		   	
			//Clear the background
			g.setBackground(new Color(0,0,0,0));
			g.clearRect(0, 0, w, h);
 
	        //Do any alpha
			alpha = Math.min( 1.00f, Math.max(0.00f, alpha) );
			if(alpha < 1) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));  

			//Rotate to direction
			if( angle != 0 ) 
			{
		    	g.rotate(angle, w/2, h/2);
		    	g.drawImage(particleMap.get(hashCode).getImage(), (memoryImg.getWidth()-w)/2, (memoryImg.getHeight()-h)/2, w, h, null);
			}
	        
			//Draw it normally
			else g.drawImage(particleMap.get(hashCode).getImage(), 0,0, w, h, null);
			
			//All done!
			g.dispose();
		}
		
		//Mark particles for removal when their time is up or when alpha has made it invisible
		if(time > 0 && alpha != 0 && size > 0 ) time--;
		else requestDelete = true;		
	}
	
	public void draw(Graphics g, Vector pos) {
				
		//Only draw if inside the screen bounds
		if( !onScreen || memoryImg == null ) return;		
				
		int xPos = pos.getX() - this.pos.getX() - memoryImg.getWidth()/2;
		int yPos = pos.getY() - this.pos.getY() - memoryImg.getHeight()/2;
		g.drawImage( memoryImg, xPos, yPos, null );
	}
}
