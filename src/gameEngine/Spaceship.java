package gameEngine;

import narwhal.Game;

public class Spaceship extends GameObject{
	private static float MAX_SPEED = 10f;
	
	public Spaceship(Vector pos, Image2D image, Input keys){
		this.pos 	= pos;
		this.image 	= image;
		this.keys 	= keys;
		this.speed 	= new Vector();
		this.direction = 0;
		
		image.resize(Game.getScreenWidth()/12, Game.getScreenWidth()/12);
		radius = image.getWidth()/2;
	}

	public void update(){
		if 		(keys.up) 	speed.add(new Vector(0.5f, direction, true));
		else if (keys.down) speed.divide(1.01f);
		//if		(keys.left) direction -= Math.PI/45;
		//else if (keys.right)direction += Math.PI/45;
		direction %= 2 * Math.PI;
		float heading = keys.mousePos.minus(pos).getAngle() - direction;
		if 		(heading > Math.PI)  heading = -((2f * (float)Math.PI) - heading);
		else if (heading < -Math.PI) heading =  ((2f * (float)Math.PI) + heading);
		direction += heading * 0.1f;
		
		image.setDirection( direction );
		
		if (speed.length() > MAX_SPEED) speed.setLength(MAX_SPEED);
		
		super.update();
		
		if 		(pos.x < 0) 					 pos.x = Game.getScreenWidth();
		else if (pos.x > Game.getScreenWidth())  pos.x = 0;
		if 		(pos.y < 0) 					 pos.y = Game.getScreenHeight();
		else if (pos.y > Game.getScreenHeight()) pos.y = 0;
	}
}
