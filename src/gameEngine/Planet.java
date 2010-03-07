package gameEngine;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import narwhal.Game;

public class Planet extends GameObject {
	public Planet(Vector pos, ArrayList<Image2D> images, long seed){
		
		Random rand = new Random(seed);	
		Image2D myImage = images.get( rand.nextInt(images.size()) );
				
		//Make it unique
		int planetSize = rand.nextInt(Game.getScreenWidth()/2) + Game.getScreenHeight()/2;
		if( rand.nextBoolean() ) myImage.horizontalFlip();
		if( rand.nextBoolean() ) myImage.verticalFlip();
		myImage.rotate( rand.nextInt(360) );
		
		super.init(pos, planetSize/2, image);
		
		//Physics
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
