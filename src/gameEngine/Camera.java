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

import gameEngine.Video.VideoQuality;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import narwhal.*;

public class Camera {
	private Vector 					cameraPos;
	private Vector					universeSize;
	
	private ArrayList<GameObject> 	entities;
	private GameObject 				follow;
	private Background 				background;
	private ParticleEngine		    particleEngine;
	private int 					shakeCamera = 0;

	public Camera(ArrayList<GameObject> entities, Background background, GameObject follow){
		this.entities = entities;
		this.follow = follow;
		this.background = background;
		this.cameraPos = new Vector();
		universeSize = background.getUniverseSize();
	}
	
	public void configureInputHandler(Input in){
		in.setCameraPos(cameraPos);
	}
		
	public void drawView(Graphics2D g){
		updateCameraVectors();
		
		// Draw background
		background.drawBackground(g, cameraPos, follow.speed);
		
		int count = 0; // For debug purposes
		
		// Draw all entities
		for (GameObject entity : entities)
		{
			if ( isInFrame(entity) )
			{
				entity.draw(g, cameraPos);
				
				entity.drawCollision(g, cameraPos);
				count++;
			}
		}
		
		//Draw particles
		if( particleEngine != null ) particleEngine.render(g);
		
		//Debug info
		g.setColor(Color.WHITE);
		g.drawString("cameraPos X: " + cameraPos.getX() + " Y: " + cameraPos.getY(), 5, 20);
		g.drawString("shipPos       X: " + entities.get(0).pos.getX() + " Y: " + entities.get(0).pos.getY(), 5, 30);
		g.drawString("PlanetPos   X: " + entities.get(1).pos.getX() + " Y: " + entities.get(1).pos.getY(), 5, 40);
		g.drawString("Drawn entities: " + count, 5, 60);
	}
	
	
	/**
	 * JJ> Returns true if the specified GameObject is inside the screen
	 * @param object Which GameObject to check
	 * @return True if it is inside the screen. False otherwise.
	 */
	public boolean isInFrame(GameObject object) {
		return isInFrame( object.pos, new Vector(object.image.getWidth(), object.image.getHeight()) );
	}
	
	/**
	 * JJ> Returns true if the specified position is inside the screen
	 * @param pos Position to be inside screen.
	 * @param tolerance The size of the image of the object
	 * @return True if it is inside the screen. False otherwise.
	 */
	public boolean isInFrame(Vector pos, Vector tolerance) {
		float drawX = pos.x - cameraPos.x;
		float drawY = pos.y - cameraPos.y;
		
		if( drawX > Video.getScreenWidth() )  return false;
		if( drawY > Video.getScreenHeight() ) return false;
		if( drawX < -tolerance.x ) 			  return false;
		if( drawY < -tolerance.y ) 			  return false;
		
		return true;
	}

	private void updateCameraVectors(){
		cameraPos.x = follow.pos.x - ((float)Video.getScreenWidth() / 2f) + follow.image.getWidth()/2;
		cameraPos.y = follow.pos.y - ((float)Video.getScreenHeight() / 2f) + follow.image.getHeight()/2;
		
		if (cameraPos.x < 0) cameraPos.x = 0;
		else if (cameraPos.x > universeSize.x - Video.getScreenWidth()) cameraPos.x = universeSize.x - Video.getScreenWidth();
		
		if (cameraPos.y < 0) cameraPos.y = 0;
		else if (cameraPos.y > universeSize.y - Video.getScreenHeight()) cameraPos.y = universeSize.y - Video.getScreenHeight();
		
		//Twist and shout! Shake it baby!
		if( shakeCamera != 0 )
		{
			Random rand = new Random();
			int x = rand.nextInt(shakeCamera)-shakeCamera/2;
			int y = rand.nextInt(shakeCamera)-shakeCamera/2;
			cameraPos.x += x;
			cameraPos.y += y;
		}
	}

	public Vector getCameraPos() {
		return cameraPos;
	}

	public void setParticleEngine(ParticleEngine particleEngine) {
		this.particleEngine = particleEngine;
	}
}
