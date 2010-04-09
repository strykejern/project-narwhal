package gameEngine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

public class ParticleEngine {
	private static final int MAX_PARTICLES = 512;
	private HashMap<String, ParticleTemplate> particleMap;
	private ArrayList<Particle> particleList;
	private Camera viewPort;
	
	/**
	 * JJ> Loads all particle images into a hash map for later use
	 */
	public ParticleEngine() {				
		String[] fileList = ResourceMananger.getFileList("/data/particles/");
		
		//Ready array lists
		particleMap = new HashMap<String, ParticleTemplate>();
		particleList = new ArrayList<Particle>();

		//Load all particles into the hash map
		for( String fileName : fileList )
		{	
			//Only load files that end with .prt
			if(!fileName.endsWith(".prt")) continue;
			String hash = fileName.substring( fileName.lastIndexOf("/")+1);
			ParticleTemplate load = new ParticleTemplate(fileName);
			
			//Only load it if the image was loaded properly
			if(load.getImageWidth() > 0) particleMap.put( hash, load );
		}
	}
	
	public void setRenderCamera(Camera viewPort) {
		this.viewPort = viewPort;
		
		//Spit out a warning if needed
		if( viewPort == null ) 
			Log.warning("Particle engine has a null pointer to the Camera! No partilces will be drawn.");
		else viewPort.setParticleEngine(this);
	}
	
	public void update() {
		
		//Update particle effects
		for( int i = 0; i < particleList.size(); i++ )
			if ( !particleList.get(i).requestsDelete() ) 
				particleList.get(i).update();
			else 
				particleList.remove(i--);		
	}
	
	public boolean spawnParticle(String name, Vector position , float rotation) {
		
		//Limit number of particles
		if( particleList.size() > MAX_PARTICLES ) return false;
		
		//Check for invalid spawns
		ParticleTemplate type = particleMap.get(name);
		if( type == null )
		{
			Log.warning("Invalid particle spawn: " + name);
			return false;
		}
		
		//Nope everything went well, add it to the active list!
		particleList.add( new Particle(position, type, rotation) );
		return true;
	}

	public void render(Graphics2D g) {
		
		//Draw all particles
		for( Particle prt : particleList ) prt.draw(g);
	}
	
	public int getParticleCount() {
		return particleList.size();
	}	
		
	private class ParticleTemplate {
		
		//Particle variables
		private ImageIcon image;
		public final int time;					//How many frames it has to live
		public final float alpha;				//Transparency
		public final float alphaAdd;
		public final float angle;				//Rotation
		public final float angleAdd;
		public final float size;					//Size
		public final float sizeAdd;	
		public final float speed;				//Movement
		private boolean attached;			//Attached to spawner?

		int getImageWidth(){
			return image.getIconWidth();
		}
		
		int getImageHeight(){
			return image.getIconHeight();
		}
		
		public ParticleTemplate( String fileName ) {		
			
			//Temp variables set to default
			int time = 1;
			float alpha = 1;
			float alphaAdd = 0;
			float angle = 0;
			float angleAdd = 0;
			float size = 1;
			float sizeAdd = 0;	
			float speed = 0;
			attached = false;

			try
			{
				BufferedReader parse = new BufferedReader(
						new InputStreamReader(
						ResourceMananger.getInputStream(fileName)));
				
				//Parse the ship file
				while(true)
				{
					String line = parse.readLine();
					
					//Reached end of file
					if(line == null) break;
					
					//Ignore comments
					if( line.startsWith("//") || line.equals("") ) continue;
					
					//Translate line into data
					if(line.startsWith("[IMAGE]:"))    	 		
					{
						image = new ImageIcon(ResourceMananger.getFilePath("/data/particles/" + parse(line)));
						if( image.getIconWidth() <= 0 ) Log.warning("Failed loading the specified image! (" + "/data/particles/" + parse(line) + ")");
					}
					else if(line.startsWith("[TIME]:"))  		time = Integer.parseInt(parse(line));
					else if(line.startsWith("[SIZE]:"))  		size = Float.parseFloat(parse(line));
					else if(line.startsWith("[SIZE_ADD]:")) 	sizeAdd = Float.parseFloat(parse(line));
					else if(line.startsWith("[ALPHA]:"))  		alpha = Float.parseFloat(parse(line));
					else if(line.startsWith("[ALPHA_ADD]:"))	alphaAdd = Float.parseFloat(parse(line));
					else if(line.startsWith("[ROTATE]:"))  		angle = Float.parseFloat(parse(line));
					else if(line.startsWith("[ROTATE_ADD]:")) 	angleAdd = Float.parseFloat(parse(line));
					else if(line.startsWith("[SPEED]:"))  		speed = Float.parseFloat(parse(line));
					else if(line.startsWith("[ATTACHED]:"))  	attached = Boolean.parseBoolean(parse(line));
					else Log.warning("Loading particle file ( "+ fileName +") unrecognized line - " + line);
				}
				if(image == null) throw new Exception("Missing a '[IMAGE]:' line describing which image to load!");
				else if( image.getIconWidth() <= 0 ) throw new Exception("Failed loading the specified image!");
			}
			catch( Exception e )
			{
				//Something went wrong
				Log.warning("Loading particle (" + fileName + ") - " + e);
			}
			
			//Now set these values to the actual final values, these can now never be changed!
			this.time = time;
			this.alpha = alpha;
			this.alphaAdd = alphaAdd;
			this.angle = angle;
			this.angleAdd = angleAdd;
			this.size = size;
			this.sizeAdd = sizeAdd;	
			this.speed = attached ? 0 : speed;
			
			Log.message("Loaded particle: " + fileName);
		}
		
