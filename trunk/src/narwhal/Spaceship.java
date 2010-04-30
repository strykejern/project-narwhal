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

import java.util.ArrayList;
import java.util.Random;

import gameEngine.*;

public abstract class Spaceship extends GameObject {

	//General stuff
	protected String name;						//This ship's name that can be unique
	public String team;						//This ship is on team with any who share the same team
	private int debrisCooldown;				//Time for new wreckage spawn
	public boolean cloaked;					//Cloaked if true
	protected Image2D disguised;			//If disguised != null then we are disguised
	public final boolean vital;				//If this dies, everyone on the team dies
	public boolean homed;

	//Engine
	protected float maxSpeed;
	private float acceleration;
	private float turnRate;
	private boolean autoBreaks;
	private float slow = 1.00f;				//Slow factor, 0.5f means 50% of normal speed
	
	//Weapon systems
	protected Weapon primary;
	protected Weapon secondary;
	protected int cooldown;					//Global ship cooldown
	
	//Defensive systems
	protected float shieldMax;
	protected float shieldRegen;
	protected float shield;
	protected float energyMax;
	protected float energyRegen;
	protected float energy;
	
	//Special Modules
	protected short radarLevel;
	protected Sound   canDisguise;
	protected boolean canStrafe;
	protected SpaceshipTemplate interceptor;
	protected boolean organic;
	protected boolean canWarp;
	protected Weapon tetiaryWeapon;
	protected boolean canCloak;
	private boolean canJam;

	public Spaceship( SpaceshipTemplate blueprint, String team, Game world ) {		
		super(world);

		//Load the variables from the spaceship template and clone them
		name = new String(blueprint.name);
		image = blueprint.image.clone();		
		vital = blueprint.vital;
		
		setMaxLife(blueprint.lifeMax);
		shield = shieldMax 	= blueprint.shieldMax;
		shieldRegen 		= blueprint.shieldRegen;
		energy = energyMax 	= blueprint.energyMax;
		energyRegen 		= blueprint.energyRegen;
		
		primary 		= blueprint.primary;
		secondary 		= blueprint.secondary;
		tetiaryWeapon 	= blueprint.tetiaryWeapon;

		maxSpeed 		= blueprint.maxSpeed;
		acceleration 	= blueprint.acceleration;
		autoBreaks 		= blueprint.autoBreaks;
		turnRate 		= blueprint.turnRate;
		
		radarLevel 		= blueprint.radarLevel;
		interceptor 	= blueprint.interceptor;
		organic 		= blueprint.organic;
		canDisguise 	= blueprint.canDisguise;
		canStrafe		= blueprint.canStrafe;
		canWarp			= blueprint.canWarp;
		canCloak		= blueprint.canCloak;
		canJam 			= blueprint.canJam;
	
		//Set our team
		this.team = team.toUpperCase();

		//Default values
		pos 	  = new Vector();
		disguised = null;
		direction = 0;
		cooldown  = 0;
				
		//Calculate size
		setRadius(image.getWidth()/2);

		//Physics
		speed 		= new Vector();
		shape 		= Shape.CIRCLE;
		canCollide  = true;
		anchored 	= false;
		mass 		= (float)Math.PI * radius * radius;
	}
	
	public void update() {
		//Do ship regeneration
		if(cooldown > 0) 	   cooldown--;
		else
		{
			if( !cloaked )
			{
				if(shield < shieldMax) shield += shieldRegen;
				if(energy < energyMax) energy += energyRegen;
				if( organic ) 		   setLife(getLife() + getLife()/2000);
			}
			
			//Activate abilities
			if( keys.mosButton1 && keys.mosButton2 ) activateSpecialMod();
			else if( keys.mosButton1 ) 	   			 activateWeapon(primary);
			else if( keys.mosButton2 )      		 activateWeapon(secondary);
		}
		
		//Activate ECM jamming
		if( homed && canJam ) jamming();
		
		//Allow new parts to fall off
		if(debrisCooldown > 0) debrisCooldown--;		

		//Reduce the warp timer
		if(warpTime > 0) warpTime--;
		
		//Sustain cloaking drain
		if( cloaked )
		{
			energy -= 0.2f;
			if( energy <= 0 ) cloak();
		}
		
		//Key move
		if 		(keys.up) 	getSpeed().add(new Vector(acceleration*slow, direction, true));
		else if (keys.down)
		{
			if (getSpeed().length() < 0.2f) getSpeed().setLength(0);
			else getSpeed().divide(1.01f);
		}
		else if( autoBreaks )
		{
			if (getSpeed().length() < 0.5f) getSpeed().setLength(0);
			else getSpeed().divide(1.05f);
		}		
		
		//Strafing
		if( canStrafe )
		{
			if (keys.left) 		 getSpeed().addDirection(acceleration, direction-((float)Math.PI/2.0f));
			else if (keys.right) getSpeed().addDirection(acceleration, direction+((float)Math.PI/2.0f));
		}
		direction %= 2 * Math.PI;
		
		//mouse move
		float heading = keys.mouseUniversePos().minus(getPosCentre()).getAngle() - direction;
		if 		(heading > Math.PI)  heading = -((2f * (float)Math.PI) - heading);
		else if (heading < -Math.PI) heading =  ((2f * (float)Math.PI) + heading);
		direction += heading * turnRate * slow;
		
		//If disguised as a rock, rotate around our axis
		if( disguised == null ) image.setDirection( direction );
		else					image.rotate(speed.length()/400);
			
		//Limit to max speed
		if (getSpeed().length() > maxSpeed*slow) getSpeed().setLength(maxSpeed*slow);
		
		//Assume nothing is homing in on us
		homed = false;
		
		super.update();
	}
	
