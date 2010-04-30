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

	public Planet(Vector spawnPos, long seed, String name, int planetSize, Game world) {
		super(world);
		Random rand = new Random(seed);
		this.world = world;
		
		//Randomize size
		if(planetSize == 0) planetSize = 325 + rand.nextInt(450);

		//Make it unkillable
		this.setMaxLife( OBJECT_INVULNERABLE );

		//Figure out the planet image
		Image2D myImage;
		if(name != null)
		{
			myImage = new Image2D(name);
		}
		else 
		{
			if( planetImages == null ) loadPlanets();
			myImage = planetImages.get( rand.nextInt(planetImages.size()) ).clone();
		}
		
		//Make it unique
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
		mass = (float)Math.PI * radius * radius * 20000;
	}
	
	public void update(){
		//Do Nothing
	}

}
