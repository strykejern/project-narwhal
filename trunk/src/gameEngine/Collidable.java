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

public abstract class Collidable {
	protected Shape shape;
	protected float direction;
	protected Vector pos;
	protected Vector size;
	protected float radius;
	
	public static enum Shape{
		RECT,
		TRIANGLE
	}
	
	/**
	 * Creates an uninitialized Collidable object
	 */
	public Collidable(){
		
	}
	
	private boolean pointInsideShape(Vector point){
		if 		(this.shape == null && pos.minus(point).length() < radius) return true;
		else if (this.shape == Shape.RECT)
		{
			// TODO: account for rotation
			if (point.x < pos.x) 		  return false;
			if (point.x > pos.x + size.x) return false;
			if (point.y < pos.y)		  return false;
			if (point.y > pos.y + size.y) return false;
		}
		else if (this.shape == Shape.TRIANGLE)
		{
			// TODO: Implement
		}
		return false;
	}
	
	public boolean collidesWith(Collidable object){
		boolean collision = false;
		
		//Can't collide with ourself
		if(this == object) return false;
		
		//Figure out collision type
		if (this.shape == null)
		{
			if (object.shape == null)
			{
				if (this.pos.minus(object.pos).length() < this.radius + object.radius) collision = true;
			}
			else if (object.shape == Shape.RECT)
			{
				// Sloppy collision detection between circle and rectangle
				Vector testPoint = object.pos.plus(object.size.dividedBy(2)).minus(this.pos);
				testPoint.setLength(this.radius);
				testPoint = this.pos.plus(testPoint);
				collision = object.pointInsideShape(testPoint);
			}
			else if (object.shape == Shape.TRIANGLE)
			{
				// TODO: Implement
			}
		}
		else if (this.shape == Shape.RECT)
		{
			if (object.shape == null) collision = object.collidesWith(this);
			else if (object.shape == Shape.TRIANGLE)
			{
				
			}
		}
		return collision;
	}
}