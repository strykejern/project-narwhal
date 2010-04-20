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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.ImageIcon;

final class ParticleTemplate {
	
	static final float RANDOM_ANGLE = Float.MIN_VALUE;
	
	//Particle variables
	public final ArrayList<ImageIcon> image;
	public final int time;					//How many frames it has to live
	public final float alpha;				//Transparency
	public final float alphaAdd;
	public final float angle;				//Rotation
	public final float angleAdd;
	public final float size;					//Size
	public final float sizeAdd;	
	public final boolean scaleToSpawner;	//Is as big the spawner initially
	public final float speed;				//Movement
	public final boolean attached;			//Attached to spawner?
	
	public final float facing;
	public final float facingAdd;
	
	public final boolean collisionEnd;		//End particle if it collides?
	public final boolean canCollide;
	public final boolean subAtomicParticle;	//Can it move through solid objects? (excluding planets)
	public final boolean physics;	        //Does this particle use the laws of physics?
	
	public final String particleEnd;		//Spawn particle if this one ends?
	public final int    multiEndSpawn;		//How many do we spawn?
	public final float  endFacingAdd;		//How much facing do we add for each one
	
	public final Sound soundEnd;
	public final Sound soundSpawn;

	public final boolean friendlyFire;

	public final float homing;
	
