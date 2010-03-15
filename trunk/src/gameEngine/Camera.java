package gameEngine;

import java.awt.Color;
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
		cameraPos.x = follow.pos.x - ((float)Video.getScreenWidth() / 2f);
		cameraPos.y = follow.pos.y - ((float)Video.getScreenHeight() / 2f);
		
		// Draw background
		background.drawBackground(g, follow.pos);
		
		// Draw all entities
		for (GameObject entity : entities)
		{
			entity.draw(g, cameraPos);
			entity.drawCollision(g, cameraPos);
		}
		
		//Draw all particles
		for( int i = 0; i < particleList.size(); i++ ) 
		{
			particleList.get(i).draw(g, follow.pos);
		}
		
		g.setColor(Color.WHITE);
		g.drawString("cameraPos X: " + cameraPos.getX() + " Y: " + cameraPos.getY(), 5, 20);
		g.drawString("shipPos   X: " + entities.get(0).pos.getX() + " Y: " + entities.get(0).pos.getY(), 5, 30);
		g.drawString("PlanetPos X: " + entities.get(1).pos.getX() + " Y: " + entities.get(1).pos.getY(), 5, 40);
	}

	private boolean isInFrame(GameObject entity){
		Vector[] points = new Vector[4];
		points[0] = entity.pos;
		points[1] = entity.pos.plus(new Vector(entity.size.x, 0));
		points[2] = entity.pos.plus(new Vector(0, 			  entity.size.y));
		points[3] = entity.pos.plus(new Vector(entity.size.x, entity.size.y));
		
		for (Vector point : points)
			if (point.x > cameraPos.x && point.x < cameraPos.x + Video.getScreenWidth()) return true; // TODO: finish
		return false;
	}
}
