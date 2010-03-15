package gameEngine;

import java.awt.Graphics2D;
import java.util.ArrayList;

import narwhal.*;

public class Camera {
	private Vector 					cameraPos;
	
	private ArrayList<GameObject> 	entities;
	private GameObject 				follow;
	private Universe 				background;
	private ArrayList<Particle>		particleList;
	
	public Camera(ArrayList<GameObject> entities, Universe background, ArrayList<Particle> particleList, GameObject follow){
		this.entities = entities;
		this.follow = follow;
		this.background = background;
		this.particleList = particleList;
		this.cameraPos = new Vector();
	}
	
	public void drawView(Graphics2D g){
		cameraPos.x = follow.pos.x - (Video.getScreenWidth() / 2);
		cameraPos.y = follow.pos.y - (Video.getScreenHeight() / 2);
		
		// Draw background
		background.drawBackground(g, follow.pos);
		
		// Draw all entities
		for (GameObject entity : entities)
		{
			entity.draw(g, cameraPos.minus(entity.pos));
			//entity.drawCollision(g);
		}
		
		//Draw all particles
		for( int i = 0; i < particleList.size(); i++ ) 
		{
			if( particleList.get(i).isOnScreen() )
				particleList.get(i).draw(g, follow.pos);
		}

		//Draw every planet
		background.drawPlanets(g);
	}
	
	private boolean isInFrame(GameObject entity){
		Vector[] points = new Vector[4];
		points[0] = entity.pos;
		points[1] = entity.pos.plus(new Vector(entity.size.x, 0));
		points[2] = entity.pos.plus(new Vector(0, 			  entity.size.y));
		points[3] = entity.pos.plus(new Vector(entity.size.x, entity.size.y));
		
		// TODO: figure this out
		
		// størrelse på univers - posisjon
		// invertere 
		return false;
	}
}
