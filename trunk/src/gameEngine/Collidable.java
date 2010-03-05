package gameEngine;

public class Collidable {
	private Shape shape;
	private float direction;
	private int width, height;
	protected Vector pos;
	
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
		width = size;
		height = size;
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
		this.width = width;
		this.height = height;
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
		return (float)width;
	}
	
	// TODO: implement
	public boolean collidesWith(Collidable object){
		if (this.shape == null)
		{
			if (object.shape == null && this.pos.minus(object.pos).length() < this.getRadius() + object.getRadius()) return true;
		}
		else if (this.shape == Shape.RECT)
		{
			
		}
		else if (this.shape == Shape.TRIANGLE)
		{
			
		}
		return false;
	}
}