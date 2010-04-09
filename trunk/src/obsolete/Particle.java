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
import java.awt.RenderingHints;
import java.awt.image.VolatileImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.swing.ImageIcon;

private class Particle {
	
	//Object functions
	private boolean requestDelete;		//Remove me?
	private boolean onScreen;			//Was it on the screen this update?
	private boolean rendering;			//Has it finished rendering?
	private VolatileImage memoryImg;	//The actual image in volatile memory
	private ParticleTemplate prt;
	
	
	//Particle variables
	private int time;					//How many frames it has to live
	private Vector pos;					//Position
	
	private float alpha;				//Transparency
	private float alphaAdd;
	
	private float angle;				//Rotation
	private float angleAdd;
	
	private float size;					//Size
	private float sizeAdd;
	
	private Vector speed;				//Movement
	
	public Particle( Vector spawnPos, String hash ) {
		
		//Check for invalid spawns
		ParticleTemplate prt = particleMap.get(hash);
		if( prt == null )
		{
			requestDelete = true;
			onScreen = false;
			Log.warning("Invalid particle spawn: " + hash);
			return;
		}

		requestDelete = false;
		onScreen = false;
		rendering = false;
		
		pos = spawnPos;
		time = prt.time;
		alpha = prt.alpha;
		alphaAdd = prt.alphaAdd;
		angle = prt.angle;
		angleAdd = prt.alphaAdd;
		size = prt.size;
		sizeAdd = prt.sizeAdd;
		speed = new Vector();		//TODO
	}
		
	/**
	 * JJ> Allocates a image in the hardware memory
	 * @param width width of the image
	 * @param height height of the image
	 * @return the Graphics2D for the new image, ready to be drawn on
	 */
	private Graphics2D createMemoryImage(int width, int height){
		
		//Create the image buffer in memory if needed
		if( memoryImg == null || memoryImg.contentsLost() )
		{
			memoryImg = Video.createVolatileImage(width, height);
		}
		Graphics2D g = memoryImg.createGraphics();
		
		//We make particles as fast as possible
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
		g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
		g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
	   	g.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE );
	   	g.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );

	   	//Clear any existing pixels
		g.setBackground(new Color(0,0,0,0));
		g.clearRect(0, 0, memoryImg.getWidth(), memoryImg.getHeight());
		return g;
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
	 * @return true if this particle was in the camera view this update
	 */
	public boolean isOnScreen() {
		return onScreen;
	}

	public void update(Camera screen) {
		
		//Update effects for next frame
		alpha += alphaAdd;
		angle += angleAdd;
		size  += sizeAdd;
		angle %= 2 * Math.PI;
		
		//Mark particles for removal when their time is up or when alpha has made it invisible
		time--;
		if(time <= 0 || alpha <= 0 && size <= 0 )
		{
			if(memoryImg != null) memoryImg.flush();
			requestDelete = true;
			onScreen = false;
			return;
		}
		
		//Movement
		pos.add(speed);
		
		//Figure out if we are inside the screen or not
		onScreen = screen.isInFrame( this );
				
		//Only do rendering operations if the image is actually on the screen
		if( onScreen ) renderParticle();		
	}
	
	//TODO: probably should not do this in own thread
	private Thread renderParticle() {
		
		//We are already rendering the particle in an existing thread
		if( rendering ) return null;
	
		Thread render = new Thread()
		{
			public void run()
			{
				rendering = true;
				int baseWidth = prt.image.getIconWidth(); 
				int baseHeight = prt.image.getIconHeight();
				int w = (int) ( baseWidth * size);
				int h = (int) ( baseHeight * size);
				
				//Make sure the VolatileImage exists
				Graphics2D g = createMemoryImage(baseWidth, baseHeight);
						   	 
		        //Do any alpha
				alpha = Math.min( 1.00f, Math.max(0.00f, alpha) );
				if(alpha < 1) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));  
	
				//Rotate to direction
				if( angle != 0 ) 
				{
			    	g.rotate(angle, w/2, h/2);
			    	g.drawImage(prt.image.getImage(), (memoryImg.getWidth()-w)/2, (memoryImg.getHeight()-h)/2, w, h, null);
				}
		        
				//Draw it normally
				else g.drawImage(prt.image.getImage(), 0,0, w, h, null);
				
				//All done!
				g.dispose();
				rendering = false;
			}
		};
		
		render.setDaemon(true);
		render.start();
		render.setPriority( Thread.NORM_PRIORITY );
		return render;
	}
	
	public void draw(Graphics g, Vector offset) {

		//Only draw if it is okay to draw
		if( !onScreen || rendering || memoryImg == null ) return;		

		int xPos = pos.getX() - memoryImg.getWidth()/2 - offset.getX();
		int yPos = pos.getY() - memoryImg.getHeight()/2 - offset.getY();
		g.drawImage( memoryImg, xPos, yPos, null );
	}

	
	//JJ> Auto generated getters and setters
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public Vector getPos() {
		return pos;
	}

	public void setPos(Vector pos) {
		this.pos = pos;
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public float getAlphaAdd() {
		return alphaAdd;
	}

	public void setAlphaAdd(float alphaAdd) {
		this.alphaAdd = alphaAdd;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngleAdd() {
		return angleAdd;
	}

	public void setAngleAdd(float angleAdd) {
		this.angleAdd = angleAdd;
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public float getSizeAdd() {
		return sizeAdd;
	}

	public void setSizeAdd(float sizeAdd) {
		this.sizeAdd = sizeAdd;
	}

	public Vector getSpeed() {
		return speed;
	}

	public void setSpeed(Vector speed) {
		this.speed = speed;
	}
}
