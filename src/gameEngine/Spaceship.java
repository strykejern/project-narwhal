package gameEngine;

import narwhal.Game;

public class Spaceship extends GameObject{
	
	public Spaceship(Vector pos, Image2D image, Keyboard keys){
		this.pos 	= pos;
		this.image 	= image;
		this.keys 	= keys;
		this.speed 	= new Vector();
		this.direction = 0;
		
		image.resize(Game.getScreenWidth()/8, Game.getScreenWidth()/8);
		radius = image.getWidth()/2;
	}

	public void update(){
		if 		(keys.up) 	speed.add(new Vector(1, direction, true));
		else if (keys.down) speed.divide(2);
		if		(keys.left) direction -= Math.PI/45;
		else if (keys.right)direction += Math.PI/45;
		image.setDirection( direction );
		super.update();
	}
}
