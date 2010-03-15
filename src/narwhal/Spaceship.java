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

import gameEngine.*;

import java.awt.Color;
import java.awt.Graphics;


public class Spaceship extends GameObject{
	private static float MAX_SPEED = 15f;
	private int life;
	private int shield;
	private int energy;
	
	public Spaceship(Vector spawnPos, Image2D newImg, Input newInput){
		pos 	= spawnPos;
		image 	= newImg;
		keys 	= newInput;
		direction = 0;

		//Calculate size
		image.resize(Video.getScreenWidth()/12, Video.getScreenWidth()/12);
		radius = image.getWidth()/2;

		//Physics
		speed = new Vector();
		shape = Shape.CIRCLE;
		canCollide = true;
		anchored = false;
	}

	public void update() {
		if 		(keys.up) 	speed.add(new Vector(0.25f, direction, true));
		else if (keys.down)
		{
			if (speed.length() < 0.2f) speed.setLength(0);
			else speed.divide(1.01f);
		}
		direction %= 2 * Math.PI;
		float heading = keys.mousePos.minus(new Vector(Video.getScreenWidth()/2, Video.getScreenHeight()/2)).getAngle() - direction;
		if 		(heading > Math.PI)  heading = -((2f * (float)Math.PI) - heading);
		else if (heading < -Math.PI) heading =  ((2f * (float)Math.PI) + heading);
		direction += heading * 0.1f;
		
		image.setDirection( direction );
		
		if (speed.length() > MAX_SPEED) speed.setLength(MAX_SPEED);
		
		// Quick implement of universe bounds
		float uniX = Video.getScreenWidth()*Universe.getUniverseSize();
		float uniY = Video.getScreenHeight()*Universe.getUniverseSize();
		
		if 		(pos.x < 0) 	pos.x = uniX + pos.x;
		else if (pos.x > uniX)  pos.x %= uniX;
		
		if 		(pos.y < 0) 	pos.y = uniY + pos.y;
		else if (pos.y > uniY)  pos.y %= uniY;
		
		super.update();
	}
}
