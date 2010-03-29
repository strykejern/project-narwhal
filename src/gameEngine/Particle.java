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
import java.util.HashMap;

import javax.swing.ImageIcon;


public class Particle {	
	public static final int MAX_PARTICLES = 256;	
	private static HashMap<Integer, ImageIcon> particleMap;
	
	/**
	 * JJ> Loads all particle images into a hash map for later use
	 */
	static public void loadParticles() {
		String[] fileList = ResourceMananger.getFileList("/data/particles/");
		
		//Make sure it is removed from memory
		if( particleMap != null ) particleMap.clear();
		
		//Load all particles into the hash map
		particleMap = new HashMap<Integer, ImageIcon>();
		for( String fileName : fileList )
		{
			ImageIcon loadGraphic = new ImageIcon(ResourceMananger.getFilePath(fileName));
			
			//Trim away the file extension (.jpg, .png, etc.) and the file path
			fileName = fileName.substring(fileName.lastIndexOf('/')+1, fileName.length()-4 );
			
			//Put the image into a hash map
			particleMap.put( fileName.hashCode(), loadGraphic );
		}
	}
	
	//Object functions
	private int hashCode;				//Unique hash code to identify which image to draw
	private int time;					//How many frames it has to live
	private Vector pos;					//Position
	private boolean requestDelete;		//Remove me?
	private boolean onScreen;			//Was it on the screen this update?
	private boolean rendering;			//Has it finished rendering?
	private VolatileImage memoryImg;	//The actual image in volatile memory
	
	//Image effects
	private float alpha;
	private float alphaAdd;
	
	private float angle;
	private float angleAdd;
	
	private float size;
	private float sizeAdd;
	
	private Vector speed;
	
	/*
	 * JJ> Most simple form
	 */
	public Particle( Vector spawnPos, String hash, int lifeTime ) {
		init(spawnPos, hash.hashCode(), lifeTime, 1, 0, 0, 0, 1, 0, new Vector());
	}
	
	/*
	 * JJ> With alpha
	 */
	public Particle( Vector spawnPos, String hash, int lifeTime, float trans, float transAdd ) {
		init(spawnPos, hash.hashCode(), lifeTime, trans, transAdd, 0, 0, 1, 0, new Vector());
	}

	/*
	 * JJ> With alpha and rotation
	 */
	public Particle( Vector pos, String hash, int time, float alpha, float alphaAdd, 
			float angle, float angleAdd, Vector speed ) {
		init(pos, hash.hashCode(), time, alpha, alphaAdd, angle, angleAdd, 1, 0, speed);
	}
	
	/**
	 * JJ> Initialises all particle variables
	 */
	private void init( Vector setPos, int setHash, int setTime, float setAlpha, float setAlphaAdd, 
			float setAngle, float setAngleAdd, float setSize, float setSizeAdd, Vector setSpeed ) {
		requestDelete = false;
		onScreen = false;
		rendering = false;
		pos = setPos;
		time = setTime;
		hashCode = setHash;
		alpha = setAlpha;
		alphaAdd = setAlphaAdd;
		angle = setAngle;
		angleAdd = setAngleAdd;
		size = setSize;
		sizeAdd = setSizeAdd;
		speed = setSpeed;
		
		//Check for invalid spawns
		if( particleMap.get(hashCode) == null )
		{
			requestDelete = true;
			onScreen = false;
			Log.warning("Invalid particle spawn: " + setHash);
			return;
		}
	}
	
	
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
	 * @return true if this particle is marked for removal
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
	
	private Thread renderParticle() {
		
		//We are already rendering the particle in an existing thread
		if( rendering ) return null;
	
		Thread render = new Thread()
		{
			public void run()
			{
				rendering = true;
				int baseWidth = particleMap.get(hashCode).getIconWidth(); 
				int baseHeight = particleMap.get(hashCode).getIconHeight();
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
			    	g.drawImage(particleMap.get(hashCode).getImage(), (memoryImg.getWidth()-w)/2, (memoryImg.getHeight()-h)/2, w, h, null);
				}
		        
				//Draw it normally
				else g.drawImage(particleMap.get(hashCode).getImage(), 0,0, w, h, null);
				
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

		//Only draw if inside the screen bounds
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
