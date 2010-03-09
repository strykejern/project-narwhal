package narwhal;

import gameEngine.GameObject;
import gameEngine.Image2D;
import gameEngine.Vector;

import java.util.ArrayList;
import java.util.Random;

public class Planet extends GameObject {
	public Planet(Vector spawnPos, ArrayList<Image2D> imageList, long seed){
		
		Random rand = new Random(seed);	
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
		anchored = true;
	}
	
}
