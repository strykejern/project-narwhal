package gameEngine;

public class Collidable {
	private Shape shape;
	protected float direction;
	protected Vector pos;
	protected Vector speed;
	private Vector size;
	private float radius;
	public boolean anchored;
	
	public static enum Shape{
		RECT,
		TRIANGLE
	}
	
	/**
	 * Creates a circle object
	 * @param size - Radius of the circle
	 */
	public Collidable(int size){
		this.shape = null;
		this.size = new Vector(size, size);
		direction = 0;
		pos = new Vector();
		anchored = false;
	}
	
	/**
	 * Creates a Rectangle or a Triangle object
	 * @param shape - Type of shape of the object
	 * @param width - Width of the object
	 * @param height - Height of the object
	 */
	public Collidable(Shape shape, int width, int height){
		this.shape = shape;
		size = new Vector(width, height);
		direction = 0;
		pos = new Vector();
		anchored = false;
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
	
	private float getRadius(){
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
	
	// TODO: Implement
	public boolean collidesWith(Collidable object){
		boolean collision = false;
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
		if (this.anchored && object.anchored) return collision;
		
		if (collision)
		{
			if (this.shape == null)
			{
				if (object.shape == null)
				{
					Vector colVec = this.pos.minus(object.pos);
					colVec.setLength((this.getRadius() + object.getRadius()) - colVec.length());
					if (this.anchored && !object.anchored)
					{
						Log.message("xspeed: " + this.speed.x);
						this.pos.add(colVec);
						float length = this.speed.length();
						Vector tmp = this.speed.clone();
						tmp.setLength(1);
						colVec.setLength(1);
						this.speed = tmp.plus(colVec);
						this.speed.setLength(length);
					}
					else if (!this.anchored && object.anchored)
					{
						
					}
					else
					{
						
					}
					
				}
				else if (object.shape == Shape.RECT)
				{
					
				}
				else if (object.shape == Shape.TRIANGLE)
				{
					// TODO: Implement
				}
			}
			else if (this.shape == Shape.RECT)
			{
				if (object.shape == Shape.TRIANGLE)
				{
					// TODO: Implement
				}
			}
		}
		
		return collision;
	}
}