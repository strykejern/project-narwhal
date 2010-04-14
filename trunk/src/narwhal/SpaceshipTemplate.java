package narwhal;

import gameEngine.Image2D;
import gameEngine.Log;
import gameEngine.ResourceMananger;
import gameEngine.Video;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SpaceshipTemplate {

	public final String name;
	public final Image2D image;

	//Engine
	public final float maxSpeed;
	public final float acceleration;
	public final float turnRate;
	public final boolean autoBreaks;
	public final boolean strafe = true;
	public final float slow = 1.00f;				//Slow factor, 0.5f means 50% of normal speed
	
	//Weapon systems
	public final Weapon primary;
	public final Weapon secondary;
	
	//Defensive systems
	public final float lifeMax;
	public final float shieldMax;
	public final float shieldRegen;
	public final float energyMax;
	public final float energyRegen;
	public final short radarLevel;
	
	public SpaceshipTemplate( String fileName ) {		
		float sizeMul = 1.00f;
				
		//Set defaults
		String name = null;
		Image2D image = null;

		float lifeMax = 100;
		float shieldMax = 200;
		float energyMax = 500;
		float shieldRegen = 0;
		float energyRegen = 0;
		
		Weapon primary = null;
		Weapon secondary = null;
		
		short radarLevel = 1;
		boolean autoBreaks = false;
		
		float turnRate = 0.1f;
		float maxSpeed = 15f;
		float acceleration = 0.25f;
		
		try
		{
			BufferedReader parse = new BufferedReader(
					new InputStreamReader(
					ResourceMananger.getInputStream(fileName)));
			
			//Parse the ship file
			while(true)
			{
				String line = parse.readLine();
				
				//Reached end of file
				if(line == null) break;
				
				//Ignore comments
				if( line.startsWith("//") || line.equals("") ) continue;
				
				//Translate line into data
				if     (line.startsWith("[NAME]:"))    name = parse(line);
				else if(line.startsWith("[FILE]:"))    image = new Image2D("/data/ships/" + parse(line));
				else if(line.startsWith("[SIZE]:"))    sizeMul = Float.parseFloat(parse(line));
				
				else if(line.startsWith("[LIFE]:"))    lifeMax = Float.parseFloat(parse(line));
				else if(line.startsWith("[SHIELD]:"))  shieldMax = Float.parseFloat(parse(line));
				else if(line.startsWith("[SREGEN]:"))  shieldRegen = Float.parseFloat(parse(line));
				else if(line.startsWith("[ENERGY]:"))  energyMax = Float.parseFloat(parse(line));
				else if(line.startsWith("[EREGEN]:"))  energyRegen = Float.parseFloat(parse(line));
				
				else if(line.startsWith("[PRIMARY]:"))  primary = new Weapon(parse(line));
				else if(line.startsWith("[SECONDARY]:"))  secondary = new Weapon(parse(line));
				
				else if(line.startsWith("[RADAR]:"))   radarLevel = Short.parseShort(parse(line));
				else if(line.startsWith("[NULLIFIER]:")) autoBreaks = Boolean.parseBoolean(parse(line));
				
				else if(line.startsWith("[TURN_RATE]:")) turnRate = Float.parseFloat(parse(line));
				else if(line.startsWith("[MAX_SPEED]:")) maxSpeed = Float.parseFloat(parse(line));
				
				else if(line.startsWith("[ACCELERATION]:")) acceleration = Float.parseFloat(parse(line));
				else Log.warning("Loading ship file ( "+ fileName +") unrecognized line - " + line);
			}
			if(image == null) throw new Exception("Missing a '[FILE]:' line describing which image to load!");
		}
		catch( Exception e )
		{
			//Something went wrong
			Log.warning("Loading ship (" + fileName + ") - " + e);
		}
				
		//Adjust image size
		if(image != null)
		{
			image.resize(Video.getScreenWidth()/12, Video.getScreenWidth()/12);
			image.scale(sizeMul);
		}
		
		//Now set the temp variables to final
		this.name = name;
		this.image = image;
		
		this.lifeMax = lifeMax;
		this.shieldMax = shieldMax;
		this.energyMax = energyMax;
		this.shieldRegen = shieldRegen;
		this.energyRegen = energyRegen;
		
		this.primary = primary;
		this.secondary = secondary;
		
		this.radarLevel = radarLevel;
		this.autoBreaks = autoBreaks;
		
		this.turnRate = turnRate;
		this.maxSpeed = maxSpeed;
		this.acceleration = acceleration;
	}

	/**
	 * JJ> This is simply to make parsing easier. Gets whatever is behind the colon and trims all
	 *     whitespace before and after the text.
	 * @param line The String to parse
	 * @return The parsed String
	 */
	private String parse(String line) {
		return line.substring(line.indexOf(':')+1).trim();
	}

}
