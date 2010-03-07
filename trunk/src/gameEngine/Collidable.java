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

public class Collidable {
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
	
	/**
	 * Creates a circle object
	 * @param size - Radius of the circle
	 */
	public Collidable(int size){
		init(size);
	}
	
	/**
	 * Creates a Rectangle or a Triangle object
	 * @param shape - Type of shape of the object
	 * @param width - Width of the object
	 * @param height - Height of the object
	 */
	public Collidable(Shape shape, Vector size){
		init(shape, size);
	}
	
	/**
	 * Initializes a Collidable object with shape RECT or TRIANGLE
	 * PS: only needed if default constructor was used
	 * @param shape - Type of shape of the object
	 * @param width - Width of the object
	 * @param height - Height of the object
	 */
	protected void init(Shape shape, Vector size){
		this.shape = shape;
		this.size = size;
		direction = 0;
		pos = new Vector();
	}
	
	/**
	 * Initializes a Collidable object with shape CIRCLE
	 * PS: only needed if default constructor was used
	 * @param size - Radius of the circle
	 */
	protected void init(int size){
		this.shape = null;
		this.size = new Vector(size, size);
		direction = 0;
		pos = new Vector();
	}
	
	/**
	 * Sets the direction of the object
	 */
	protected void setDirection(int degrees){
		setDirection((float)Math.toRadians(degrees));
	}
	
	/**
	 * Sets the direction of the object
	 */
	protected void setDirection(float radians){
		this.direction = radians;
	}
	
	protected float getRadius(){
		return radius;
	}
	
	protected void setRadius(int radius){
		this.radius = (float)radius;
	}
	
	private boolean pointInsideShape(Vector point){
		if 		(this.shape == null && pos.minus(point).length() < getRadius()) return true;
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
				if (this.pos.minus(object.pos).length() < this.getRadius() + object.getRadius()) collision = true;
			}
			else if (object.shape == Shape.RECT)
			{
				// Sloppy collision detection between circle and rectangle
				Vector testPoint = object.pos.plus(object.size.dividedBy(2)).minus(this.pos);
				testPoint.setLength(this.getRadius());
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