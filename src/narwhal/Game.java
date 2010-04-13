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

import narwhal.AI.aiType;


public class Game {
	private ArrayList<GameObject>	entities;		// Contains all gameObjects in the universe...
	private ParticleEngine 	        particleEngine;	// Handles all particles
	private Input 					keys;			// Class to read inputs from
	private HUD						hud;			// User interface
	private Shipyard                shipyard;		// The factory that spawns ships for us
   	public final int universeSize;
	
	private Camera					viewPort;		// Handles viewpoints and drawing
	
	public Game(Input keys, int universeSize, Shipyard shipyard){       	
       	
		// Size of the universe
		this.universeSize = universeSize;

       	//Reference to the shipyard
       	this.shipyard = shipyard;

       	//Reference to player input control
		this.keys = keys;

       	// Initialize the entity container
       	entities = new ArrayList<GameObject>();
       	
		//Prepare particle engine
		particleEngine = new ParticleEngine();
       	
       	//Game music
       	Music.play( "battle.ogg" );

		//Initialize the player ship
		Spaceship player = shipyard.spawnSelectedShip(new Vector(200, 200), this, aiType.PLAYER, "GOOD");
        entities.add(player);

		// Initialize the HUD and bind it to the player's ship
		hud = new HUD(player);

       	//Spawn allies
        for(int i = 0; i < 2; i++)
        {
			Spaceship ally = shipyard.spawnShip("raptor.ship", new Vector(i*100, i*100), this, aiType.CONTROLLER, "GOOD");
	       	entities.add(ally);
			hud.addTracking(ally);		
        }
        
       	//Spawn enemies
        for(int i = 0; i < 3; i++)
        {
			Spaceship enemy = shipyard.spawnShip("juggernaught.ship", new Vector(i*100, 0), this, aiType.BRUTE, "EVIL");
	       	entities.add(enemy);
			hud.addTracking(enemy);		
        }

       	//Spawn the weird Andromeda ships
        for(int i = 0; i < 3; i++)
        {
			Spaceship enemy = shipyard.spawnShip("andromeda.ship", new Vector(0, i*100), this, aiType.CONTROLLER, "ANDROMEDA");
	       	entities.add(enemy);
			hud.addTracking(enemy);		
        }

		//Generate random planets
		Random rand = new Random();
		for(int x = 0; x < universeSize; x++)
			for(int y = 0; y < universeSize; y++)
			{
				if( rand.nextInt(100) >= 25 ) continue;
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

	}
	
	public GameWindow.gameState update(){
		
		if(keys.escape) return gameState.GAME_MENU;
				
		// Update all entities
		for (int i = 0; i < entities.size(); i++)
		{
			GameObject entity = entities.get(i);
			
			//Remove destroyed spaceships from the list
			if(entity instanceof Spaceship)
			{
				Spaceship ship = ((Spaceship) entity);
				if(ship.life == Spaceship.KILLED)
				{
					entities.remove(ship);
					continue;
				}
			}
			
			//Update
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
	
	ParticleEngine getParticleEngine(){
		return particleEngine;
	}

	ArrayList<GameObject> getEntityList(){
		return entities;
	}

	Input getPlayerController() {
		return keys;
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
