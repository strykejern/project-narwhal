package gameEngine;

public abstract class Physics extends Collidable{
	protected Vector speed;
	public boolean anchored;
	
	public Physics(){
		
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
				colVec.setLength((this.radius + object.radius) - colVec.length());
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
