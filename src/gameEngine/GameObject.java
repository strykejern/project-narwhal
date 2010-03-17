//********************************************************************************************
//*
//*    This file is part of Project Narwhal.
//*
//*    Project Narwhal is free software: you can redistribute it and/or modify it
//*    under the terms of the GNU General Public License as published by
//*    the Free Software Foundation, either version 3 of the License, or
//*    (at your option) any later version.
//*
//*    Project Narwhal is distributed in the hope that it will be useful, but
//*    WITHOUT ANY WARRANTY; without even the implied warranty of
//*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//*    General Public License for more details.
//*
//*    You should have received a copy of the GNU General Public License
//*    along with Project Narwhal.  If not, see <http://www.gnu.org/licenses/>.
//*
//********************************************************************************************
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
		this.setRadius(radius);
		this.canCollide = true;
	}
	
	public void update() {
		super.update();
	}
	
	public void draw(Graphics g, Vector offset) {
		int xPos = drawX(offset);
		int yPos = drawY(offset);
		g.drawImage(image.toImage(), xPos, yPos, null);
	}
	
	public void drawCollision(Graphics g, Vector offset) {
		
		//Always draw the image bounds
		int w = image.getWidth();
		int h = image.getHeight();		
		g.setColor(Color.BLUE);
		g.drawRect(drawX(offset), drawY(offset), w, h);

		//Draw it as a circle
		if( super.shape == Shape.CIRCLE )
		{
			w = size.getX();
			h = size.getY();
			g.setColor(Color.RED);
			g.drawOval(drawX(offset), drawY(offset), w, h);			
		}
	}
	
	protected int drawX(Vector offset){
		return pos.getX() - offset.getX();
	}
	
	protected int drawY(Vector offset){
		return pos.getY() - offset.getY();
	}

	public Vector getPosition() {
		return pos;
	}
	
	public Vector getTopLeftVector(){
		return pos.minus(new Vector(image.getWidth()/2, image.getHeight()/2));
	}
}
