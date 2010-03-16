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

public abstract class Physics extends Collidable{
	protected Vector speed;
	public boolean anchored;
	
	public Physics() {		
	}
	
	public void update(){
		pos.add(speed);
	}
	
	public void collision(Physics object){
		
		//We are circle
		if (this.shape == Shape.CIRCLE)
		{
			//They are circle
			if (object.shape == Shape.CIRCLE)
			{
				Vector colVec = this.pos.minus(object.pos);
				colVec.setLength((this.radius + object.radius) - colVec.length());
				if (this.anchored && !object.anchored)
				{
					colVec.negate();
					object.speed.add(colVec);
					object.pos.add(colVec);
					object.speed.add(colVec);
					object.speed.multiply(0.85f);		//Lose 15% speed
				}
				else if (!this.anchored && object.anchored)
				{
					// TODO: Implement					
				}
				else
				{
					// TODO: Implement			
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
}
