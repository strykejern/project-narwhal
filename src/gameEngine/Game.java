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
package gameEngine;

import java.awt.*;
import java.util.ArrayList;

import narwhal.*;

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
       	
		//Initialize the player ship
       	Spaceship player = new Spaceship(new Vector(200, 200), new Image2D("data/spaceship.png"), keys, particleList);
		entities.add(player);
		
		// Initialize the camera
		viewPort = new Camera(
				entities, 
				new Universe(4, System.currentTimeMillis()), 
				particleList,
				entities.get(0));
		
		//Generate random planets
		entities.add( new Planet(new Vector(1, 1), System.currentTimeMillis()) );
		
		// Initialize the hud and bind it to the player's ship
		hud = new UI(player);
		
		this.keys = keys;
	}
	
	public void update(){
		
		// Update all entities
		for (GameObject entity : entities)
			entity.update();
		
		//Update particle effects
		for( int i = 0; i < particleList.size(); i++ )
			if ( !particleList.get(i).requestsDelete() ) 
				particleList.get(i).update();
			else 
				particleList.remove(i--);
		
	}
	
	public void draw(Graphics2D g){
		viewPort.drawView(g);
		keys.drawCrosshair(g);
		hud.draw(g);
	}
	
	public void spawnParticle( Particle prt ) {
		if( particleList.size() >= Particle.MAX_PARTICLES ) return;
		particleList.add( prt );
	}
	
}
