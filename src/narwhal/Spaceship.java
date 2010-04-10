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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import gameEngine.*;

public class Spaceship extends GameObject{

	private ParticleEngine 	particleEngine;
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
	public int lifeMax;
	public float life;
	public int shieldMax;
	public float shield;
	public int energyMax;
	public float energy;
	public String name;

	/**
	 * JJ> This is simply to make parsing easier. Gets whatever is behind the colon and trims all
	 *     whitespace before and after the text.
	 * @param line The String to parse
	 * @return The parsed String
	 */
	private String parse(String line) {
		return line.substring(line.indexOf(':')+1).trim();
	}
	
	public Spaceship( String fileName ) {		
		float sizeMul = 1.00f;
		
		//Set defaults
		lifeMax = 100;
		shieldMax = 200;
		energyMax = 500;
				
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
				if( line.startsWith("//") ) continue;
				
				//Translate line into data
				if     (line.startsWith("[NAME]:"))    name = parse(line);
				else if(line.startsWith("[FILE]:"))    image = new Image2D("/data/ships/" + parse(line));
				else if(line.startsWith("[SIZE]:"))    sizeMul = Float.parseFloat(parse(line));
				else if(line.startsWith("[LIFE]:"))    lifeMax = Integer.parseInt(parse(line));
				else if(line.startsWith("[SHIELD]:"))  shieldMax = Integer.parseInt(parse(line));
				else if(line.startsWith("[ENERGY]:"))  energyMax = Integer.parseInt(parse(line));
				else if(line.startsWith("[EREGEN]:"))  ;	//TODO
				else if(line.startsWith("[WEAPON]:"))  ;	//TODO
				else if(line.startsWith("[SREGEN]:"))  ;	//TODO
				else Log.warning("Loading ship file ( "+ fileName +") unrecognized line - " + line);
				/*TODO: weapon, engine and regen and mods*/
			}
			if(image == null) throw new Exception("Missing a '[FILE]:' line describing which image to load!");
		}
		catch( Exception e )
		{
			//Something went wrong
			Log.warning("Loading ship (" + fileName + ") - " + e);
		}
		
		//Default values
		pos 	  = new Vector();
		direction = 0;
		life = lifeMax;
		shield = shieldMax;
		energy = energyMax;
				
		//TODO Ship weapons (remove here)
		weapon = new Weapon(45.0f, 30, 15, "laser.prt", "Laser Cannon");

		//Calculate size
		image.resize(Video.getScreenWidth()/12, Video.getScreenWidth()/12);
		image.scale(sizeMul);
		setRadius(image.getWidth()/2);

		//Physics
		speed = new Vector();
		shape = Shape.CIRCLE;
		canCollide = true;
		anchored = false;
	}
	
	public void instantiate(Vector pos, Input keys, Vector universeSize, ParticleEngine particleEngine) {
		this.pos 	      = pos;
		this.keys 	      = keys;
		this.universeSize = universeSize;
		this.particleEngine = particleEngine;
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
				particleEngine.spawnParticle( "shield", pos, direction, this );
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
		
		//Spawn in front of ship
		Vector spawnPos = getPosCentre();
		spawnPos.add(new Vector(radius*2, direction, true));

		//Spawn particle effect
		particleEngine.spawnParticle( wpn.particle, spawnPos, direction, this );
	}
	
	public Image2D getImage(){
		return image;
	}
}
