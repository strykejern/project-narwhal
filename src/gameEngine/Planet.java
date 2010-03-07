package gameEngine;

import java.util.ArrayList;
import java.util.Random;

import narwhal.Game;

public class Planet extends GameObject {
	public Planet(Vector spawnPos, ArrayList<Image2D> imageList, long seed){
		
		Random rand = new Random(seed);	
		Image2D myImage = imageList.get( rand.nextInt(imageList.size()) );

		//Make it unique
		int planetSize = rand.nextInt(Game.getScreenWidth()/2) + Game.getScreenHeight()/2;
		if( rand.nextBoolean() ) myImage.horizontalFlip();
		if( rand.nextBoolean() ) myImage.verticalFlip();
		myImage.rotate( rand.nextFloat() );
		myImage.resize(planetSize, planetSize);
		
		pos = spawnPos;
		radius = planetSize/2;
		image = myImage;
		
		//Physics
		anchored = true;
	}
	
}
