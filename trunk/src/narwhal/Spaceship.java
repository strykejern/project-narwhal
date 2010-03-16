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

	private static float MAX_SPEED = 15f;
	private Vector universeSize;
	private Weapon weapon;
	private int cooldown;
	
	public int lifeMax = 100;
	public float life = 50;
	public int shieldMax = 200;
	public float shield = 200;
	public int energyMax = 500;
	public float energy = 350;
	
	public Spaceship(Vector spawnPos, Image2D image, Input keys, Vector universeSize, ArrayList<Particle> particleList){
		pos 	= spawnPos;
		this.image 	= image;
		this.keys 	= keys;
		direction = 0;
		
		//Where do we spawn particles?
		this.particleList = particleList;
		
		//Ship weapons
		weapon = new Weapon(45.0f, 50, 15, "fire");

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
		if 		(keys.up) 	speed.add(new Vector(0.25f, direction, true));
		else if (keys.down)
		{
			if (speed.length() < 0.2f) speed.setLength(0);
			else speed.divide(1.01f);
		}
		direction %= 2 * Math.PI;
		
		//mouse move
		float heading = keys.mousePos.minus(new Vector(Video.getScreenWidth()/2, Video.getScreenHeight()/2)).getAngle() - direction;
		if 		(heading > Math.PI)  heading = -((2f * (float)Math.PI) - heading);
		else if (heading < -Math.PI) heading =  ((2f * (float)Math.PI) + heading);
		direction += heading * 0.1f;
		image.setDirection( direction );
		
		if (speed.length() > MAX_SPEED) speed.setLength(MAX_SPEED);
		
		// Quick implement of universe bounds
		float uniX = universeSize.x;
		float uniY = universeSize.y;
		
		if 		(pos.x < 0) 	pos.x = uniX + pos.x;
		else if (pos.x > uniX)  pos.x %= uniX;
		
		if 		(pos.y < 0) 	pos.y = uniY + pos.y;
		else if (pos.y > uniY)  pos.y %= uniY;
		
		super.update();
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
	
	public void activateWeapon(Weapon wpn){
		
		//Enough energy to activate weapon?
		if( wpn.cost > energy ) return;
		
		//Ship is on cooldown
		if( cooldown > 0 ) return;
		Sound snd = new Sound("data/fire.au");
		snd.play();
		
		//It'll cost ya
		cooldown += wpn.cooldown;
		energy -= wpn.cost;

		//Spawn particle effect
		Vector speed = this.speed.clone().times(2);
		particleList.add( new Particle(pos.clone(), wpn.particle, 200, 1.0f, -0.01f, direction, 0, speed ) );
	}
}