	public ParticleTemplate( String fileName ) throws InvalidParticleException {		
		
		//Temp variables set to default
		image = new ArrayList<ImageIcon>();
		int time = 1;
		
		float alpha = 1;
		float alphaAdd = 0;
		
		float angle = 0;
		float angleAdd = 0;
		
		float facing = 0;
		float facingAdd = 0;
		
		float size = 1;
		float sizeAdd = 0;
		boolean scaleToSpawner = false;
		
		Sound soundSpawn = null;
		Sound soundEnd = null;
		
		boolean attached = false;
		boolean canCollide = false;
		boolean collisionEnd = true;
		boolean friendlyFire = true;
		boolean subAtomicParticle = false;
		boolean physics = false;
		
		float homing = 0;
		float speed = 0;
		
		String particleEnd = null;
		int    multiEndSpawn = 1;
		float  endFacingAdd = 0;

		BufferedReader parse = new BufferedReader(
				new InputStreamReader(
				ResourceMananger.getInputStream(fileName)));
		
		//Parse the ship file
		while(true)
		{
			String line;
			try 
			{
				line = parse.readLine();
			} 
			catch (IOException e) 
			{
				throw new InvalidParticleException( e.toString() );
			}
			
			//Reached end of file
			if(line == null) break;
			
			//Ignore comments
			if( line.startsWith("//") || line.equals("") ) continue;
			
			//Ignore NONE values
			if(line.indexOf("NONE") != -1) continue;

			//Translate line into data
			if(line.startsWith("[IMAGE]:"))    	 		
			{
				//Make sure the file actually exist first.
				String path = "/data/particles/" + parse(line);
				if( !ResourceMananger.fileExists( path ) )
				{
					Log.warning("Loading particle: " + fileName + " - The specified image does not exist: " + path);
					continue;
				}
				
				//Try loading it
				ImageIcon load = new ImageIcon(ResourceMananger.getFilePath(path));	
				if( load.getIconWidth() <= 0 )
				{
					Log.warning("Loading particle: " + fileName + " - Failed loading the specified image! (" + path + ")");
					continue;
				}
				image.add(load);
			}
			else if(line.startsWith("[TIME]:"))  		time = Integer.parseInt(parse(line));
			else if(line.startsWith("[SPEED]:"))  		speed = Float.parseFloat(parse(line));
			else if(line.startsWith("[ATTACHED]:"))  	attached = Boolean.parseBoolean(parse(line));

			else if(line.startsWith("[SIZE]:"))  		size = Float.parseFloat(parse(line));
			else if(line.startsWith("[SIZE_ADD]:")) 	sizeAdd = Float.parseFloat(parse(line));
			else if(line.startsWith("[SCALE_TO_SPAWNER]:"))  scaleToSpawner = Boolean.parseBoolean(parse(line));
			
			else if(line.startsWith("[ALPHA]:"))  		alpha = Float.parseFloat(parse(line));
			else if(line.startsWith("[ALPHA_ADD]:"))	alphaAdd = Float.parseFloat(parse(line));
			
			else if(line.startsWith("[ROTATE]:"))  		
			{
				if(line.indexOf("RANDOM") == -1) angle = Float.parseFloat(parse(line));
				else							 angle = RANDOM_ANGLE;
			}
			else if(line.startsWith("[ROTATE_ADD]:")) 	angleAdd = Float.parseFloat(parse(line));
			
			else if(line.startsWith("[FACING]:"))  		
			{
				if(line.indexOf("RANDOM") == -1) facing = Float.parseFloat(parse(line));
				else							 facing = RANDOM_ANGLE;
			}
			else if(line.startsWith("[FACING_ADD]:")) 	facingAdd = Float.parseFloat(parse(line));
			
			else if(line.startsWith("[SOUND_SPAWN]:"))  soundSpawn = new Sound( parse(line) );
			else if(line.startsWith("[SOUND_END]:"))	soundEnd = new Sound( parse(line) );

			else if(line.startsWith("[CAN_COLLIDE]:"))  canCollide = Boolean.parseBoolean(parse(line));
			else if(line.startsWith("[COLLISION_END]:")) collisionEnd = Boolean.parseBoolean(parse(line));
			else if(line.startsWith("[FRIENDLY_FIRE]:")) friendlyFire = Boolean.parseBoolean(parse(line));
			else if(line.startsWith("[SUBATOMIC]:"))     subAtomicParticle = Boolean.parseBoolean(parse(line));
			else if(line.startsWith("[PHYSICS]:"))       physics = Boolean.parseBoolean(parse(line));

			else if(line.startsWith("[PARTICLE_END]:")) particleEnd = parse(line);
			else if(line.startsWith("[MULTISPAWN_END]:")) multiEndSpawn = Integer.parseInt(parse(line));
			else if(line.startsWith("[END_FACING_ADD]:")) endFacingAdd = Float.parseFloat(parse(line));

			else if(line.startsWith("[HOMING]:")) homing = Float.parseFloat( parse(line) );

			else Log.warning("Loading particle file ( "+ fileName +") unrecognized line - " + line);
		}
		if( image.size() == 0 ) throw new InvalidParticleException("Missing a '[IMAGE]:' line describing which image to load!");
		
		//Now set these values to the actual final values, these can now never be changed!
		this.time = time;
		this.speed = attached ? 0 : speed;
		this.attached = attached;
		this.homing = homing;
		
		this.alpha = alpha;
		this.alphaAdd = alphaAdd;
		
		this.angle = angle;
		this.angleAdd = angleAdd;
		
		this.facing = facing;
		this.facingAdd = facingAdd;
		
		this.size = size;
		this.sizeAdd = sizeAdd;	
		this.scaleToSpawner = scaleToSpawner;
		
		this.soundSpawn = soundSpawn;	
		this.soundEnd = soundEnd;
		
		this.friendlyFire = friendlyFire;
		this.collisionEnd = collisionEnd;
		this.canCollide = canCollide;
		this.subAtomicParticle = subAtomicParticle;
		this.physics = physics;
		
		this.particleEnd = particleEnd;
		this.multiEndSpawn = multiEndSpawn;
		this.endFacingAdd = endFacingAdd;			
	}
	
	/**
	 * JJ> This is simply to make parsing easier. Gets whatever is behind the colon and trims all
	 *     whitespace before and after the text.
	 * @param line The String to parse
	 * @return The parsed String
	 */
	private String parse(String line) {			
		//Return the trimmed value
		return line.substring(line.indexOf(':')+1).trim();
	}
	
	/**
	 * JJ> Custom exception class that descends from Java's Exception class.
	 */
	public class InvalidParticleException extends Exception
	{
		private static final long serialVersionUID = 1L;
		  
		  // Default constructor - initializes instance variable to unknown
		  public InvalidParticleException()
		  {	  
		    super();
		  }
		  
		  // Constructor receives some kind of message that is saved in an instance variable.
		  public InvalidParticleException(String err)
		  {
		    super(err);
		  }
	}
}