	public void damage(Weapon weapon) {
		float damage = weapon.damage;

		//Apply energy damage first
		energy -= weapon.energyDamage;
		if(energy < 0) energy = 0;
		
		//Slowing effect
		slow -= weapon.slow;
		if( slow < 0.33f ) slow = 0.33f;
		
		//Next damage the shields
		if(shield > 0)
		{
			float shieldDmg = damage*weapon.shieldMul;

			//Shields down?
			if(shield-shieldDmg < 0)
			{
				damage -= shield;
				shield = 0;
			}
			else
			{
				//Nope, damage absorbed by the shield
				shield -= shieldDmg;
				
				//Spawn a shield effect
				GameEngine.getParticleEngine().spawnParticle( "shield.prt", getPosCentre(), direction, this, null );
				return;
			}
		}
		
		float lifeDamage = damage*weapon.lifeMul;
		if( lifeDamage <= 0 ) return;
		
		//Next, lose some life
		setLife(getLife() - lifeDamage);
		
		//Make the player camera shake
		if( this instanceof AI && ((AI)this).isPlayer() )
		{
			Camera.shakeCamera((int)lifeDamage);
		}
		
		//Organics don't explode
		if( !organic )
		{
			//Spawn a explosion effect
			GameEngine.getParticleEngine().spawnParticle( "explosion.prt", getPosCentre(), direction, this, null );

			//Make some part of the ship fall off (25% chance)
			if(debrisCooldown == 0) 
			{
				GameEngine.getParticleEngine().spawnParticle( "debris.prt", getPosCentre(), direction, this, null );
				debrisCooldown = 100;
			}
		}
		else if( debrisCooldown == 0 )
		{
			GameEngine.getParticleEngine().spawnParticle( "gib.prt", getPosCentre(), direction, this, null );
			debrisCooldown = 100;
		}
		
		//We lose 15% speed as well
		getSpeed().multiply(0.85f);	
	}
	
	public void activateWeapon(Weapon wpn) {
		
		//Non-functional system?
		if( wpn == null ) return;
		
		//Ship is on cooldown
		if( cooldown > 0 ) return;

		//Enough energy to activate weapon?
		if( wpn.cost > energy ) return;
		
		//Disable disguise
		if( disguised != null ) disguise();
		
		//It'll cost ya
		cooldown += wpn.cooldown;
		energy -= wpn.cost;
		
		//Spawn in front of ship
		Vector spawnPos = getPosCentre();
		spawnPos.add(new Vector(radius, direction, true));

		//Spawn particle effect
		GameEngine.getParticleEngine().spawnParticle( wpn.particle, spawnPos, direction, this, wpn );
	}
	
	public Image2D getImage(){
		return image;
	}
	
	/**
	 * JJ> Destroys this spaceship, creating a wreckage and explosion
	 */
	public void destroy(){
		
		//Cant kill what is already dead
		if( !active() ) return;
		
		//Spawn wreckage and explode if we are not organic
		if( !organic )
		{
			GameEngine.getParticleEngine().spawnParticle( "bigexplosion.prt", getPosCentre(), direction, this, null );
			for(int i = 0; i < 4; i++) GameEngine.getParticleEngine().spawnParticle( "explosion.prt", getPosCentre(), direction, this, null );
			GameEngine.getParticleEngine().spawnParticle( "wreck.prt", getPosCentre(), direction, this, null );
		}
		else for(int i = 0; i < 4; i++) GameEngine.getParticleEngine().spawnParticle( "gib.prt", getPosCentre(), direction, this, null );
		
		//This kills everyone on our team as well
		if( vital )
		{
			for(int i = 0; world != null && i < world.getEntityList().size(); i++)
			{
				//Only destroy spaceship
				GameObject object = world.getEntityList().get(i);
				if( !object.active() || object == this || !(object instanceof Spaceship) ) continue;
				
				//Only destroy allies
				Spaceship ally = (Spaceship) object;
				if( !ally.team.equals(this.team) ) continue;
				
				ally.remove();
			}
		}

		super.destroy();
	}
		
