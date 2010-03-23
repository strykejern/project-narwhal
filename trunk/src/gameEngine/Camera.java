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
import java.util.Random;

import narwhal.*;

public class Camera {
	private Vector 					cameraPos;
	private Vector					cameraOverflow;
	
	private Vector					cameraBotRight;
	private Vector					cameraBotLeft;
	private Vector					cameraTopRight;
	
	private Vector					universeBotRight;
	private Vector					universeBotLeft;
	private Vector					universeTopRight;
	
	private ArrayList<GameObject> 	entities;
	private GameObject 				follow;
	private Universe 				background;
	private ArrayList<Particle>		particleList;

	private int 					shakeCamera = 0;

	public Camera(ArrayList<GameObject> entities, Universe background, ArrayList<Particle> particleList, GameObject follow){
		this.entities = entities;
		this.follow = follow;
		this.background = background;
		this.particleList = particleList;
		this.cameraPos = new Vector();
	}
	
	public void drawView(Graphics2D g){
		updateCameraVectors();
		
		//Twist and shout! Shake it baby!
		if( shakeCamera != 0 )
		{
			Random rand = new Random();
			int x = rand.nextInt(shakeCamera)-shakeCamera/2;
			int y = rand.nextInt(shakeCamera)-shakeCamera/2;
			cameraPos.x += x;
			cameraPos.y += y;
		}

		// Draw background
		background.drawBackground(g, cameraPos);
		
		int count = 0; // For debug purposes
		
		// Draw all entities
		for (GameObject entity : entities)
		{
			Vector entityOffset = isInFrame(entity);
			if (entityOffset != null)
			{
				entity.draw(g, entityOffset);
				entity.drawCollision(g, entityOffset);
				count++;
			}
		}
		
		//Draw all particles
		for( int i = 0; i < particleList.size(); i++ ) 
		{
			particleList.get(i).draw(g, cameraPos);
		}
		
		//Debug info
		g.setColor(Color.WHITE);
		g.drawString("cameraPos X: " + cameraPos.getX() + " Y: " + cameraPos.getY(), 5, 20);
		g.drawString("shipPos       X: " + entities.get(0).pos.getX() + " Y: " + entities.get(0).pos.getY(), 5, 30);
		g.drawString("PlanetPos   X: " + entities.get(1).pos.getX() + " Y: " + entities.get(1).pos.getY(), 5, 40);
		g.drawString("Drawn entities: " + count, 5, 60);
	}

