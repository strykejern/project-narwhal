package gameEngine;

public class Spaceship extends GameObject{
	
	public Spaceship(Vector pos, Image2D image, Keyboard keys){
		super.init(pos, image.getWidth()/2, image, keys);
	}

	public void update(){
		
		super.update();
	}
}
