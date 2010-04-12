package gameEngine;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import narwhal.Spaceship;
import narwhal.Weapon;

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
			if(load.image.getIconHeight() > 0) particleMap.put( hash, load );
		}
	}
	
	public void setRenderCamera(Camera viewPort) {
		this.viewPort = viewPort;
		
		//Spit out a warning if needed
		if( viewPort == null ) 
			Log.warning("Particle engine has a null pointer to the Camera! No partilces will be drawn.");
		else viewPort.setParticleEngine(this);
	}
	
	public void update(ArrayList<GameObject> entities) {
		
		//Update particle effects
		for( int i = 0; i < particleList.size(); i++ )
		{
			Particle prt = particleList.get(i);
			
			//Remove unused particles
			if( prt.requestsDelete() )
			{
				particleList.remove(i--);
				continue;
			}
			
			//Update this particle
			particleList.get(i).update();
			
			//Collision detection between particles and GameObjects
			for(int j = 0; j < entities.size(); j++ )
			{	
				GameObject object  = entities.get(j);
				if( prt.collidesWith(object) )
				{
					prt.delete();
					if( object instanceof Spaceship )
					{
						Spaceship them = (Spaceship)object;
						them.damage( new Weapon("lasercannon.wpn") );
					}
				}
			}

		}
	}
	
	public boolean spawnParticle(String name, Vector position , float rotation, GameObject spawner) {
		
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
		particleList.add( new Particle(position, type, rotation, spawner) );
		return true;
	}

	public void render(Graphics2D g) {
		
		//Draw all particles
		for( Particle prt : particleList ) prt.draw(g);
	}
	
	public int getParticleCount() {
		return particleList.size();
	}	
		
	private final class ParticleTemplate {
		
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
		public boolean collisionEnd;		//End particle if it collides?
		
		public final String particleEnd;
		public final Sound soundEnd;
		public final Sound soundSpawn;
		
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
			Sound soundSpawn = null;
			Sound soundEnd = null;
			String particleEnd = null;
			attached = false;
			collisionEnd = true;
			
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
					
					//Ignore NONE values
					if(line.indexOf("NONE") != -1) continue;

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
					else if(line.startsWith("[SOUND_SPAWN]:"))  soundSpawn = Sound.loadSound( parse(line) );
					else if(line.startsWith("[SOUND_END]:"))	soundEnd = Sound.loadSound( parse(line) );
					else if(line.startsWith("[PARTICLE_END]:")) particleEnd = parse(line);
					else if(line.startsWith("[COLLISION_END]:")) collisionEnd = Boolean.parseBoolean(parse(line));
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
			this.soundSpawn = soundSpawn;	
			this.soundEnd = soundEnd;
			this.particleEnd = particleEnd;
			
			Log.message("Loaded particle: " + fileName);
		}
		
		/**
		 * JJ> This is simply to make parsing easier. Gets whatever is behind the colon and trims all
		 *     whitespace before and after the text.
		 * @param line The String to parse
		 * @return The parsed String
		 */
		private String parse(String line) {			
			//Return the trimmed value
			return line.substring(line.indexOf(':')+1).trim();
		}
	}
	
	private class Particle extends Physics {
		
		//Object functions
		private boolean requestDelete;		//Remove me?
		private boolean onScreen;			//Was it on the screen this update?
		
		private ImageIcon image;
			
		private String particleEnd;			//What particle to spawn on end
		private Sound soundEnd;				//What sound to play on end
		private boolean collisionEnd;
		private Physics attached;
		
		//Particle properties
		private int time;					//How many frames it has to live
		private float alpha;				//Transparency
		private float alphaAdd;
		
		private float angle;				//Rotation
		private float angleAdd;
		
		private float size;					//Size
		private float sizeAdd;
		
		private float velocity;				//Movement
		
		
		private Particle( Vector spawnPos, ParticleTemplate template, float rotation, Physics spawner ) {
			
			requestDelete = false;
			onScreen = false;
			
			//Is this particle attached to a specific position?
			if( template.attached && spawner != null )	
			{
				attached = spawner;
				pos = spawner.pos;
				velocity = 0;
			}
			else
			{
				attached = null;
				pos = spawnPos.clone();
				velocity = template.speed;
			}
			
			image = template.image;			
			time = template.time;
			alpha = template.alpha;
			alphaAdd = template.alphaAdd;
			angle = template.angle + rotation;
			angleAdd = template.angleAdd;
			size = template.size;
			sizeAdd = template.sizeAdd;
			particleEnd = template.particleEnd;
			
			//Prepare end sound
			soundEnd = template.soundEnd;
			
			//Play spawn sound
			if( template.soundSpawn != null ) template.soundSpawn.play();
			
			//Physics stuff
			canCollide = template.collisionEnd;
			shape = Shape.CIRCLE;
			anchored = false;
			if(spawner == null) 	speed = new Vector();
			else					speed = spawner.speed.clone();
			setRadius( image.getIconWidth()/4 );
		}
		
		/**
		 * @return true if this particle is marked for removal
		 */
		public boolean requestsDelete() {
			return requestDelete;
		}

		/**
		 * JJ> Keeps a particle up to date with movement, rotation, etc.
		 */
		final public void update() {
			
			if( requestDelete ) return;
			
			if( attached != null )
			{
				pos = attached.pos.plus(new Vector(attached.radius, attached.radius));
			}
			
			//Update effects for next frame
			alpha = Math.min( 1.00f, Math.max(0.00f, alpha+alphaAdd) );
			angle += angleAdd;
			size  += sizeAdd;
			angle %= 2 * Math.PI;

			//Mark particles for removal when their time is up or when alpha has made it invisible
			time--;
			if(time <= 0 || alpha <= 0 && size <= 0 )
			{
				this.delete();
				return;
			}
			
			//Movement
			Vector move = new Vector(velocity, angle, true);
			move.add(speed);
			pos.add(move);
			
			//Figure out if we are inside the screen or not
			if( viewPort != null ) onScreen = viewPort.isInFrame( this.pos, new Vector() );
		}
		
		/**
		 * JJ> This performs all rendering operations for a single particle.
		 * @param g Which Graphics2D object to do the rendering to
		 */
		final public void draw(Graphics2D g) {

			//Only draw if it is okay to draw
			if( !onScreen || viewPort == null ) return;
			g = (Graphics2D)g.create();

			//We make particles as fast as possible
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
			g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
			g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
		   	g.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE );
		   	g.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );

			//Calculate new width and height
			int w = (int) ( image.getIconWidth() * size);
			int h = (int) ( image.getIconHeight() * size);

			//Calculate position
			Vector offset = viewPort.getCameraPos();
			int xPos = pos.getX() - w/2 - offset.getX();
			int yPos = pos.getY() - h/2 - offset.getY();
			
	        //Do any alpha
			if(alpha < 1) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

			//Rotate before drawing
			AffineTransform xs = g.getTransform(); 
			xs.translate(xPos, yPos);
			xs.rotate(angle, w/2, h/2);
			xs.scale(size, size);
			
			//Now draw it to screen after doing all render operations
			g.drawImage( image.getImage(), xs, null);			
		}

		/**
		 * JJ> Marks this particle for removal and does any end stuff it needs to do (such as
		 *     spawning other particles or playing sound) and then frees any resources used.
		 */
		public void delete() {
			requestDelete = true;
			onScreen = false;
			
			//Play end sound
			if( soundEnd != null ) soundEnd.play();
			
			//Spawn any end particle
			if(particleEnd != null) spawnParticle( particleEnd, this.pos, this.angle, null );
		}
	}
}
