package narwhal;

import gameEngine.GameObject;
import gameEngine.Image2D;
import gameEngine.Input;
import gameEngine.Vector;

import java.awt.Graphics;


public class Spaceship extends GameObject{
	private static float MAX_SPEED = 10f;
	
	public Spaceship(Vector spawnPos, Image2D newImg, Input newInput){
		pos 	= spawnPos;
		image 	= newImg;
		keys 	= newInput;
		direction = 0;

		//Calculate size
		image.resize(Game.getScreenWidth()/12, Game.getScreenWidth()/12);
		radius = image.getWidth()/2;

		//Physics
		speed = new Vector();
		shape = Shape.CIRCLE;
		canCollide = true;
		anchored = false;
	}

	public void update() {
		if 		(keys.up) 	speed.sub(new Vector(0.5f, direction, true));
		else if (keys.down) speed.divide(1.01f);
		direction %= 2 * Math.PI;
		float heading = keys.mousePos.minus(new Vector(Game.getScreenWidth()/2, Game.getScreenHeight()/2)).getAngle() - direction;
		if 		(heading > Math.PI)  heading = -((2f * (float)Math.PI) - heading);
		else if (heading < -Math.PI) heading =  ((2f * (float)Math.PI) + heading);
		direction += heading * 0.1f;
		
		image.setDirection( direction );
		
		if (speed.length() > MAX_SPEED) speed.setLength(MAX_SPEED);
		
		// Quick implement of universe bounds
		float uniX = Game.getScreenWidth()*Universe.getUniverseSize();
		float uniY = Game.getScreenHeight()*Universe.getUniverseSize();
		
		if 		(pos.x < 0) 	pos.x = uniX + pos.x;
		else if (pos.x > uniX)  pos.x %= uniX;
		
		if 		(pos.y < 0) 	pos.y = uniY + pos.y;
		else if (pos.y > uniY)  pos.y %= uniY;
		
		super.update();
	}
	
	protected int drawX(){
		return Game.getScreenWidth()/2-image.getWidth()/2;
	}
	
	protected int drawY(){
		return Game.getScreenHeight()/2-image.getHeight()/2;
	}
	
	public void draw(Graphics g) {
		g.drawImage(image.toImage(), drawX(), drawY(), null);
	}

}