		/**
		 * JJ> This is simply to make parsing easier. Gets whatever is behind the colon and trims all
		 *     whitespace before and after the text.
		 * @param line The String to parse
		 * @return The parsed String
		 */
		private String parse(String line) {
			return line.substring(line.indexOf(':')+1).trim();
		}		
	}
	
	private class Particle {
		
		//Object functions
		private boolean requestDelete;		//Remove me?
		private boolean onScreen;			//Was it on the screen this update?
		private boolean rendering;			//Has it finished rendering?
		private VolatileImage memoryImg;	//The actual image in volatile memory
		private ParticleTemplate prt;
			
		//Particle properties
		private int time;					//How many frames it has to live
		private Vector pos;					//Position
		
		private float alpha;				//Transparency
		private float alphaAdd;
		
		private float angle;				//Rotation
		private float angleAdd;
		
		private float size;					//Size
		private float sizeAdd;
		
		private Vector speed;				//Movement
		
		public Particle( Vector spawnPos, ParticleTemplate template, float rotation ) {
			
			requestDelete = false;
			onScreen = false;
			rendering = false;
			
			prt = template;
			pos = spawnPos;
			time = template.time;
			alpha = template.alpha;
			alphaAdd = template.alphaAdd;
			angle = template.angle + rotation;
			angleAdd = template.angleAdd;
			size = template.size;
			sizeAdd = template.sizeAdd;
			speed = new Vector();		//TODO
		}
			
		/**
		 * JJ> Allocates a image in the hardware memory
		 * @param width width of the image
		 * @param height height of the image
		 * @return the Graphics2D for the new image, ready to be drawn on
		 */
		private Graphics2D createMemoryImage(int width, int height) {
			
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
		 * @return true if this particle is marked for removal
		 */
		public boolean requestsDelete() {
			return requestDelete;
		}

		final public void update() {
			
			if( requestDelete ) return;
			
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
			if( viewPort != null )
				onScreen = viewPort.isInFrame( this.pos, new Vector() );
					
			//Only do rendering operations if the image is actually on the screen
			if( onScreen ) renderParticle();		
		}
		
		//TODO: probably should not do this in own thread
		private Thread renderParticle() {
			if(prt == null) 
				{
				Log.warning("ERROR");
				return null;
				}
			//We are already rendering the particle in an existing thread
			if( rendering ) return null;
		
			Thread render = new Thread()
			{
				public void run()
				{
					rendering = true;
					int w = (int) ( prt.getImageWidth() * size);
					int h = (int) ( prt.getImageHeight() * size);

					//Make sure the VolatileImage exists
					Graphics2D g = createMemoryImage(w, h);
									     
			        //Do any alpha
					alpha = Math.min( 1.00f, Math.max(0.00f, alpha) );
					if(alpha < 1) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		
					//Rotate to direction
/*					if( angle != 0 ) 
					{
						AffineTransform t = g.getTransform();
						t.rotate(angle, w/2, h/2);
						g.setTransform(t); 
					}*/
					
					//Now render it in memory
				     g.drawImage(prt.image.getImage(), 0,0, w, h, null);
				     
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
		
		final public void draw(Graphics2D g) {

			//Only draw if it is okay to draw
			if( !onScreen || rendering || memoryImg == null || viewPort == null ) return;		
			Vector offset = viewPort.getCameraPos();

			int xPos = pos.getX() - memoryImg.getWidth()/2 - offset.getX();
			int yPos = pos.getY() - memoryImg.getHeight()/2 - offset.getY();
			
			//Rotate before drawing
			AffineTransform xs = g.getTransform();
			xs.translate(xPos, yPos);
			xs.rotate(angle, memoryImg.getWidth()/2, memoryImg.getHeight()/2);
			g.drawImage( memoryImg, xs, null);
		}
	}
}