	private Vector isInFrame(GameObject entity){
		Vector topLeft  = entity.pos;
		Vector botRight = topLeft.plus(new Vector(entity.image.getWidth(), entity.image.getHeight())).returnOverflowWithin(universeBotRight);
		Vector botLeft  = topLeft.plus(new Vector(0, botRight.y)).returnOverflowWithin(universeBotRight);
		Vector topRight = topLeft.plus(new Vector(botRight.x, 0)).returnOverflowWithin(universeBotRight);
		
		if (topLeft.isInsideRect( cameraPos, cameraPos.plus(Video.getResolutionVector()))) return cameraPos;
		if (botRight.isInsideRect(cameraPos, cameraPos.plus(Video.getResolutionVector())))
		{
			if (topLeft.isTopLeftOf(cameraBotRight))
				return cameraPos;
			else
				return background.getUniverseSize().plus(cameraPos);
		}
		
		// Where is the bottom right part of the camera in relation to the top left
		Vector cameraRelation = cameraBotRight.minus(cameraPos);
		
		// Camera overlaps to the right
		if (cameraRelation.x < 0 && cameraRelation.y > 0)
		{
			// topLeft is inside right side
			if (topLeft.isBotLeftOf(cameraTopRight) && topLeft.isTopLeftOf(cameraBotRight)) 
				return (new Vector(universeBotRight.x, 0)).negated().plus(cameraPos);
			
			// botLeft is inside left side
			if (botLeft.isTopRightOf(cameraBotLeft) && botLeft.isBotRightOf(cameraPos)) 
			{
				if (topLeft.isTopRightOf(cameraBotLeft)) 
					return cameraPos;
				else 
					return (new Vector(0, universeBotRight.y)).plus(cameraPos);
			}
			
			// botLeft is inside right side TODO: test
			if (botLeft.isTopLeftOf(cameraBotRight) && botLeft.isBotLeftOf(cameraTopRight))
			{
				if (topLeft.isTopLeftOf(botRight))
					return (new Vector(universeBotRight.x, 0)).negated().plus(cameraPos);
				else
					return (new Vector(universeBotRight.x, -universeBotRight.y)).negated().plus(cameraPos);
			}
		}
		// Camera overlaps to the bottom
		if (cameraRelation.x > 0 && cameraRelation.y < 0)
		{
			// topLeft is inside bottom part
			if (topLeft.isTopLeftOf(cameraBotRight) && topLeft.isTopRightOf(cameraBotLeft)) 
				return (new Vector(0, universeBotRight.y)).negated().plus(cameraPos);
			
			// topRight is inside top part TODO test
			if (topRight.isBotLeftOf(cameraTopRight) && topRight.isBotRightOf(cameraPos))
			{
				if (topLeft.isBotLeftOf(cameraPos))
					return cameraPos;
				else
					return (new Vector(universeBotRight.x, 0)).plus(cameraPos);
			}
			
			// topRight is inside bottom part TODO test
			if (topRight.isTopLeftOf(cameraBotRight) && topRight.isTopRightOf(cameraBotLeft))
			{
				if (topLeft.isTopLeftOf(cameraBotLeft))
					return (new Vector(0, universeBotRight.y)).negated().plus(cameraPos);
				else
					return (new Vector(-universeBotRight.x, universeBotRight.y)).negated().plus(cameraPos);
			}
		}
		// Camera overlaps to the right and the bottom
		if (cameraRelation.x < 0 && cameraRelation.y < 0)
		{
			if (topLeft.isInsideRect(cameraBotRight)) 
				return background.getUniverseSize().negated().plus(cameraPos);
			
			if (topLeft.isTopRightOf(cameraBotLeft))  
				return (new Vector(0, background.getUniverseSize().y)).negated().plus(cameraPos);
			
			if (topLeft.isBotLeftOf(cameraTopRight))  
				return (new Vector(background.getUniverseSize().x, 0)).negated().plus(cameraPos);
			
			if (botRight.isTopLeftOf(cameraBotRight)) 
				return cameraPos;
			
			if (botLeft.isTopRightOf(cameraBotLeft))
				return cameraPos;
			
			if (topRight.isBotLeftOf(cameraTopRight))
				return cameraPos;
		}
		return null;
	}
	
	private void updateCameraVectors(){
		cameraPos.x = follow.pos.x - ((float)Video.getScreenWidth() / 2f) + follow.image.getWidth()/2;
		cameraPos.y = follow.pos.y - ((float)Video.getScreenHeight() / 2f) + follow.image.getHeight()/2;
		cameraPos.overflowWithin(background.getUniverseSize());
		calculateCameraOverflow();
		
		universeBotRight	= background.getUniverseSize();
		universeBotLeft		= new Vector(0, universeBotRight.y);
		universeTopRight	= new Vector(universeBotRight.x, 0);
		
		cameraBotRight		= cameraPos.plus(Video.getResolutionVector()).returnOverflowWithin(universeBotRight);
		cameraBotLeft		= cameraPos.plus(new Vector(0, Video.getResolutionVector().y)).returnOverflowWithin(universeBotRight);
		cameraTopRight		= cameraPos.plus(new Vector(Video.getResolutionVector().x, 0)).returnOverflowWithin(universeBotRight);
	}
	
	public boolean isInUniverse(Vector point){
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
