package gameEngine;

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
	
	public void init(Vector pos, int radius, Image2D image, Keyboard keys){
		super.init(pos, false, radius);
		this.image = image;
		this.keys = keys;
	}
	
	public void update(){
		super.update();
	}
	
	public abstract void draw(Graphics g);
	public abstract void drawCollision(Graphics g);
}
