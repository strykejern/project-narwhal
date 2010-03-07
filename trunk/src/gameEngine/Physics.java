package gameEngine;

public class Physics extends Collidable{
	protected Vector speed;
	public boolean anchored;
	
	public Physics(){
		
	}
	
	public Physics(Vector pos, Shape shape, Vector size){
		init(pos, false, shape, size);
	}
	
	public Physics(Vector pos, int radius){
		init(pos, false, radius);
	}
	
	protected void init(Vector pos, boolean anchored, Shape shape, Vector size){
		super.init(shape, size);
		this.pos = pos;
		this.speed = new Vector();
		this.anchored = anchored;
	}
	
	protected void init(Vector pos, boolean anchored, int radius){
		super.init(radius);
		this.pos = pos;
		this.speed = new Vector();
		this.anchored = anchored;
	}
	
	public void update(){
		pos.add(speed);
	}
	
	public void collision(Physics object){
		//We are circle
		if (this.shape == null)
		{
			//They are circle
			if (object.shape == null)
			{
				Vector colVec = this.pos.minus(object.pos);
				colVec.setLength((this.getRadius() + object.getRadius()) - colVec.length());
				if (this.anchored && !object.anchored)
				{
					colVec.negate();
					object.speed.add(colVec);
					object.pos.add(colVec);
					object.speed.add(colVec);
					object.speed.multiply(0.8f);		//Lose 20% speed
					/*
					float length = object.speed.length();
					Vector tmp = object.speed.clone();
					tmp.setLength(1);
					colVec.setLength(1);
					object.speed = tmp.plus(colVec).plus(colVec);
					object.speed.setLength(length);
					*/
					object.direction = object.speed.getAngle();// + (float)(Math.PI / 2);
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