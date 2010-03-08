package gameEngine;

import java.awt.Color;
import java.awt.Graphics;

public abstract class GameObject extends Physics{
	protected Input keys;
	protected Image2D image;
	
	public GameObject(){
		super();
	}
	
	protected void init(Vector pos, Vector speed, Vector size, Image2D image, Shape shape, float direction, int radius){
		this.pos = pos;
		this.speed = speed;
		this.size = size;
		this.image = image;
		this.shape = shape;
		this.direction = direction;
		this.radius = radius;
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
			w = (int)super.radius*2;
			h = (int)super.radius*2;
			g.setColor(Color.RED);
			g.drawOval(pos.getX()-w/2, pos.getY()-h/2, w, h);			
		}
	}

	public Vector getPosition() {
		return pos;
	}
}
