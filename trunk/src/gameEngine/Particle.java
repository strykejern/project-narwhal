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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;

import narwhal.AI;
import narwhal.Spaceship;
import narwhal.Weapon;

class Particle extends Physics {
	
	//Object functions
	private boolean requestDelete;		//Remove me?
	private boolean onScreen;			//Was it on the screen this update?
	private ParticleTemplate template;
	public ArrayList<GameObject> collisionList;	//List of all GameObject we have damaged
	
	private ImageIcon image;

	private Physics attached;			//Who is it attached to?
	private Spaceship homing;			//Who are we following?

	public String team;						//Who's side is it on?
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
			
	
	public Particle( Vector spawnPos, ParticleTemplate template, float baseFacing, Physics spawner, Weapon damage ) {
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
		setRadius( image.getIconWidth()/4 );
		collisionList = new ArrayList<GameObject>();
		mass 		= ((float)Math.PI * radius * radius);
		
		//Don't collide with spawner
		if( spawner instanceof Spaceship ) collisionList.add((Spaceship)spawner);
			
		//TODO: change this somehow
		if(spawner == null) 	speed = new Vector();
		else					speed = spawner.getSpeed().clone();
		
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
			requestDelete = true;
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
		if( !onScreen || requestDelete  ) return;
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
	
	public float getFacing() {
		return facing;
	}
	
	public ParticleTemplate getParticleTemplate(){
		return template;
	}
	
	/**
	 * JJ> Gets who spawned this particle
	 * @return The Physics object that spawned the particle which might be a Spaceship,
	 *         Planet or another Particle or any other Physics object.
	 */
	public Physics getSpawner(){
		return spawner;
	}
}
