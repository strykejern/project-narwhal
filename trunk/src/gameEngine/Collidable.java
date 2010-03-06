package gameEngine;

public class Collidable {
	private Shape shape;
	private float direction;
	protected Vector pos;
	private Vector size;
	private float radius;
	
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
		if (this.shape == null)
		{
			if (object.shape == null)
			{
				if (this.pos.minus(object.pos).length() < this.getRadius() + object.getRadius()) return true;
				else return false;
			}
			if (object.shape == Shape.RECT)
			{
				// Sloppy collision detection between circle and rectangle
				Vector testPoint = object.pos.plus(object.size.dividedBy(2)).minus(this.pos);
				testPoint.setLength(this.getRadius());
				testPoint = this.pos.plus(testPoint);
				return object.pointInsideShape(testPoint);
			}
		}
		else if (this.shape == Shape.RECT)
		{
			if (object.shape == null) return object.collidesWith(this);
		}
		else if (this.shape == Shape.TRIANGLE)
		{
			
		}
		return false;
	}
}