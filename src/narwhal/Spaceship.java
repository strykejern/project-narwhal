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

import gameEngine.*;

public class Spaceship extends GameObject{

	private ArrayList<Particle> 	particleList;	// Contains Particles in the universe
	private Vector universeSize;

	//Engine
	private float maxSpeed = 15f;
	private float acceleration = 0.25f;
	private float turnRate = 0.1f;
	private boolean autoBreaks = false;
	
	//Weapon systems
	public Weapon weapon;
	private int cooldown;					//Global ship cooldown
		
	//Defensive systems
	public int lifeMax = 100;
	public float life = lifeMax;
	public int shieldMax = 200;
	public float shield = shieldMax;
	public int energyMax = 500;
	public float energy = energyMax;
	public String name;
	
	public Spaceship(Vector spawnPos, Image2D image, Input keys, Vector universeSize, ArrayList<Particle> particleList, String name){
		pos 	    = spawnPos;
		this.image 	= image;
		this.keys 	= keys;
		this.name   = name;
		direction = 0;
		
		//Where do we spawn particles?
		this.particleList = particleList;
		
		//Ship weapons
		weapon = new Weapon(45.0f, 30, 15, "laser", "Laser Cannon");

		//Calculate size
		image.resize(Video.getScreenWidth()/12, Video.getScreenWidth()/12);
		setRadius(image.getWidth()/2);

		//Physics
		speed = new Vector();
		shape = Shape.CIRCLE;
		canCollide = true;
		anchored = false;
		
		this.universeSize = universeSize;
	}
	
	public void update() {
		
		//Do ship regeneration
		if(cooldown > 0) 	   cooldown--;
		else
		{
			if(shield < shieldMax) shield += 0.25f;
			if(energy < energyMax) energy += 0.5f;
		}
		
		//Fire!
		if( keys.mosButton1 ) activateWeapon(weapon);
		
		//Key move
		if 		(keys.up) 	speed.add(new Vector(acceleration, direction, true));
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
		direction %= 2 * Math.PI;
		
		//mouse move
		float heading = keys.mouseUniversePos().minus(getPosCentre()).getAngle() - direction;
		if 		(heading > Math.PI)  heading = -((2f * (float)Math.PI) - heading);
		else if (heading < -Math.PI) heading =  ((2f * (float)Math.PI) + heading);
		direction += heading * turnRate;
		image.setDirection( direction );
		
		if (speed.length() > maxSpeed) speed.setLength(maxSpeed);
		
		// Quick implement of universe bounds
		float uniX = universeSize.x;
		float uniY = universeSize.y;
		
		if 		(pos.x < 0) 	pos.x = uniX + pos.x;
		else if (pos.x > uniX)  pos.x %= uniX;
		
		if 		(pos.y < 0) 	pos.y = uniY + pos.y;
		else if (pos.y > uniY)  pos.y %= uniY;
		
		super.update();
	}

	public void planetHit() {
		int damage = lifeMax >> 3;
			
		//Next, lose some life
		life -= damage;
		if(life <= 0)
		{
			//TODO: die
		}		
	}
	
	public void damage(Spaceship attacker) {
		Weapon weapon = attacker.weapon;
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
			}
			else
			{
				//Nope, damage absorbed by the shield
				shield -= shieldDmg;
				
				//Spawn a shield effect
				particleList.add( new Particle(pos, "shield", 100, 0.75f, -0.025f, direction, 0 , new Vector() ) );

				return;
			}
		}
		
		//Next, lose some life
		life -= damage*weapon.lifeMul;	
		if(life <= 0)
		{
			//TODO: die
		}
		
		//We lose 15% speed as well
		speed.multiply(0.85f);	
	}
	
	public void activateWeapon(Weapon wpn) {
		
		//Enough energy to activate weapon?
		if( wpn.cost > energy ) return;
		
		//Ship is on cooldown
		if( cooldown > 0 ) return;
		
		//It'll cost ya
		cooldown += wpn.cooldown;
		energy -= wpn.cost;

		//Spawn particle effect
		wpn.spawnParticle(particleList, getPosCentre(), direction, speed);
	}
	
	public Image2D getImage(){
		return image;
	}
}
