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
import java.util.ArrayList;
import java.util.Random;

public class Planet extends GameObject {
	private static ArrayList<Image2D> planetImages;
	
	private static void loadPlanets() {
		
		String[] fileList = ResourceMananger.getFileList("/data/planets/");
		planetImages = new ArrayList<Image2D>();
		for( String fileName : fileList )
		{
			Image2D load = new Image2D( fileName );
			planetImages.add( load ) ;
		}
	}
	
	public Planet(Vector spawnPos, long seed) {
		Random rand = new Random(seed);
		
		//Initialize the planet images if needed
		if( planetImages == null ) loadPlanets();
		
		//Make it unique
		Image2D myImage = planetImages.get( rand.nextInt(planetImages.size()) );
		int planetSize = rand.nextInt(Video.getScreenWidth()/4) + Video.getScreenHeight()/4;
		if( rand.nextBoolean() ) myImage.horizontalFlip();
		if( rand.nextBoolean() ) myImage.verticalFlip();
		myImage.rotate( (float)Math.toRadians(rand.nextInt(360)) );
		myImage.resize(planetSize, planetSize);
		
		pos = spawnPos;
		setRadius(planetSize/2);
		image = myImage;
		speed = new Vector();
		
		//Physics
		shape = Shape.CIRCLE;
		anchored = true;
		canCollide = true;
	}
}
