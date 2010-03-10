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

import java.util.ArrayList;
import java.util.Random;

public class Planet extends GameObject {
	public Planet(Vector spawnPos, ArrayList<Image2D> imageList, Random rand){
		
		Image2D myImage = imageList.get( rand.nextInt(imageList.size()) );

		//Make it unique
		int planetSize = rand.nextInt(Game.getScreenWidth()/4) + Game.getScreenHeight()/4;
		if( rand.nextBoolean() ) myImage.horizontalFlip();
		if( rand.nextBoolean() ) myImage.verticalFlip();
		myImage.rotate( (float)Math.toRadians(rand.nextInt(360)) );
		myImage.resize(planetSize, planetSize);
		
		pos = spawnPos;
		radius = planetSize/2;
		image = myImage;
		
		//Physics
		shape = Shape.CIRCLE;
		anchored = true;
		canCollide = true;
	}
	
}
