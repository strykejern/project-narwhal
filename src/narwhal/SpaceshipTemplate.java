package narwhal;

import gameEngine.Image2D;
import gameEngine.Log;
import gameEngine.ResourceMananger;
import gameEngine.Sound;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SpaceshipTemplate {

	//Default data
	public final String name;
	public final Image2D image;

	//Engine
	public final float maxSpeed;
	public final float acceleration;
	public final float turnRate;
	
	//Weapon systems
	public final Weapon primary;
	public final Weapon secondary;
	
	//Defensive systems
	public final float lifeMax;
	public final float shieldMax;
	public final float shieldRegen;
	public final float energyMax;
	public final float energyRegen;
	
	//Special mods
	public final short radarLevel;
	public final boolean autoBreaks;
	public final SpaceshipTemplate interceptor;
	public final boolean organic;
	public final Sound canDisguise;
	public final boolean canWarp;
	public final boolean canStrafe;
	public final Weapon tetiaryWeapon;
	public final boolean canCloak;
	public final boolean vital;
	public final boolean canJam;
	
	public SpaceshipTemplate( String fileName ) throws Exception {		
		float sizeMul = 1.00f;
				
		//Set defaults
		String name = null;
		Image2D image = null;
		boolean vital = false;

		float lifeMax = 100;
		float shieldMax = 200;
		float energyMax = 500;
		float shieldRegen = 0;
		float energyRegen = 0;
		
		Weapon primary = null;
		Weapon secondary = null;
		Weapon tetiaryWeapon = null;
		
		short radarLevel = 1;
		boolean autoBreaks = false;
		SpaceshipTemplate interceptor = null;
		boolean organic = false;
		Sound canDisguise = null;
		boolean canStrafe = false;
		boolean canWarp = false;
		boolean canCloak = false;
		boolean canJam = false;
		
		float turnRate = 0.1f;
		float maxSpeed = 15f;
		float acceleration = 0.25f;
		
		if( !ResourceMananger.fileExists(fileName) ) throw new Exception("File not found: " + fileName);
		
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
			if( line.startsWith("//") || line.equals("") || line.indexOf("NONE") != -1 ) continue;
			
			//Translate line into data
			if     (line.startsWith("[NAME]:"))    name = parse(line);
			else if(line.startsWith("[FILE]:"))
			{
				//Figure out if the path is absolute or not
				String path = parse(line);				
				if(path.indexOf('/') != -1) image = new Image2D(path);
				else image = new Image2D("/data/ships/" + path);
			}
			else if(line.startsWith("[SIZE]:"))    sizeMul = Float.parseFloat(parse(line));
			else if(line.startsWith("[VITAL]:"))   vital = Boolean.parseBoolean(parse(line));
			
			else if(line.startsWith("[LIFE]:"))    lifeMax = Float.parseFloat(parse(line));
			else if(line.startsWith("[SHIELD]:"))  shieldMax = Float.parseFloat(parse(line));
			else if(line.startsWith("[SREGEN]:"))  shieldRegen = Float.parseFloat(parse(line));
			else if(line.startsWith("[ENERGY]:"))  energyMax = Float.parseFloat(parse(line));
			else if(line.startsWith("[EREGEN]:"))  energyRegen = Float.parseFloat(parse(line));
			
			else if(line.startsWith("[PRIMARY]:"))  primary = new Weapon(parse(line));
			else if(line.startsWith("[SECONDARY]:"))  secondary = new Weapon(parse(line));
			else if(line.startsWith("[TETIARY]:"))  tetiaryWeapon = new Weapon(parse(line));
			
			else if(line.startsWith("[RADAR]:"))     radarLevel = Short.parseShort(parse(line));
			else if(line.startsWith("[NULLIFIER]:")) autoBreaks = Boolean.parseBoolean(parse(line));
			else if(line.startsWith("[INTERCEPTOR]:")) 
			{
				String load = parse(line);
				interceptor = new SpaceshipTemplate("data/ships/" + load );
				
				//Spit out a warning if it's not a interceptor ship
				if( !load.endsWith( ".interceptor" ) )
					Log.warning( fileName + " - loading interceptor - Not an interceptor ship: " + load);
			}
			else if(line.startsWith("[ORGANIC]:"))  organic = Boolean.parseBoolean(parse(line));
			else if(line.startsWith("[DISGUISE]:")) canDisguise = new Sound(parse(line));
			else if(line.startsWith("[STRAFING]:")) canStrafe = Boolean.parseBoolean(parse(line));
			else if(line.startsWith("[WARP]:"))     canWarp = Boolean.parseBoolean(parse(line));
			else if(line.startsWith("[CLOAKING]:")) canCloak = Boolean.parseBoolean(parse(line));
			else if(line.startsWith("[JAMMING]:"))  canJam = Boolean.parseBoolean(parse(line));
			
			else if(line.startsWith("[TURN_RATE]:")) turnRate = Float.parseFloat(parse(line));
			else if(line.startsWith("[MAX_SPEED]:")) maxSpeed = Float.parseFloat(parse(line));
			else if(line.startsWith("[ACCELERATION]:")) acceleration = Float.parseFloat(parse(line));
			
			else Log.warning("Loading ship file ( "+ fileName +") unrecognized line - " + line);
		}
			
		//Correct the image
		if(image == null) throw new Exception("Missing a '[FILE]:' line describing which image to load!");
		image.resize(75, 75);
		image.scale(sizeMul);
		this.image = image;
		this.vital = vital;
		
		//Now set the temp variables to final
		this.name = name;
		
		this.lifeMax = lifeMax;
		this.shieldMax = shieldMax;
		this.energyMax = energyMax;
		this.shieldRegen = shieldRegen;
		this.energyRegen = energyRegen;
		
		this.primary = primary;
		this.secondary = secondary;
		this.tetiaryWeapon = tetiaryWeapon;
		
		this.radarLevel = radarLevel;
		this.autoBreaks = autoBreaks;
		this.interceptor = interceptor;
		this.organic = organic;
		this.canDisguise = canDisguise;
		this.canWarp = canWarp;
		this.canStrafe = canStrafe;
		this.canCloak = canCloak;
		this.canJam = canJam;
		
		this.turnRate = turnRate;
		this.maxSpeed = maxSpeed;
		this.acceleration = acceleration;
	}
	
	public SpaceshipTemplate( SpaceshipTemplate base, Weapon primary, Weapon secondary, Weapon tetiary, short radar,
							float life, float shield, float energy ) {
		this.image = base.image;
		this.vital = base.vital;
		
		this.name = base.name;
		
		this.lifeMax = life;
		this.shieldMax = shield;
		this.energyMax = energy;
		this.shieldRegen = base.shieldRegen;
		this.energyRegen = base.energyRegen;
		
		this.primary = primary;
		this.secondary = secondary;
		this.tetiaryWeapon = tetiary;
		
		this.radarLevel = radar;
		this.autoBreaks = base.autoBreaks;
		this.interceptor = base.interceptor;
		this.organic = base.organic;
		this.canDisguise = base.canDisguise;
		this.canWarp = base.canWarp;
		this.canStrafe = base.canStrafe;
		this.canCloak = base.canCloak;
		this.canJam = base.canJam;
		
		this.turnRate = base.turnRate;
		this.maxSpeed = base.maxSpeed;
		this.acceleration = base.acceleration;
	}

public SpaceshipTemplate( SpaceshipTemplate base, boolean ecm, boolean strafe, boolean warp, SpaceshipTemplate interceptor, boolean nullifier, boolean cloak, Sound disguise ) {

	this.image = base.image;
	this.vital = base.vital;
	this.organic = base.organic;
	this.name = base.name;
	
	this.lifeMax = base.lifeMax;
	this.shieldMax = base.shieldMax;
	this.energyMax = base.energyMax;
	this.shieldRegen = base.shieldRegen;
	this.energyRegen = base.energyRegen;
	this.primary = base.primary;
	this.secondary = base.secondary;
	this.tetiaryWeapon = base.tetiaryWeapon;
	this.radarLevel = base.radarLevel;
	
	this.turnRate = base.turnRate;
	this.maxSpeed = base.maxSpeed;
	this.acceleration = base.acceleration;

	//These are the only changes that are actually made
	this.autoBreaks = nullifier;
	this.interceptor = interceptor;
	this.canDisguise = disguise;
	this.canWarp = warp;
	this.canStrafe = strafe;
	this.canCloak = cloak;
	this.canJam = ecm;
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
