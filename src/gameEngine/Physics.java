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

import java.util.ArrayList;

import narwhal.Asteroid;
import narwhal.Interceptor;

public abstract class Physics extends Collidable {
	protected Vector speed;
	protected boolean anchored;
	protected float mass = 0;
	private static final float G = 0.00000000667428f;	//6.67428f * 0.000000001f;

	public void update(){
		pos.add( getSpeed() );
	}
	
	public static void updatePhysics( ArrayList<GameObject> objects ){
		ArrayList<Particle> particles = GameEngine.getParticleEngine().getParticleList();
		
		for (int i = 0; i < objects.size(); i++)
		{
			Physics us = objects.get(i);
			
			for (int k = i+1; k < objects.size(); k++)
			{
				Physics them = objects.get(k);
				
				//Check if it is a interceptor that is docking first
				if( them instanceof Interceptor )
				{
					Interceptor tiny = (Interceptor)them;
					if( tiny.outOfFuel() && tiny.getMaster() == us && us.collidesWith(them) )
					{
						tiny.dock();
						continue;
					}
				}
				
				//Do collisions
				if( us.collidesWith(them) )
				{
					us.collision( them );
				}
				
				//Mext, do gravity pulls
				Vector diff = us.getPosCentre().minus(them.getPosCentre());
				
				//Skip gravity pulls if distance is to far
				if( diff.length() > Math.max(us.radius, them.radius)*2 ) continue;

				float pull = G * (( us.mass * them.mass ) / ( diff.length() ));
				
				if (!them.anchored)
				{
					diff.setLength(pull / them.mass);
					them.speed.add(diff);
					
					//Throw asteroids out in space again
					if( us.anchored && them instanceof Asteroid )
					{
						them.getSpeed().setLength(them.speed.length()*1.00175f);
					}
				}
				
				if (!us.anchored)
				{
					diff.negate();
					diff.setLength(pull / us.mass);
					us.speed.add(diff);
				}
			}
			
			//Particle and object gravity
			for (int k = i+1; k < particles.size(); k++)
			{
				Particle them = particles.get(k);
				
				//Only if they have physics activated
				if( !them.getParticleTemplate().physics ) continue;
								
				//Skip gravity pulls if distance is to far
				Vector diff = us.getPosCentre().minus(them.getPosCentre());
				if( diff.length() > 1200 ) continue;

				float pull = G * (( us.mass * them.mass ) / ( diff.length() ));
				
				if (!objects.get(i).anchored)
				{
					diff.setLength(pull / them.mass);
					us.speed.add(diff);
				}
				
				diff.negate();
				diff.setLength(pull / objects.get(i).mass);
				objects.get(i).speed.add(diff);
			}
		}
	}
	
	/**
	 * JJ> Handle one Physics object colliding with another physics object. Note that this is the actual
	 *     collision, not the detection of the collision.
	 * @param object Which other Physics object we are supposed to collide with.
	 */
	public void collision(Physics object){
		
		//We are circle
		if (this.shape == Shape.CIRCLE)
		{
			//They are circle
			if (object.shape == Shape.CIRCLE)
			{
				Vector colVec = this.getPosCentre().minus(object.getPosCentre());
				colVec.setLength((this.radius + object.radius) - colVec.length());
				if (this.anchored && !object.anchored)
				{
					colVec.negate();
					object.getSpeed().add(colVec);
					object.pos.add(colVec);
					if(object.getSpeed().length() < 5) object.getSpeed().setLength(5);
				}
				else if (!this.anchored && object.anchored)
				{
					this.getSpeed().add(colVec);
					this.pos.add(colVec);
					if(object.getSpeed().length() < 5) object.getSpeed().setLength(5);	
				}
				else if (!this.anchored && !object.anchored)
				{
					float combinedMass = this.mass + object.mass;
					
					float thisMultiplier = 1f-(this.mass/combinedMass);
					this.getSpeed().add(colVec.times(thisMultiplier));
					this.pos.add(colVec.times(thisMultiplier));
					this.getSpeed().multiply(0.85f);		//Lose 15% speed
					
					float objectMultiplier = 1f-(object.mass/combinedMass);
					colVec.negate();
					object.getSpeed().add(colVec.times(objectMultiplier));
					object.pos.add(colVec.times(objectMultiplier));
					object.getSpeed().multiply(0.85f);		//Lose 15% speed
				}
			}
			else if (object.shape == Shape.RECT)
			{
				// TODO: Implement
			}
			else if (object.shape == Shape.TRIANGLE)
			{
				// TODO: Implement
			}
		}
		
		//We are rectangle
		else if (this.shape == Shape.RECT)
		{
			if (object.shape == Shape.TRIANGLE)
			{
				// TODO: Implement
			}
		}
	}

	public Vector getSpeed() {
		return speed;
	}
}
