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
	protected boolean canCollide;

	public static enum Shape{
		RECT,
		TRIANGLE,
		CIRCLE
	}
	
	/**
	 * JJ> Test whether a specific Vector point is inside a this Collidable's shape
	 * @param point The Vector point to be tested
	 * @return True if it fits inside us, false otherwise
	 */
	private boolean pointInsideShape(Vector point){
		
		//Circle
		if 		(this.shape == Shape.CIRCLE && pos.minus(point).length() < radius) return true;
		
		//Rectangle
		else if (this.shape == Shape.RECT)
		{
			// TODO: account for rotation
			if (point.x < pos.x) 		  return false;
			if (point.x > pos.x + size.x) return false;
			if (point.y < pos.y)		  return false;
			if (point.y > pos.y + size.y) return false;
			return true;
		}
		
		//Triangle
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

		//Only collide if both can collide
		if( !this.canCollide || !object.canCollide ) return false;

		//Figure out collision type
		if (this.shape == Shape.CIRCLE)
		{
			if (object.shape == Shape.CIRCLE)
			{
				if (this.getPosCentre().minus(object.getPosCentre()).length() < this.radius + object.radius) collision = true;
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
			if (object.shape == Shape.CIRCLE) collision = object.collidesWith(this);
			else if (object.shape == Shape.TRIANGLE)
			{
				
			}
		}
		
		return collision;
	}
		
	/**
	 * JJ> Changes the collision radius for this object. Also changes object
	 *     new size bounds automatically (which equals radius*2 in a square rectangle)
	 * @param radius The integer to set the new radius to
	 */
	protected void setRadius(int radius){
		this.radius = radius;
		this.size = new Vector(radius*2, radius*2);
	}
	
	protected Vector getPosCentre(){
		return pos;
	}
}