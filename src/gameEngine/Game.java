package gameEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import narwhal.*;

public class Game {
	private static Dimension 		resolution;		// ?
	
	private ArrayList<GameObject>	entities;		// Contains all gameObjects in the universe...
	private ArrayList<Particle> 	particleList;	// Contains Particles in the universe
	private Input 					keys;			// Class to read inputs from
	
	private Camera					viewPort;		// Handles viewpoints and drawing
	
	public Game(Input keys){
		//Prepare graphics
		particleList = new ArrayList<Particle>();
       	
		//Load resources
       	Particle.loadParticles();
       	
       	// Initialize the entity container
       	entities = new ArrayList<GameObject>();
       	
		//Initialize the player ship
		entities.add(new Spaceship(new Vector(1, 1), new Image2D("data/spaceship.png"), keys));
		
		// Initialize the camera
		viewPort = new Camera(
				entities, 
				new Universe(resolution, 4, System.currentTimeMillis()), 
				particleList,
				entities.get(0));
		
		this.keys = keys;
	}
	
	public void update(){
		// Update all entities
		for (GameObject entity : entities)
			entity.update();
		
		//Update particle effects
		for( int i = 0; i < particleList.size(); i++ )
			if (!particleList.get(i).requestsDelete()) 
				particleList.get(i).update();
			else 
				particleList.remove(i--);
	}
	
	private static int shootDelay = 0;
	private void handleInputs(){
		shootDelay++;
		if (keys.shoot && shootDelay > 20)
		{
			shootDelay = 0;
			// Testing particle spawn
			/*Random rand = new Random();
			float angle = (float)Math.toRadians(rand.nextInt(360));
			float angleAdd = (float)Math.toRadians(rand.nextInt(5)+1);
			Vector pos = new Vector(ship.getPosition().x - mouse.getPoint().x, ship.getPosition().y - mouse.getPoint().y);
			spawnParticle( new Particle( pos, "fire", 500, 1.0f, -0.005f, angle, angleAdd ));*/
			// end
		}
	}
	
	public void draw(Graphics2D g){
		viewPort.drawView(g);
		keys.drawCrosshair(g);
		// TODO: draw HUD
	}
	
	private void spawnParticle( Particle prt ) {
		if( particleList.size() >= Particle.MAX_PARTICLES ) return;
		particleList.add( prt );
	}
}
