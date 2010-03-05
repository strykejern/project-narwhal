package gameEngine;

public class Collidable {
	private int shape;
	private float direction;
	
	static enum SHAPE_TYPE
	{
		SHAPE_RECT, 
		SHAPE_CIRCLE, 
		SHAPE_TRIANGLE,
		SHAPE_COUNT;
	}
	
	public Collidable(int shape){
		this.shape = shape;
		direction = 0;
	}
	
	protected void setDirection(int degrees){
		setDirection((float)Math.toRadians(degrees));
	}
	
	protected void setDirection(float radians){
		this.direction = radians;
	}
	// TODO: implement
	public boolean collidesWith(){
		return false;
	}
}
