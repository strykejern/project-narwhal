package gameEngine;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class Planet extends GameObject {
	public Planet(Vector pos, ArrayList<Image2D> images, long seed){
		
		// TODO: Randomize planet look
		
		super.init(pos, null, new Vector(image.getWidth(), image.getHeight()), image, new Vector());
		this.anchored = true;
	}
	
	public void draw(Graphics g){
		// TODO: implement
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
