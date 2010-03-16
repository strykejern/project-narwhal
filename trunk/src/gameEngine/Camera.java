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
	private Vector					cameraOverflow;
	
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
		cameraPos.overflowWithin(background.getUniverseSize());
		calculateCameraOverflow();
		
		// Draw background
		background.drawBackground(g, cameraPos);
		
		// Draw all entities
		for (GameObject entity : entities)
		{
			Vector entityOffset = isInFrame(entity);
			if (entityOffset != null)
			{
				entity.draw(g, cameraPos.minus(entityOffset));
				entity.drawCollision(g, cameraPos.minus(entityOffset));
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

	private Vector isInFrame(GameObject entity){
		Vector[] points = new Vector[3];
		points[0] = entity.pos.plus(new Vector(entity.size.x, 0));
		points[1] = entity.pos.plus(new Vector(0, 			  entity.size.y));
		points[2] = entity.pos.plus(new Vector(entity.size.x, entity.size.y));
		
		boolean inframe = false;
		if (entity.pos.isInsideRect(cameraPos, cameraPos.plus(Video.getResolutionVector())))
		{
			inframe = true;
		}
		for (Vector point : points)
		{
			if (point.returnOverflowWithin(background.getUniverseSize()).isInsideRect(cameraPos, cameraPos.plus(Video.getResolutionVector())))
			{
				inframe = true;
				break;
			}
		}
		// TODO: finish
		if (inframe)
		{
			if (isInUniverse(entity.pos)) return new Vector();
			else
			{
				boolean overFlowX = entity.pos.x > background.getUniverseSize().x / 2;
				boolean overFlowY = entity.pos.y > background.getUniverseSize().y / 2;
				
				if (overFlowX && overFlowY) return background.getUniverseSize();
				else if (overFlowX) return new Vector(background.getUniverseSize().x, 0);
				else if (overFlowY) return new Vector(0, background.getUniverseSize().y);
				else return new Vector();
			}
		}
		
		return null;
	}
	
	private boolean isInUniverse(Vector point){
		if (point.isInsideRect(new Vector(), background.getUniverseSize())) return true;
		return false;
	}
	
	private void calculateCameraOverflow(){
		cameraOverflow = new Vector();
		
		if (cameraPos.x < 0) cameraOverflow.x -= 1;
		else if (cameraPos.x + Video.getScreenWidth() > background.getUniverseSize().x) cameraOverflow.x += 1;
		
		if (cameraPos.y < 0) cameraOverflow.y -= 1;
		else if (cameraPos.y + Video.getScreenHeight() > background.getUniverseSize().y) cameraOverflow.y += 1; 
	}
}
