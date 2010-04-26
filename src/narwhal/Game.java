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
import gameEngine.GameWindow.GameState;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import narwhal.AI.aiType;
import narwhal.SpawnPoint.Type;


public class Game {
	private ArrayList<GameObject>	entities;		// Contains all gameObjects in the universe...
	private Input 					keys;			// Class to read inputs from
	private HUD						hud;			// User interface
	private Shipyard                shipyard;		// The factory that spawns ships for us
   	public int 						universeSize;
	private Camera					viewPort;		// Handles viewpoints and drawing
	private Spaceship				player;
	private Background 				background;
	private boolean					victory;
		
	private void generateWorld( long seed, int universeSize, ArrayList<SpawnPoint> spawnList ) {
        Random rand = new Random(seed);
        
		// Size of the universe
		this.universeSize = universeSize;

		//Spawn every object
		for(SpawnPoint spawn : spawnList) try 
		{
			//Randomize spawn position if needed
			if(spawn.pos == null) spawn.pos = new Vector(rand.nextInt(GameEngine.getScreenWidth()*universeSize), rand.nextInt(GameEngine.getScreenHeight()*universeSize));
			
			
			//Spaceship
			if( spawn.type == Type.SPACESHIP )
			{
				//Players are handled a little different than AI
				if( spawn.ai == aiType.PLAYER )
				{
	   				shipyard.setCurrentShip(new SpaceshipTemplate(spawn.name));
					player = shipyard.spawnSelectedShip(spawn.pos, this, aiType.PLAYER, spawn.team);
					entities.add(player);
		       		continue;
				}
				
				Spaceship entity;
				entity = shipyard.spawnShip(new SpaceshipTemplate(spawn.name), spawn.pos, this, spawn.ai, spawn.team);
		       	entities.add(entity);
			}
			
			//Planet
			if( spawn.type == Type.PLANET )
			{
				Planet entity;
				entity = new Planet(spawn.pos, seed, spawn.name, this);
		       	entities.add(entity);
			}
			
		} 
		catch (Exception e) 
		{
			Log.warning("Failed to spawn object: " + spawn.name);
		}

		//Initialize the HUD and bind it to the player's ship
		hud = new HUD(player, entities);
		
		//Generate random asteroids
		for(int x = 0; x < universeSize; x++)
			for(int y = 0; y < universeSize; y++)
			{
				int offX = rand.nextInt(GameEngine.getScreenWidth() );
				int offY = rand.nextInt(GameEngine.getScreenHeight() );
				entities.add( new Asteroid(new Vector(x*GameEngine.getScreenWidth() + offX, y*GameEngine.getScreenHeight() + offY), this, 0) );				
			}

		//Generate background
		background = new Background(universeSize, seed);
	}
	
	public Game(Input keys, Shipyard shipyard, ArrayList<SpawnPoint> spawnList, int universeSize){       	
		
		victory = false;
       			
       	//Reference to the shipyard
       	this.shipyard = shipyard;

       	//Reference to player input control
		this.keys = keys;

       	// Initialize the entity container
       	entities = new ArrayList<GameObject>();
		
       	//Game music
       	Music.play( "battle.ogg" );

		//TODO remove this here
		if(spawnList == null)
		{
			spawnList = new ArrayList<SpawnPoint>();
			spawnList.add( new SpawnPoint(Type.SPACESHIP, "data/ships/juggernaught.ship", null, aiType.CONTROLLER, "EVIL") );
			spawnList.add( new SpawnPoint(Type.SPACESHIP, "data/ships/juggernaught.ship", null, aiType.CONTROLLER, "EVIL") );
			spawnList.add( new SpawnPoint(Type.SPACESHIP, "data/ships/juggernaught.ship", null, aiType.CONTROLLER, "EVIL") );
			spawnList.add( new SpawnPoint(Type.SPACESHIP, "data/ships/juggernaught.ship", null, aiType.CONTROLLER, "EVIL") );
			player = shipyard.spawnSelectedShip(new Vector(200, 200), this, aiType.PLAYER, "GOOD");
			entities.add(player);
		}

		//Generate the universe
		generateWorld( System.currentTimeMillis(), universeSize, spawnList );

		// Initialize the camera
		viewPort = new Camera(
				entities, 
				background, 
				player);
		viewPort.configureInputHandler(keys);
		GameEngine.setCamera(viewPort);
	}
	
	public GameWindow.GameState update(){
		
		if(keys.escape) return GameState.GAME_MENU;
		
		// Update all entities
		victory = true;
		for (int i = 0; i < entities.size(); i++)
		{
			GameObject entity = entities.get(i);
			
			//Remove inactive objects from the list
			if( !entity.active() )
			{
				entities.remove(entity);
				continue;
			}
			else if( victory && entity instanceof Spaceship )
			{
				victory = player.team.equals( ((Spaceship)entity).team );
			}
			
			//Update
			entity.update();
		}
		
		//Collision detection
		for(int i = 0; i < entities.size(); i++ )
		{
			GameObject us = entities.get(i);
			
			//Collision with other entities
			for(int j = i + 1; j < entities.size(); j++)
			{
				GameObject them = entities.get(j);
				
				//Check if it is a interceptor that is docking first
				if( them instanceof Interceptor )
				{
					Interceptor tiny = (Interceptor)them;
					if( tiny.outOfFuel() && tiny.getMaster() == us && us.collidesWith(them) )
					{
						tiny.dock();
						continue;
					}
				}
					
				if( us.collidesWith(them) )
				{
					us.collision( them );
				}
			}
		}
		Physics.updateGravitation(entities);
		
		GameEngine.getParticleEngine().update(entities, universeSize);
		
		
		return GameState.GAME_PLAYING;
	}
	
	ArrayList<GameObject> getEntityList(){
		return entities;
	}

	Input getPlayerController() {
		return keys;
	}
	
	public void draw(Graphics2D g){

		viewPort.drawView(g);
		GameEngine.getParticleEngine().render(g);
		hud.draw(g);
				
		//Debug info
		g.setColor(Color.white);
		g.drawString("Number of particles: " + GameEngine.getParticleEngine().getParticleCount(), 5, 50);
		g.drawString("Number of threads: " + Thread.activeCount() + " (" + Sound.getActiveSounds() + " sound)", 5, 70);
	}

	public boolean isEnded() {
		return !player.active();
	}
	
	public boolean victory() {
		return player.active() && victory;
	}
}
