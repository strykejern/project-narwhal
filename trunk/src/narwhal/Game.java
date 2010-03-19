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
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class Game {
	private ArrayList<GameObject>	entities;		// Contains all gameObjects in the universe...
	private ArrayList<Particle> 	particleList;	// Contains Particles in the universe
	private Input 					keys;			// Class to read inputs from
	private UI						hud;			// User interface
	
	private Camera					viewPort;		// Handles viewpoints and drawing
	
	public Game(Input keys){
		//Prepare graphics
		particleList = new ArrayList<Particle>();
       	
		//TODO: move this to a resource loader
       	Particle.loadParticles();
       	
       	// Initialize the entity container
       	entities = new ArrayList<GameObject>();
       	
       	// Size of the universe (for constructors)
       	final int universeSize = 4;
       	
       	//Game music
       	Music.play( new Sound("/data/space.ogg") );
       	
		//Initialize the player ship
		Spaceship player = new Spaceship(new Vector(200, 200), new Image2D("/data/spaceship.png"), keys, new Vector(universeSize * Video.getScreenWidth(), universeSize * Video.getScreenHeight()), particleList);
		entities.add(player);
		
		// Initialize the camera
		viewPort = new Camera(
				entities, 
				new Universe(universeSize, System.currentTimeMillis()), 
				particleList,
				entities.get(0));
		
		//Generate random planets
		entities.add( new Planet(new Vector(800*4-50, 600*4-50), System.currentTimeMillis()) );
		entities.add( new Planet(new Vector(200, 200), System.currentTimeMillis()) );
		
		// Initialize the HUD and bind it to the player's ship
		hud = new UI(player);
		
		this.keys = keys;
	}
	
	public GameWindow.gameState update(){
		
		if(keys.escape) return gameState.GAME_MENU;
				
		// Update all entities
		for (GameObject entity : entities)
			entity.update();
		
		//Update particle effects
		for( int i = 0; i < particleList.size(); i++ )
			if (!particleList.get(i).requestsDelete()) 
				particleList.get(i).update(viewPort);
			else 
				particleList.remove(i--);
		
		return gameState.GAME_PLAYING;
	}
	
	public void draw(Graphics2D g){
		viewPort.drawView(g);
		keys.drawCrosshair(g);
		hud.draw(g);
		
		//Debug info
		g.setColor(Color.white);
		g.drawString("Number of particles: " + particleList.size(), 5, 50);
	}
}
