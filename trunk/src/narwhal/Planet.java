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
package narwhal;

import gameEngine.GameObject;
import gameEngine.Image2D;
import gameEngine.Vector;
import gameEngine.Video;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Random;

public class Planet extends GameObject {
	Image surfaceImg;
	
	public Planet(Vector spawnPos, ArrayList<Image2D> imageList, Random rand){
		
		Image2D myImage = imageList.get( rand.nextInt(imageList.size()) );

		//Make it unique
		int planetSize = rand.nextInt(Video.getScreenWidth()/4) + Video.getScreenHeight()/4;
		if( rand.nextBoolean() ) myImage.horizontalFlip();
		if( rand.nextBoolean() ) myImage.verticalFlip();
		myImage.rotate( (float)Math.toRadians(rand.nextInt(360)) );
		myImage.resize(planetSize, planetSize);
		
		pos = spawnPos;
		radius = planetSize/2;
		image = myImage;
		surfaceImg = myImage.toImage();
		
		//Physics
		shape = Shape.CIRCLE;
		anchored = true;
		canCollide = true;
	}
	

	//TODO: SILLY Overrides because we use Image (surfaceImage) instead of Image2D (image)
	public void draw(Graphics g) {
		int xPos = drawX();
		int yPos = drawY();
		g.drawImage(surfaceImg, xPos, yPos, null);		
	}
	protected int drawX(){
		return Game.getPlayerPos().getX() - pos.getX() - surfaceImg.getWidth(null)/2;
	}
	protected int drawY(){
		return Game.getPlayerPos().getY() - pos.getY() - surfaceImg.getHeight(null)/2;
	}	
	public void drawCollision(Graphics g) {
		
		//Always draw the image bounds
		int w = surfaceImg.getWidth(null);
		int h = surfaceImg.getHeight(null);		
		g.setColor(Color.BLUE);
		g.drawRect(drawX(), drawY(), w, h);

		//Draw it as a circle
		if( super.shape == Shape.CIRCLE )
		{
			w = (int)super.radius*2;
			h = (int)super.radius*2;
			g.setColor(Color.RED);
			g.drawOval(drawX()+w/10, drawY()+h/10, w, h);			
		}
	}
	//TODO: END SILLY OVERRIDE

	
}
