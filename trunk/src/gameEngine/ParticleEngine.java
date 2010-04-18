package gameEngine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.ImageIcon;

import narwhal.AI;
import narwhal.Planet;
import narwhal.Spaceship;
import narwhal.Weapon;

public class ParticleEngine {
	private static final int MAX_PARTICLES = 512;
	private HashMap<String, ParticleTemplate> particleMap;
	private ArrayList<Particle> particleList;
	
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
			ParticleTemplate load;

			//Only load it if the image was loaded properly
			try 
			{
				load = new ParticleTemplate(fileName);
				particleMap.put( hash, load );
			} 
			catch (InvalidParticleException e) 
			{
				Log.warning("Loading particle: " + fileName + " - " + e);
			}			
		}
	}
		
	public void update( ArrayList<GameObject> entities, int universeSize ) {
		
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
			prt.update();
			
			//Collision detection between particles and GameObjects
			if( prt.canCollide )
			{
				for(int j = 0; j < entities.size(); j++ )
				{	
					GameObject object  = entities.get(j);

					if( prt.collidesWith(object) )
					{
						//Skip if no friendly fire
						if( !prt.template.friendlyFire && (object instanceof Spaceship) && prt.team.equals(((Spaceship)object).team) ) continue;
												
						//Damage them
						if( prt.weapon != null )
						{
							//Have we already hit them?
							if( prt.collisionList.contains(object) ) continue;

							//Do the collision
							object.damage( prt.weapon );
							prt.collisionList.add(object);
						}
						
						//TODO: disabled particle on object collision until we have implemented Mass
						//if( prt.template.physics ) prt.collision(object);
						
						//Die away if told to
						if( prt.template.collisionEnd && ( !prt.template.subAtomicParticle 
								|| object instanceof Planet ) ) 	prt.delete();
					}
				}
			}
			
			//Particle on particle collision
			if( prt.template.physics )
			{
				for( int j = i+1; j < particleList.size(); j++ )
				{
					Particle cPrt = particleList.get(j);
					if( !cPrt.template.physics ) continue;
					
					if( prt.collidesWith(cPrt) ) prt.collision(cPrt);
				}
			}
			
			// Quick implement of universe bounds
			float uniX = universeSize * Video.getScreenWidth();
			float uniY = universeSize * Video.getScreenHeight();
			
			if 		(prt.pos.x < 0) 	prt.pos.x = uniX + prt.pos.x;
			else if (prt.pos.x > uniX)  prt.pos.x %= uniX;
			
			if 		(prt.pos.y < 0) 	prt.pos.y = uniY + prt.pos.y;
			else if (prt.pos.y > uniY)  prt.pos.y %= uniY;
		}
	}
	
	public boolean spawnParticle(String name, Vector position , float facing, Physics spawner, Weapon damage) {
		
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
		particleList.add( new Particle(position, type, facing, spawner, damage) );
		return true;
	}

	public void render(Graphics2D g) {
		//Draw all particles
		for(int i = 0; i < particleList.size(); i++) particleList.get(i).draw(g);
	}
	
	/**
	 * JJ> This gets the number of particles currently in the list. It can also include
	 *     particles that are deleted and inactive but not yet removed this update.
	 * @return A Integer which holds the number of particles currently used.
	 */
	public int getParticleCount() {
		return particleList.size();
	}	
		
	/****************************************************************************************************/
	private final class ParticleTemplate {
		
		static final float RANDOM_ANGLE = Float.MIN_VALUE;
		
		//Particle variables
		private final ArrayList<ImageIcon> image;
		public final int time;					//How many frames it has to live
		public final float alpha;				//Transparency
		public final float alphaAdd;
		public final float angle;				//Rotation
		public final float angleAdd;
		public final float size;					//Size
		public final float sizeAdd;	
		public final boolean scaleToSpawner;	//Is as big the spawner initially
		public final float speed;				//Movement
		private final boolean attached;			//Attached to spawner?
		
		private final float facing;
		private final float facingAdd;
		
		public final boolean collisionEnd;		//End particle if it collides?
		public final boolean canCollide;
		public final boolean subAtomicParticle;	//Can it move through solid objects? (excluding planets)
		public final boolean physics;	        //Does this particle use the laws of physics?
		
		public final String particleEnd;		//Spawn particle if this one ends?
		public final int    multiEndSpawn;		//How many do we spawn?
		public final float  endFacingAdd;		//How much facing do we add for each one
		
		public final Sound soundEnd;
		public final Sound soundSpawn;

		public final boolean friendlyFire;

		public final float homing;
		
		public ParticleTemplate( String fileName ) throws InvalidParticleException {		
			
			//Temp variables set to default
			image = new ArrayList<ImageIcon>();
			int time = 1;
			
			float alpha = 1;
			float alphaAdd = 0;
			
			float angle = 0;
			float angleAdd = 0;
			
			float facing = 0;
			float facingAdd = 0;
			
			float size = 1;
			float sizeAdd = 0;
			boolean scaleToSpawner = false;
			
			Sound soundSpawn = null;
			Sound soundEnd = null;
			
			boolean attached = false;
			boolean canCollide = false;
			boolean collisionEnd = true;
			boolean friendlyFire = true;
			boolean subAtomicParticle = false;
			boolean physics = false;
			
			float homing = 0;
			float speed = 0;
			
			String particleEnd = null;
			int    multiEndSpawn = 1;
			float  endFacingAdd = 0;

			BufferedReader parse = new BufferedReader(
					new InputStreamReader(
					ResourceMananger.getInputStream(fileName)));
			
			//Parse the ship file
			while(true)
			{
				String line;
				try 
				{
					line = parse.readLine();
				} 
				catch (IOException e) 
				{
					throw new InvalidParticleException( e.toString() );
				}
				
				//Reached end of file
				if(line == null) break;
				
				//Ignore comments
				if( line.startsWith("//") || line.equals("") ) continue;
				
				//Ignore NONE values
				if(line.indexOf("NONE") != -1) continue;

				//Translate line into data
				if(line.startsWith("[IMAGE]:"))    	 		
				{
					//Make sure the file actually exist first.
					String path = "/data/particles/" + parse(line);
					if( !ResourceMananger.fileExists( path ) )
					{
						Log.warning("Loading particle: " + fileName + " - The specified image does not exist: " + path);
						continue;
					}
					
					//Try loading it
					ImageIcon load = new ImageIcon(ResourceMananger.getFilePath(path));	
					if( load.getIconWidth() <= 0 )
					{
						Log.warning("Loading particle: " + fileName + " - Failed loading the specified image! (" + path + ")");
						continue;
					}
					image.add(load);
				}
				else if(line.startsWith("[TIME]:"))  		time = Integer.parseInt(parse(line));
				else if(line.startsWith("[SPEED]:"))  		speed = Float.parseFloat(parse(line));
				else if(line.startsWith("[ATTACHED]:"))  	attached = Boolean.parseBoolean(parse(line));

				else if(line.startsWith("[SIZE]:"))  		size = Float.parseFloat(parse(line));
				else if(line.startsWith("[SIZE_ADD]:")) 	sizeAdd = Float.parseFloat(parse(line));
				else if(line.startsWith("[SCALE_TO_SPAWNER]:"))  scaleToSpawner = Boolean.parseBoolean(parse(line));
				
				else if(line.startsWith("[ALPHA]:"))  		alpha = Float.parseFloat(parse(line));
				else if(line.startsWith("[ALPHA_ADD]:"))	alphaAdd = Float.parseFloat(parse(line));
				
				else if(line.startsWith("[ROTATE]:"))  		
				{
					if(line.indexOf("RANDOM") == -1) angle = Float.parseFloat(parse(line));
					else							 angle = RANDOM_ANGLE;
				}
				else if(line.startsWith("[ROTATE_ADD]:")) 	angleAdd = Float.parseFloat(parse(line));
				
				else if(line.startsWith("[FACING]:"))  		
				{
					if(line.indexOf("RANDOM") == -1) facing = Float.parseFloat(parse(line));
					else							 facing = RANDOM_ANGLE;
				}
				else if(line.startsWith("[FACING_ADD]:")) 	facingAdd = Float.parseFloat(parse(line));
				
				else if(line.startsWith("[SOUND_SPAWN]:"))  soundSpawn = new Sound( parse(line) );
				else if(line.startsWith("[SOUND_END]:"))	soundEnd = new Sound( parse(line) );

				else if(line.startsWith("[CAN_COLLIDE]:"))  canCollide = Boolean.parseBoolean(parse(line));
				else if(line.startsWith("[COLLISION_END]:")) collisionEnd = Boolean.parseBoolean(parse(line));
				else if(line.startsWith("[FRIENDLY_FIRE]:")) friendlyFire = Boolean.parseBoolean(parse(line));
				else if(line.startsWith("[SUBATOMIC]:"))     subAtomicParticle = Boolean.parseBoolean(parse(line));
				else if(line.startsWith("[PHYSICS]:"))       physics = Boolean.parseBoolean(parse(line));

				else if(line.startsWith("[PARTICLE_END]:")) particleEnd = parse(line);
				else if(line.startsWith("[MULTISPAWN_END]:")) multiEndSpawn = Integer.parseInt(parse(line));
				else if(line.startsWith("[END_FACING_ADD]:")) endFacingAdd = Float.parseFloat(parse(line));

				else if(line.startsWith("[HOMING]:")) homing = Float.parseFloat( parse(line) );

				else Log.warning("Loading particle file ( "+ fileName +") unrecognized line - " + line);
			}
			if( image.size() == 0 ) throw new InvalidParticleException("Missing a '[IMAGE]:' line describing which image to load!");
			
			//Now set these values to the actual final values, these can now never be changed!
			this.time = time;
			this.speed = attached ? 0 : speed;
			this.attached = attached;
			this.homing = homing;
			
			this.alpha = alpha;
			this.alphaAdd = alphaAdd;
			
			this.angle = angle;
			this.angleAdd = angleAdd;
			
			this.facing = facing;
			this.facingAdd = facingAdd;
			
			this.size = size;
			this.sizeAdd = sizeAdd;	
			this.scaleToSpawner = scaleToSpawner;
			
			this.soundSpawn = soundSpawn;	
			this.soundEnd = soundEnd;
			
			this.friendlyFire = friendlyFire;
			this.collisionEnd = collisionEnd;
			this.canCollide = canCollide;
			this.subAtomicParticle = subAtomicParticle;
			this.physics = physics;
			
			this.particleEnd = particleEnd;
			this.multiEndSpawn = multiEndSpawn;
			this.endFacingAdd = endFacingAdd;			
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
	/****************************************************************************************************/
	
	/**
	 * JJ> Custom exception class that descends from Java's Exception class.
	 */
	public class InvalidParticleException extends Exception
	{
		private static final long serialVersionUID = 1L;
		  
		  // Default constructor - initializes instance variable to unknown
		  public InvalidParticleException()
		  {	  
		    super();
		  }
		  
		  // Constructor receives some kind of message that is saved in an instance variable.
		  public InvalidParticleException(String err)
		  {
		    super(err);
		  }
	}
		  	  	
	/****************************************************************************************************/
	private class Particle extends Physics {
		
		//Object functions
		private boolean requestDelete;		//Remove me?
		private boolean onScreen;			//Was it on the screen this update?
		public ArrayList<GameObject> collisionList;	//List of all GameObject we have damaged
		private ParticleTemplate template;
		
		private ImageIcon image;

		private Physics attached;			//Who is it attached to?
		private Spaceship homing;			//Who are we following?

		private String team;				//Who's side is it on?
		public Weapon weapon;
		
		private Physics spawner;					//Who spawned us?

		//Particle properties
		private int time;					//How many frames it has to live
		private float alpha;				//Transparency
		private float alphaAdd;
		
		private float angle;				//Rotation
		private float angleAdd;
		
		private float facing;				//Direction we are supposed to move
		
		private float size;					//Size
		private float sizeAdd;
		
		private float velocity;				//Movement
		private float facingAdd;
				
		
		private Particle( Vector spawnPos, ParticleTemplate template, float baseFacing, Physics spawner, Weapon damage ) {
			Random rand = new Random();
			float baseRotation = 0;
			
			//Default stuff
			this.spawner = spawner;
			this.template = template;
			requestDelete = false;
			onScreen = false;

			//Randomize our particle image from the list of images our template
			//has given us. This list could be 1 in size.
			image = template.image.get( rand.nextInt( template.image.size() ) );

			//These values can safely be copied now
			size = template.size;
			time = template.time;
			alpha = template.alpha;
			alphaAdd = template.alphaAdd;
			sizeAdd = template.sizeAdd;			
			weapon = damage;
			team = "NEUTRAL";

			//Set some values if we have a spawner
			if( spawner != null )
			{
				//We keep the same angle as the spawner
				if(spawner instanceof Particle)
					baseRotation = ((Particle)spawner).angle;
				else baseRotation = spawner.direction;
				
				//Get homing target if needed
				if( template.homing != 0 && spawner instanceof AI )
				{
					homing = ((AI)spawner).getHomingTarget( template.homing );
				}

				//Attached to the spawner?
				if( template.attached )
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
				
				//Scale this particle initial size to spawner's size
				if( template.scaleToSpawner )
				{
					size *= ((float)spawner.radius*2) / ((float)image.getIconWidth());
				}
				
				//We share team with our spawner
				if( spawner instanceof Spaceship )
				{
					Spaceship owner = (Spaceship)spawner;
					team = owner.team;
				}
				else if( spawner instanceof Particle )
				{
					Particle owner = (Particle)spawner;
					team = owner.team;
				}
			}
						
			//Randomize angle?
			if(template.angle == ParticleTemplate.RANDOM_ANGLE) 
			{
				float randAngle = rand.nextFloat() + rand.nextInt(4);
				angle = randAngle + baseRotation;
			}
			else angle = template.angle + baseRotation;
			angleAdd = template.angleAdd;

			//Randomize facing?
			if(template.facing == ParticleTemplate.RANDOM_ANGLE) 
			{
				float randFacing = rand.nextFloat() + rand.nextInt(4);
				facing = randFacing + baseFacing;
			}
			else facing = template.facing + baseFacing;
			facingAdd = template.facingAdd;
									
			//Physics stuff
			canCollide = template.canCollide;
			shape = Shape.CIRCLE;
			anchored = false;
			if(spawner == null) 	speed = new Vector();
			else					speed = spawner.getSpeed().clone();
			setRadius( image.getIconWidth()/4 );
			collisionList = new ArrayList<GameObject>();
			
			//Play spawn sound
			if( template.soundSpawn != null ) template.soundSpawn.play3D(pos, Video.getCameraPos());
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
			if(time <= 0 || alpha <= 0 || size <= 0 )
			{
				this.delete();
				return;
			}
			
			
			//Are we homing in on a target?
			facing += facingAdd;
			if( homing != null )
			{
				float heading = homing.getPosCentre().minus(pos).getAngle() - facing;
				if 		(heading > Math.PI)  heading = -( ((float)Math.PI*2) - heading);
				else if (heading < -Math.PI) heading = ( ((float)Math.PI*2) + heading);
				facing += heading * (velocity/200);
				
				//TODO: now we always face towards the homed one, this might not be what we want
				angle = facing;
			}
			facing %= 2 * Math.PI;
				
			//Movement
			Vector move = new Vector(velocity, facing, true);
			move.add(getSpeed());
			pos.add(move);
			
			//Figure out if we are inside the screen or not
			onScreen = Video.isInFrame( this.pos, new Vector() );
		}
		
		/**
		 * JJ> This performs all rendering operations for a single particle.
		 * @param g Which Graphics2D object to do the rendering to
		 */
		final public void draw(Graphics2D g) {

			//Only draw if it is okay to draw
			if( !onScreen  ) return;
			g = (Graphics2D)g.create();

			//We make particles as fast as possible
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
			g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
		   	g.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
			g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
		   	g.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE );

			//Calculate new width and height
			int w = (int) ( image.getIconWidth() * size);
			int h = (int) ( image.getIconHeight() * size);

			//Calculate position
			Vector offset = Video.getCameraPos();
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
			
			//Draw collision circle
			if( Configuration.debugMode )
			{
				g.setColor(Color.YELLOW);
				Vector drawPos = pos.minus(new Vector(radius, radius)).minus(offset);
				g.drawOval( drawPos.getX(), drawPos.getY(), (int)radius*2, (int)radius*2);
			}
		}

		/**
		 * JJ> Marks this particle for removal and does any end stuff it needs to do (such as
		 *     spawning other particles or playing sound) and then frees any resources used.
		 */
		public void delete() {
			requestDelete = true;
			onScreen = false;
			
			//Play end sound
			if( template.soundEnd != null )template. soundEnd.play3D( this.pos, Video.getCameraPos() );
			
			//Spawn any end particle
			if( template.particleEnd != null )
			{
				float prtFacing = this.facing;
				for(int i = 0; i < template.multiEndSpawn; i++)
				{
					spawnParticle( template.particleEnd, this.pos, prtFacing, this.spawner, weapon );
					prtFacing += template.endFacingAdd;
				}
			}
		}
	}
	
}
