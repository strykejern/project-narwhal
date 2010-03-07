package gameEngine;

import narwhal.Game;

public class Spaceship extends GameObject{
	
	public Spaceship(Vector pos, Image2D image, Keyboard keys){
		super.init(pos, image.getWidth()/2, image, keys);
		image.resize(Game.getScreenWidth()/8, Game.getScreenWidth()/8);
		super.setRadius(image.getWidth()/2 );
	}

	public void update(){
		if 		(keys.up) 	speed.add(new Vector(1, direction, true));
		else if (keys.down) speed.sub(new Vector(1, direction, true));
		if		(keys.left) direction -= Math.PI/45;
		else if (keys.right)direction += Math.PI/45;
		super.update();
	}
}