	/**
	 * JJ> Resets all input actions for this spaceship
	 */
	protected void resetInput() {
		keys.down = false;
		keys.left = false;
		keys.left = false;
		keys.right = false;
		keys.up = false;
		keys.mosButton1 = false;
		keys.mosButton2 = false;
		keys.mosButton3 = false;
	}

	/**
	 * JJ> Removes this Spaceship from game without destroying it with explosions and all
	 */
	public void remove() {
		super.destroy();
	}
	
	/**
	 * JJ> Activates the first and best found special ability
	 */
	private void activateSpecialMod() {
		if( interceptor != null ) spawnInterceptor();
		else if( canDisguise != null ) disguise();
		else if( canWarp ) warp();
		else if( canCloak ) cloak();
		else if( hasECM() ) jamming();
		else activateWeapon(tetiaryWeapon);
	}

	/**
	 * JJ> Spawns a small interceptor ship if valid. A interceptor is a lesser ship that bigger
	 *     ships can carry around with their own independent AI if launched. Costs life to use.
	 */
	private void spawnInterceptor(){
		
		//Do we even have interceptors aboard?
		if( interceptor == null ) return;
		
		//Don't spawn interceptors before we have properly initialized
		if( world == null ) return;
		
		//Only if ready
		if( cooldown > 0 ) return;
		
		//Not enough life to spawn interceptor?
		if( getLife()-interceptor.lifeMax < getMaxLife()/10 ) return;
		setLife( getLife() - interceptor.lifeMax );
		cooldown = 80;
		
		//Spawn a interceptor ship behind of this ship
		Random rand = new Random();
		Vector spawnPos = getPosCentre();
		float spawnDir = (direction + (float)Math.PI + rand.nextFloat() - 0.25f );
		spawnDir %= Math.PI * 2;
		
		spawnPos.addDirection(radius + interceptor.image.getWidth(), spawnDir );
		world.getEntityList().add( new Interceptor(spawnPos, interceptor, this) );		
	}
		
	/**
	 * JJ> Disguises this spaceship as an asteroid. This costs 100% of max Energy and is
	 *     removed when activated again or when the pilot fires his weapons
	 */
	private void disguise() {
		
		//Only if ready
		if( cooldown > 0 || canDisguise == null ) return;
		
		if( disguised == null )
		{
			//Enough energy to activate ability?
			if( energyMax > energy ) return;
			energy = 0;
			cooldown = 100;
			
			//Save our previous form
			disguised = image;
			
			//Load new asteroid form
			image = new Image2D("data/asteroid2.png");
			image.resize( size.getX(), size.getY() );
			image.scale(0.8f);
			
			//Play transformation sound effect
			canDisguise.play3D(pos, GameEngine.getCameraPos());
		}
		else
		{			
			//Turn back into a spaceship
			image.dispose();
			image = disguised;
			cooldown = 100;
			disguised = null;
			
			//Play transformation sound effect
			canDisguise.play3D(pos, GameEngine.getCameraPos());
		}	
	}
	
	private int warpTime = 0;
	private void warp() {
		if( energy <= 0 ) return;
		speed.multiply(0);
		pos.addDirection(maxSpeed*3, direction);
		energy -= maxSpeed/3;
		
		if(warpTime == 0)
		{
			GameEngine.getParticleEngine().spawnParticle("burst.prt", pos, direction, this, null);
			warpTime = 8;
		}
		
		if(energy < maxSpeed) cooldown = 100;
	}
	
	private void jamming() {
		if( energy < 100 || cooldown != 0 ) return;
		energy -= 100;
		cooldown += 100;

		//Jamming effect
		GameEngine.getParticleEngine().spawnParticle("jamming.prt", pos, direction, this, null);

		//Disable enemy homing particles
		ArrayList<Particle> list = GameEngine.getParticleEngine().getParticleList();
		for(int i = 0; i < list.size(); i++)
		{
			Particle prt = list.get(i);
			
			//Only check homing particles
			if( prt.getParticleTemplate().homing == 0 ) continue;
			
			//Max jamming distance
			if( prt.getPos().minus(this.getPosCentre()).length() > 1500 ) continue;
			
			prt.team = "NEUTRAL";
			prt.jammed = true;
 		}
		
		//Iterate through every entity and disable cloaking
		for(int i = 0; i < world.getEntityList().size(); i++)
		{
			GameObject object = world.getEntityList().get(i);
			if( !(object instanceof Spaceship) ) continue;
			
			//Jamming distance
			if( this.getDistanceTo(object) > 1500 ) continue;
			
			Spaceship target = (Spaceship)object;
			if( !target.cloaked || target.team.equals(this.team) ) continue;
			
			//Remove cloaking
			target.cooldown += 75;
			target.cloak();
			
			//TODO: disable radar?
		}
	}
	
	private void cloak() {
		
		cloaked ^= true;
		cooldown = 50;
		if( cloaked ) image.setAlpha( 0.075f );
		else 		  image.setAlpha( 1 );
	}

	public Input getInput() {
		return keys;
	}

	public boolean hasECM() {
		return canJam;
	}
}
