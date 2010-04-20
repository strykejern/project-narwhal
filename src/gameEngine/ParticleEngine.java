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


import gameEngine.ParticleTemplate.InvalidParticleException;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

import narwhal.Planet;
import narwhal.Spaceship;
import narwhal.Weapon;


/**
 * JJ> A useful class that keeps track of all particles for us. It handles particle 
 *     drawing, updating, collisions, etc.
 * @author Anders Eie and Johan Jansen
 *
 */
public class ParticleEngine {
	private static final int MAX_PARTICLES = 512;
	private HashMap<String, ParticleTemplate> particleMap;
	private ArrayList<Particle> particleList;
	
	/**
	 * JJ> Loads all particle images into a hash map for later use
	 */
	public ParticleEngine() {				
		String[] fileList = ResourceMananger.getFileList("/data/particles/");
		
		//Ready array lists
		particleMap = new HashMap<String, ParticleTemplate>();
		particleList = new ArrayList<Particle>();

		//Load all particles into the hash map
		for( String fileName : fileList )
		{	
			//Only load files that end with .prt
			if(!fileName.endsWith(".prt")) continue;
			String hash = fileName.substring( fileName.lastIndexOf("/")+1);
			ParticleTemplate load;

			//Only load it if the image was loaded properly
			try 
			{
				load = new ParticleTemplate(fileName);
				particleMap.put( hash, load );
			} 
			catch (InvalidParticleException e) 
			{
				Log.warning("Loading particle: " + fileName + " - " + e);
			}			
		}
	}
			
	public void update( ArrayList<GameObject> entities, int universeSize ) {
		
		//Update particle effects
		for( int i = 0; i < particleList.size(); i++ )
		{
			Particle prt = particleList.get(i);
			ParticleTemplate template = prt.getParticleTemplate();
			
			//Remove unused particles
			if( prt.requestsDelete() )
			{
				deleteParticle( prt );
				continue;
			}
			
			//Update this particle
			prt.update();
			
			//Collision detection between particles and GameObjects
			if( prt.canCollide )
			{
				for(int j = 0; j < entities.size(); j++ )
				{	
					GameObject object  = entities.get(j);

					if( prt.collidesWith(object) )
					{
						//Skip if no friendly fire
						if( !template.friendlyFire && (object instanceof Spaceship) && prt.team.equals(((Spaceship)object).team) ) continue;
												
						//Damage them
						if( prt.weapon != null )
						{
							//Have we already hit them?
							if( prt.collisionList.contains(object) ) continue;

							//Do the collision
							object.damage( prt.weapon );
							prt.collisionList.add(object);
						}
						
						if( prt.getParticleTemplate().physics ) object.collision(prt);
						
						//Die away if told to
						if( template.collisionEnd && ( !template.subAtomicParticle 
								|| object instanceof Planet ) )
							{
								deleteParticle(prt);
								break;
							}
					}
				}
			}
			
			//Particle on particle collision
			if( template.physics )
			{
				for( int j = i+1; j < particleList.size(); j++ )
				{
					Particle cPrt = particleList.get(j);
					ParticleTemplate cTemplate = cPrt.getParticleTemplate();
					if( !cTemplate.physics ) continue;
					
					if( prt.collidesWith(cPrt) ) prt.collision(cPrt);
				}
			}
			
			// Quick implement of universe bounds
			float uniX = universeSize * Video.getScreenWidth();
			float uniY = universeSize * Video.getScreenHeight();
			
			if 		(prt.pos.x < 0) 	prt.pos.x = uniX + prt.pos.x;
			else if (prt.pos.x > uniX)  prt.pos.x %= uniX;
			
			if 		(prt.pos.y < 0) 	prt.pos.y = uniY + prt.pos.y;
			else if (prt.pos.y > uniY)  prt.pos.y %= uniY;
		}
	}
	
	public boolean spawnParticle(String name, Vector position , float facing, Physics spawner, Weapon damage) {
		
		//Limit number of particles
		if( particleList.size() > MAX_PARTICLES ) return false;
		
		//Check for invalid spawns
		ParticleTemplate type = particleMap.get(name);
		if( type == null )
		{
			Log.warning("Invalid particle spawn: " + name);
			return false;
		}
		
		//Nope everything went well, add it to the active list!
		particleList.add( new Particle(position, type, facing, spawner, damage) );
		return true;
	}

	public void render(Graphics2D g) {
		//Draw all particles
		for(int i = 0; i < particleList.size(); i++) particleList.get(i).draw(g);
	}
	
	/**
	 * JJ> This gets the number of particles currently in the list. It can also include
	 *     particles that are deleted and inactive but not yet removed this update.
	 * @return A Integer which holds the number of particles currently used.
	 */
	public int getParticleCount() {
		return particleList.size();
	}	
	
	/**
	 * JJ> Marks this particle for removal and does any end stuff it needs to do (such as
	 *     spawning other particles or playing sound) and then frees any resources used.
	 */
	public void deleteParticle( Particle prt ){
		ParticleTemplate template = prt.getParticleTemplate();
		
		//Play end sound
		if( template.soundEnd != null )template. soundEnd.play3D( prt.pos, Video.getCameraPos() );
		
		//Spawn any end particle
		if( template.particleEnd != null )
		{
			float prtFacing = prt.getFacing();
			for(int i = 0; i < template.multiEndSpawn; i++)
			{
				spawnParticle( template.particleEnd, prt.pos, prtFacing, prt.getSpawner(), prt.weapon );
				prtFacing += template.endFacingAdd;
			}
		}
		
		//Free it from the particle list
		particleList.remove(prt);
	}	  
	
	public ArrayList<Particle> getParticleList(){
		return particleList;
	}
}
