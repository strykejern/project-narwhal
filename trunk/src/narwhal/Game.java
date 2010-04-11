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
import gameEngine.GameWindow.gameState;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;


public class Game {
	private ArrayList<GameObject>	entities;		// Contains all gameObjects in the universe...
	private ParticleEngine 	        particleEngine;	// Handles all particles
	private Input 					keys;			// Class to read inputs from
	private UI						hud;			// User interface
	
	private Camera					viewPort;		// Handles viewpoints and drawing
	
	public Game(Input keys){       	
       	// Initialize the entity container
       	entities = new ArrayList<GameObject>();
       	
       	// Size of the universe (for constructors)
       	final int universeSize = 4;
       	
		//Prepare particle engine
		particleEngine = new ParticleEngine();
       	
       	//Game music
       	//Music.play( new Sound("/data/space.ogg") );

       	//Debug other ship
       	Spaceship xs = new Spaceship("/data/ships/raptor.ship");
       	xs.instantiate( new Vector(126, 126), keys, new Vector(universeSize * Video.getScreenWidth(), universeSize * Video.getScreenHeight()), particleEngine );
       	entities.add(xs);

		//Initialize the player ship
		Spaceship player = new Spaceship("/data/ships/juggernaught.ship");
       	player.instantiate( new Vector(200, 200), keys, new Vector(universeSize * Video.getScreenWidth(), universeSize * Video.getScreenHeight()), particleEngine );
		entities.add(player);
		
		//Generate random planets
		Random rand = new Random();
		for(int x = 0; x < universeSize; x++)
			for(int y = 0; y < universeSize; y++)
			{
				int offX = rand.nextInt(Video.getScreenWidth() - Planet.getMaxSize());
				int offY = rand.nextInt(Video.getScreenHeight() - Planet.getMaxSize());
				entities.add( new Planet(new Vector(x*Video.getScreenWidth() + offX, y*Video.getScreenHeight() + offY), System.nanoTime()) );			
			}
		
		// Initialize the camera
		viewPort = new Camera(
				entities, 
				new Background(universeSize, System.currentTimeMillis()), 
				player);
		viewPort.configureInputHandler(keys);
		particleEngine.setRenderCamera(viewPort);

		// Initialize the HUD and bind it to the player's ship
		hud = new UI(player);
		
		this.keys = keys;
	}
	
	public GameWindow.gameState update(){
		
		if(keys.escape) return gameState.GAME_MENU;
				
		// Update all entities
		for (GameObject entity : entities)
		{
			entity.update();
		}
		
		//Collision detection
		for(int i = 0; i < entities.size(); i++ )
		{
			GameObject us = entities.get(i);
			//Only go through spaceships
			//if( !( us instanceof Spaceship) ) continue;
			
			//Collision with other entities
			for(int j = i + 1; j < entities.size(); j++)
			{
				GameObject them = entities.get(j);
				if( us.collidesWith(them) )
				{
					us.collision( them );
				}
			}
		}
		
		particleEngine.update(entities);
		
		return gameState.GAME_PLAYING;
	}
	
	public void draw(Graphics2D g){

		viewPort.drawView(g);
		keys.drawCrosshair(g);
		hud.draw(g);
				
		//Debug info
		g.setColor(Color.white);
		g.drawString("Number of particles: " + particleEngine.getParticleCount(), 5, 50);
		g.drawString("Number of threads: " + Thread.activeCount(), 5, 70);
	}
}
