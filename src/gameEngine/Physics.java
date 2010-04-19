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

public abstract class Physics extends Collidable{
	protected Vector speed;
	protected boolean anchored;
	
	protected float mass = 0;
	
	private static final float G = 6.67428f * 0.000000001f;
	//private static final float G = 0.00000000001f;
	
	public Physics() {		
	}
	
	public void update(){
		pos.add(getSpeed());
	}
	
	public static void updateGravitation(ArrayList<GameObject> objects, ArrayList<Particle> particles){
		for (int i = 0; i < objects.size(); i++)
		{
			for (int k = i+1; k < objects.size(); k++)
			{
				Vector diff = objects.get(i).getPosCentre().minus(objects.get(k).getPosCentre());
				
				//Skip gravity pulls if distance is to far
				if( diff.length() > 1200 ) continue;
				
				float pull = G * (( objects.get(i).mass * objects.get(k).mass ) / ( diff.length() ));
				
				if (!objects.get(k).anchored)
				{
					diff.setLength(pull / objects.get(k).mass);
					objects.get(k).speed.add(diff);
				}
				
				if (!objects.get(i).anchored)
				{
					diff.negate();
					diff.setLength(pull / objects.get(i).mass);
					objects.get(i).speed.add(diff);
				}
			}/*
			for (Particle particle : particles)
			{
				Vector diff = objects.get(i).getPosCentre().minus(particle.getPosCentre());
				float pull = G * (( objects.get(i).mass * particle.mass ) / ( diff.length() ));
				
				if (!objects.get(i).anchored)
				{
					diff.setLength(pull / particle.mass);
					particle.speed.add(diff);
				}
				
				if (!particle.anchored)
				{
					diff.negate();
					diff.setLength(pull / objects.get(i).mass);
					objects.get(i).speed.add(diff);
				}
			}*/
		}
	}
	
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
					float thisMultiplier = this.mass/combinedMass;
					this.getSpeed().add(colVec.times(thisMultiplier));
					this.pos.add(colVec.times(thisMultiplier));
					this.getSpeed().multiply(0.85f);		//Lose 15% speed
					
					float objectMultiplier = object.mass/combinedMass;
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
