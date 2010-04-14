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

public class Spaceship extends GameObject {

	protected static final int KILLED = Integer.MIN_VALUE;
	
	//References
	protected ParticleEngine  particleEngine;
	protected int 		      universeSize;

	//General stuff
	public String name;						//This ship's name that can be unique
	public String team;						//This ship is on team with any who share the same team

	//Engine
	protected float maxSpeed;
	private float acceleration;
	private float turnRate;
	private boolean autoBreaks;
	private float slow = 1.00f;				//Slow factor, 0.5f means 50% of normal speed
	
	//Weapon systems
	public Weapon primary;
	public Weapon secondary;
	private int cooldown;					//Global ship cooldown
	
	//Defensive systems
	public float lifeMax;
	public float life;
	public float shieldMax;
	public float shieldRegen;
	public float shield;
	public float energyMax;
	public float energyRegen;
	public float energy;
	
	//Modules
	public short radarLevel;
	
	public Spaceship( SpaceshipTemplate blueprint, String team ) {		

		//Load the variables from the spaceship template and clone them
		name = new String(blueprint.name);
		image = blueprint.image.clone();
		
		life = lifeMax = blueprint.lifeMax;
		shield = shieldMax = blueprint.shieldMax;
		shieldRegen = blueprint.shieldRegen;
		energy = energyMax = blueprint.energyMax;
		energyRegen = blueprint.energyRegen;
		
		primary = blueprint.primary;
		secondary = blueprint.secondary;

		maxSpeed = blueprint.maxSpeed;
		acceleration = blueprint.acceleration;
		autoBreaks = blueprint.autoBreaks;
		turnRate = blueprint.turnRate;
		
		radarLevel = 4;
	
		//Set our team
		this.team = team.toUpperCase();

		//Default values
		pos 	  = new Vector();
		direction = 0;
		cooldown  = 0;
				
		//Calculate size
		setRadius(image.getWidth()/2);

		//Physics
		speed = new Vector();
		shape = Shape.CIRCLE;
		canCollide = true;
		anchored = false;
	}
	
	public void update() {
				
		//Do ship regeneration
		if(cooldown > 0) 	   cooldown--;
		else
		{
			if(shield < shieldMax) shield += shieldRegen;
			if(energy < energyMax) energy += energyRegen;
		}
		
		//Fire!
		if( keys.mosButton2 ) 	   activateWeapon(primary);
		if( keys.mosButton1 )      activateWeapon(secondary);
		
		//Key move
		if 		(keys.up) 	speed.add(new Vector(acceleration*slow, direction, true));
		else if (keys.down)
		{
			if (speed.length() < 0.2f) speed.setLength(0);
			else speed.divide(1.01f);
		}
		else if( autoBreaks )
		{
			if (speed.length() < 0.5f) speed.setLength(0);
			else speed.divide(1.05f);
		}
		if (keys.left) speed.add(new Vector(acceleration, direction-((float)Math.PI/2.0f), true));
		else if (keys.right) speed.add(new Vector(acceleration, direction+((float)Math.PI/2.0f), true));
		direction %= 2 * Math.PI;
		
		//mouse move
		float heading = keys.mouseUniversePos().minus(getPosCentre()).getAngle() - direction;
		if 		(heading > Math.PI)  heading = -((2f * (float)Math.PI) - heading);
		else if (heading < -Math.PI) heading =  ((2f * (float)Math.PI) + heading);
		direction += heading * turnRate;
		image.setDirection( direction );
		
		if (speed.length() > maxSpeed*slow) speed.setLength(maxSpeed);
		
		// Quick implement of universe bounds
		float uniX = universeSize * Video.getScreenWidth();
		float uniY = universeSize * Video.getScreenHeight();
		
		if 		(pos.x < 0) 	pos.x = uniX + pos.x;
		else if (pos.x > uniX)  pos.x %= uniX;
		
		if 		(pos.y < 0) 	pos.y = uniY + pos.y;
		else if (pos.y > uniY)  pos.y %= uniY;
		
		super.update();
	}

	public void planetHit() {
		float damage = lifeMax / 4;
			
		//Next, lose some life
		life -= damage;
		if(life <= 0)
		{
			this.kill();
		}		
	}
	
	public void damage(Weapon weapon) {
		float damage = weapon.damage;
		
		//Apply energy damage first
		energy -= weapon.energyDamage;
		if(energy < 0) energy = 0;
		
		//Next damage the shields
		if(shield > 0)
		{
			float shieldDmg = damage*weapon.shieldMul;

			//Shields down?
			if(shield-shieldDmg < 0)
			{
				damage -= shield;
				shield = 0;
				
				//Spawn a explosion effect
				particleEngine.spawnParticle( "explosion.prt", getPosCentre(), direction, this );
			}
			else
			{
				//Nope, damage absorbed by the shield
				shield -= shieldDmg;
				
				//Spawn a shield effect
				particleEngine.spawnParticle( "shield.prt", getPosCentre(), direction, this );
				return;
			}
		}
		
		//Next, lose some life
		life -= damage*weapon.lifeMul;	
		if(life <= 0)
		{
			this.kill();
		}
		
		//We lose 15% speed as well
		speed.multiply(0.85f);	
	}
	
	public void activateWeapon(Weapon wpn) {
		
		//Non-functional system?
		if( wpn == null ) return;
		
		//Enough energy to activate weapon?
		if( wpn.cost > energy ) return;
		
		//Ship is on cooldown
		if( cooldown > 0 ) return;
		
		//It'll cost ya
		cooldown += wpn.cooldown;
		energy -= wpn.cost;
		
		//Spawn in front of ship
		Vector spawnPos = getPosCentre();
		spawnPos.add(new Vector(radius*2, direction, true));

		//Spawn particle effect
		particleEngine.spawnParticle( wpn.particle, spawnPos, direction, this );
	}
	
	public Image2D getImage(){
		return image;
	}
	
	public void kill(){
		
		//Cant kill what is already dead
		if( !active() ) return;
		
		//Spawn particle effect
		particleEngine.spawnParticle( "bigexplosion.prt", getPosCentre(), direction, this );
		for(int i = 0; i < 4; i++) particleEngine.spawnParticle( "explosion.prt", getPosCentre(), direction, this );
		
		//Mark as dead so that it gets removed in the next update
		life = KILLED;
		
		//Free any resources
		image.dispose();
		particleEngine = null;
	}
	
	public boolean active() {
		return life != KILLED;
	}
}
