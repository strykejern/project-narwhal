package gameEngine;

import java.awt.Color;
import java.awt.Graphics;

public abstract class GameObject extends Physics{
	protected Keyboard keys;
	protected Image2D image;
	
	public GameObject(){
		super();
	}
	
	public GameObject(Vector pos, Shape shape, Vector size, Image2D image, Keyboard keys){
		init(pos, shape, size, image, keys);
	}
	
	public void init(Vector pos, Shape shape, Vector size, Image2D image, Keyboard keys){
		super.init(pos, false, shape, size);
		this.image = image;
		this.keys = keys;
	}
	
	public void init(Vector pos, Shape shape, Vector size, Image2D image){
		super.init(pos, false, shape, size);
		this.image = image;
	}
	
	public void init(Vector pos, int radius, Image2D image){
		super.init(pos, false, radius);
		this.image = image;
	}
	
	public void init(Vector pos, int radius, Image2D image, Keyboard keys) {
		super.init(pos, false, radius);
		this.image = image;
		this.keys = keys;
	}
	
	public void update() {
		super.update();
	}
	
	public void draw(Graphics g) {
		g.drawImage(image.toImage(), pos.getX()-image.getWidth()/2, pos.getY()-image.getHeight()/2, null);		
	}
	
	public void drawCollision(Graphics g) {
		//Always draw the image bounds
		int w = image.getWidth();
		int h = image.getHeight();		
		g.setColor(Color.BLUE);
		g.drawRect(pos.getX()-w/2, pos.getY()-h/2, w, h);

		//Draw it as a circle
		if( super.shape == null )
		{
			w = (int)super.getRadius()*2;
			h = (int)super.getRadius()*2;
			g.setColor(Color.RED);
			g.drawOval(pos.getX()-w/2, pos.getY()-h/2, w, h);			
		}
	}
}
