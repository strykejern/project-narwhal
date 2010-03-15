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
		entities.add(new Spaceship(new Vector(1, 1), new Image2D("data/spaceship.png"), keys));
		
		// Initialize the camera
		viewPort = new Camera(
				entities, 
				new Universe(4, System.currentTimeMillis()), 
				particleList,
				entities.get(0));
		
		// Initialize the hud and bind it to the player's ship
		hud = new UI();
		
		this.keys = keys;
	}
	
	public void update(){
		handleInputs();
		
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
		if ( shootDelay > 0 ) shootDelay--;
		else if ( keys.shoot && shootDelay == 0 )
		{
			shootDelay = 200;
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
		hud.draw(g);
		
		//Debug info
		g.setColor(Color.white);
		//g.drawString("Ship position: X: " + ship.getPosition().x + ", Y: " + ship.getPosition().y, 5, 20);
		g.drawString("Number of particles: " + particleList.size(), 5, 40);
	}
	
	private void spawnParticle( Particle prt ) {
		if( particleList.size() >= Particle.MAX_PARTICLES ) return;
		particleList.add( prt );
	}
	
}
