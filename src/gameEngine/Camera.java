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
			if (isInFrame(entity))
			{
				entity.draw(g, cameraPos);
				entity.drawCollision(g, cameraPos);
			}
		}
		
		//Draw all particles
		for( int i = 0; i < particleList.size(); i++ ) 
		{
			particleList.get(i).draw(g, cameraPos);
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
			if (point.x > cameraPos.x && 
				point.x < cameraPos.x + Video.getScreenWidth() &&
				point.y > cameraPos.y &&
				point.y < cameraPos.y + Video.getScreenHeight()) 
					return true;
		return false;
	}
}
