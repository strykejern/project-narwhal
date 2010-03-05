package gameEngine;

public class Collidable {
	protected static final int SHAPE_RECT = 1, SHAPE_CIRCLE = 2, SHAPE_TRIANGLE = 3;
	private int shape;
	private float direction;
	
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
