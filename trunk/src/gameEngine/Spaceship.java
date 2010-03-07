package gameEngine;

import java.awt.Color;
import java.awt.Graphics;

public class Spaceship extends GameObject{
	public Spaceship(Vector pos, Image2D image, Keyboard keys){
		super.init(pos, image.getWidth()/2, image, keys);
	}

	public void update(){
		
		super.update();
	}
	
	public void draw(Graphics g){
		
	}
	
	public void drawCollision(Graphics g){
		int w = (int)super.getRadius()*2;
		int h = (int)super.getRadius()*2;
		g.setColor(Color.RED);
		g.drawOval(pos.getX()-w/2, pos.getY()-h/2, w, h);
		
		w = image.getWidth();
		h = image.getHeight();		
		g.setColor(Color.BLUE);
		g.drawRect(pos.getX()-w/2, pos.getY()-h/2, w, h);
	}
}
