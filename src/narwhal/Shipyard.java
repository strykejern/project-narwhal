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
package narwhal;

import gameEngine.GameWindow;
import gameEngine.Image2D;
import gameEngine.Input;
import gameEngine.Log;
import gameEngine.ResourceMananger;
import gameEngine.Vector;
import gameEngine.GameEngine;
import gameEngine.GameWindow.GameState;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import narwhal.AI.aiType;
import narwhal.GameFont.FontType;
import narwhal.SpawnPoint.Type;

/**
 * JJ> This class works like a factory of Spaceships. First it loads all blueprints of all
 *     Spaceship when this class is constructed. Then it can produce new Spaceships using those
 *     blueprints.
 * @author Anders Eie and Johan Jansen
 * @Todo Should split this into a new Skirmish class, class is currently too big and unorganized
 *
 */
public class Shipyard {
	private Image2D radarBackground, spaceshipImage, computerBackground, techscreenBackground;
	private SpaceshipTemplate ship, selectedShip;
	private Iterator<String> select;
	private Input key;
	boolean campaignMode, selectOthers;
	private boolean drawUpgradeScreen;
	
	//Skirmish mode
	private ArrayList<Image2D> fleetList;
	private ArrayList<SpawnPoint> skirmishFleet;
	private String skirmishTeam;
	public int universeSize;
	boolean doPlanets;
	
	//The blueprints of all ships
	private HashMap<String, SpaceshipTemplate> shipList;
	
	//Available upgrades (Campaign only)
	private int techLevel;
	public int maxTechLevel;
	public short maxRadarLevel;
	private ArrayList<Weapon> weaponList;
	private ArrayList<SpecialModule> techList;
	private Iterator<SpecialModule> currentTech;
	private boolean strafing;
	private boolean nullifier;

	public enum SpecialModule{
		NONE,
		CLOAK,
		DISGUISE,
		TETIARY,
		WARP,
		ECM,
		STRAFING,
		NULLIFIER,
		INTERCEPTOR,
	}
	
	//Clickable buttons
	private HashMap<Integer, Button> buttonList;
	static final int BUTTON_START_GAME = 0;
	
	static final int BUTTON_UPGRADE = 2;
	static final int BUTTON_BACK = 3;
	static final int BUTTON_PRIMARY = 4;
	static final int BUTTON_SECONDARY = 5;
	static final int BUTTON_RADAR = 6;
	static final int BUTTON_SPECIAL = 7;
	static final int BUTTON_STRAFING = 8;
	static final int BUTTON_NULLFIER = 9;
	
	static final int BUTTON_NEXT_SHIP = 1;
	static final int BUTTON_ADD_SHIP = 10;
	static final int BUTTON_TEAM     = 11;
	static final int BUTTON_MAP_SIZE = 12;
	static final int BUTTON_PLANETS  = 13;
		
	public Shipyard(Input key) {
		
		//Init arrays
		fleetList = new ArrayList<Image2D>();
		skirmishFleet = new ArrayList<SpawnPoint>();
		
		//Load the list of ships
		parseShipList();
		
		//Ready background
		techscreenBackground = new Image2D("/data/techscreen.png");
		techscreenBackground.resize((int)(GameEngine.getScreenWidth()*0.50f), GameEngine.getScreenHeight());
		radarBackground = new Image2D("/data/shipyard.png");
		radarBackground.resize((int)(GameEngine.getScreenWidth()*0.50f), GameEngine.getScreenHeight());
		computerBackground = new Image2D("/data/interface.png");
		computerBackground.resize(GameEngine.getScreenWidth() - radarBackground.getWidth(), GameEngine.getScreenHeight());
		
		//Ready buttons
		buttonList = new HashMap<Integer, Button>();
    	Vector size = new Vector( 200, 50 );
    	Vector pos = new Vector( GameEngine.getScreenWidth()-size.x, GameEngine.getScreenHeight()-size.y );
		buttonList.put(BUTTON_START_GAME, new Button(pos, size, "Start Game", BUTTON_START_GAME, pos));
		buttonList.put(BUTTON_BACK, new Button(pos, size, "Back", BUTTON_BACK, pos));
		pos.y -= size.y;
		
		//Skirmish buttons
		buttonList.put(BUTTON_NEXT_SHIP, new Button(pos, size.dividedBy(2), "Next", BUTTON_NEXT_SHIP, pos));
		buttonList.put(BUTTON_ADD_SHIP, new Button(pos.minus(new Vector(100, 0)), size.dividedBy(2), "ADD", BUTTON_ADD_SHIP, pos));
		buttonList.put(BUTTON_TEAM, new Button(pos.plus(new Vector(100, 0)), size.dividedBy(2), "TEAM", BUTTON_TEAM, pos));
		buttonList.put(BUTTON_UPGRADE, new Button(pos, size, "UPGRADE", BUTTON_UPGRADE, pos));
		buttonList.put(BUTTON_MAP_SIZE, new Button(pos.minus(new Vector(100, 0)), size.dividedBy(2), "MAP", BUTTON_MAP_SIZE, pos));
		buttonList.put(BUTTON_PLANETS, new Button(pos.plus(new Vector(100, 0)), size.dividedBy(2), "PLANETS", BUTTON_PLANETS, pos));

		//Colorize those buttons
		buttonList.get(BUTTON_PLANETS).setColor( new Color(10, 0, 200), new Color(5, 0, 255, 128) );
		buttonList.get(BUTTON_MAP_SIZE).setColor( new Color(10, 0, 200), new Color(5, 0, 255, 128) );

		//The upgrade buttons
    	pos = new Vector( radarBackground.getWidth()/2, radarBackground.getHeight()/5 );
    	size = new Vector( 240, 40 );
		buttonList.put(BUTTON_PRIMARY, new Button(pos, size, "Primary Weapon", BUTTON_PRIMARY, pos));
		pos.y += size.y;
		buttonList.put(BUTTON_SECONDARY, new Button(pos, size, "Secondary Weapon", BUTTON_SECONDARY, pos));
		pos.y += size.y*2;
		buttonList.put(BUTTON_RADAR, new Button(pos, size, "Radar Level", BUTTON_RADAR, pos));
		pos.y += size.y;
		buttonList.put(BUTTON_SPECIAL, new Button(pos, size, "Special Module", BUTTON_SPECIAL, pos));
		pos.y += size.y;
		buttonList.put(BUTTON_NULLFIER, new Button(pos, size, "Internal Nullfier", BUTTON_NULLFIER, pos));
		pos.y += size.y;
		buttonList.put(BUTTON_STRAFING, new Button(pos, size, "Side Thrusters", BUTTON_STRAFING, pos));

		//Colorize those buttons
		buttonList.get(BUTTON_PRIMARY).setColor( new Color(255, 76, 76), new Color(255, 44, 44, 128) );
		buttonList.get(BUTTON_SECONDARY).setColor( new Color(255, 76, 76), new Color(255, 44, 44, 128) );
		buttonList.get(BUTTON_RADAR).setColor( new Color(255, 76, 255), new Color(255, 44, 255, 128) );
		buttonList.get(BUTTON_SPECIAL).setColor( new Color(255, 255, 76), new Color(255, 255, 44, 128) );
		buttonList.get(BUTTON_NULLFIER).setColor( new Color(76, 90, 255), new Color(55, 55, 255, 128) );
		buttonList.get(BUTTON_STRAFING).setColor( new Color(76, 90, 255), new Color(55, 55, 255, 128) );
		
		//Skirmish mode is default
		setSkrimishMode();
		
		//Input controller
		this.key = key;
	}
		
/*	public Spaceship spawnShip(String name, Vector pos, Game world, aiType AI, String team) {
		
		if( !shipList.containsKey( name ) )
		{
			Log.warning("Trying to spawn an invalid ship - " + name);
			return null;
		}
		
		AI produced;
		produced = new AI(shipList.get(name), team, world);
		produced.instantiate(pos, AI);
				
		return produced;
	}
*/

	/**
	 * JJ> This function gets a spaceship from the list of all spaceships that are current loaded into 
	 *     memory by this Shipyard object and instantiates it into a Universe as a new unique object.
	 * @param name The String reference to the list of spaceships currently loaded ("raptor.ship" for example)
	 * @param pos The Vector position in the world where it is spawned
	 * @param keys What Input controller controls this ship? (could be AI)
	 * @param world What world is this spaceship spawning in, this should be a Game object
	 * @return The new Spaceship ready to fight!
	 */
	public Spaceship spawnShip(SpaceshipTemplate template, Vector pos, Game world, aiType AI, String team) {
		AI produced;
		produced = new AI(template, team, world);
		produced.instantiate(pos, AI);
				
		return produced;
	}
	
	/**
	 * JJ> Describes a single weapon set in detail
	 */
	private int describeWeapon(Graphics2D g, int x, int y, Weapon wpn){
		
		//No weapon
		if( wpn == null )
		{
			g.drawString("WEAPON SYSTEM: N/A", x, y);
			return y;
		}
		
		//Draw weapon image
		Image icon = GameEngine.getParticleEngine().getParticleIcon(wpn.particle).getImage();
		
		//Resize it so that it fits
		if(icon.getWidth(null) < 175)
		{
			if(icon.getHeight(null) < 100)
				 g.drawImage(icon, GameEngine.getScreenWidth()-icon.getWidth(null)-50, y, null);
			else g.drawImage(icon, GameEngine.getScreenWidth()-icon.getWidth(null)-50, y, icon.getWidth(null), 80, null);
		}
		else 
			g.drawImage(icon, GameEngine.getScreenWidth()-200, y, 175, icon.getHeight(null), null);
			
		g.drawString("WEAPON SYSTEM: " + wpn.name, x, y);
		y += GameFont.getHeight(g);
		g.drawString("Damage: " + wpn.damage, x, y);
		y += GameFont.getHeight(g);
		g.drawString("Hull Penentration: " + (int)(wpn.lifeMul*100) + "%", x, y);
		y += GameFont.getHeight(g);
		g.drawString("Shield Penentration: " + (int)(wpn.shieldMul*100) + "%", x, y);
		y += GameFont.getHeight(g);
		g.drawString("Energy Cost: " + wpn.cost, x, y);
		return y;
	}
		
	
	public void draw(Graphics2D g) {
		final int OFFSET_X = GameEngine.getScreenWidth()/32;
		final int OFFSET_Y = GameEngine.getScreenHeight()/16;
		
		//Do first, draw background right side of the screen
		computerBackground.draw(g, radarBackground.getWidth(), 0);
		
		//Then Draw the left side of the screen
		//Select enemies and allies in skirmish mode
		if( selectOthers )
		{
			int x = OFFSET_X, y = OFFSET_Y;
			
			computerBackground.draw(g, 0, 0);
			
			//Draw the team name
			if( skirmishTeam.equals("GOOD") )
			{
				GameFont.set(g, FontType.FONT_CRYSTAL, Color.GREEN, 24);
				g.drawString("Team: Good", x, y);
			}
			else
			{
				GameFont.set(g, FontType.FONT_CRYSTAL, Color.RED, 24);
				g.drawString("Team: Evil", x, y);
			}			
			y += GameFont.getHeight(g);
			
			//Draw every ship in the team
			for( int i = 0; i < fleetList.size(); i++ )
			{
				//Only draw ships on this team
				SpawnPoint object = skirmishFleet.get(i);
				if( object.team == null || !object.team.equals(skirmishTeam) ) continue;

				//Draw the little ship icon
				fleetList.get(i).draw(g, x, y);
				x += fleetList.get(i).getWidth();
				if( x > fleetList.get(i).getWidth()*7 )
				{
					x = OFFSET_X;
					y += OFFSET_Y;
					if( y > GameEngine.getScreenHeight() ) break;
				}
			}		
		}
		
		//Upgrade techs
		else if( drawUpgradeScreen )
		{
			techscreenBackground.draw(g, 0, 0);
			
			//Draw the tech level bar
			calculateTechLevel();
			int width = OFFSET_X*11;
			final int barX = techscreenBackground.getWidth()/2-width/2;
			final int barY = techscreenBackground.getHeight()-OFFSET_Y*2;
						
			//Empty bar
			g.setColor(Color.DARK_GRAY);
			g.fillRoundRect(barX, barY, width, OFFSET_Y, 15, 15);
			
			//Filled bar
			if(techLevel > maxTechLevel)  g.setColor(Color.RED);
			else						  g.setColor(Color.BLUE);
			width = (width/maxTechLevel) * Math.min(techLevel, maxTechLevel);
			g.fillRoundRect(barX, barY, width, OFFSET_Y, 15, 15);
			
			//Text
			String text = "Tech Level: " + techLevel + "/" + maxTechLevel;
			GameFont.set(g, FontType.FONT_DESCRIBE, Color.BLACK, 16);
			g.drawString(text, techscreenBackground.getWidth()/2-GameFont.getWidth(text, g)/2, barY+OFFSET_Y/2);
			
			if(techLevel > maxTechLevel) 
			{
				g.setColor(Color.RED);
				text = "TECH LEVEL TOO HIGH";
				g.drawString(text, techscreenBackground.getWidth()/2-GameFont.getWidth(text, g)/2, barY);
				buttonList.get(BUTTON_BACK).hide();
			}
			else buttonList.get(BUTTON_BACK).show();
			
		}
		
		//Select player ship
		else 
		{
			int x = radarBackground.getWidth()/2; 
			int y = radarBackground.getHeight()/2-spaceshipImage.getHeight()/2;
			
			radarBackground.draw(g, 0, 0);
			spaceshipImage.rotate(0.01f);
			spaceshipImage.draw(g, x-spaceshipImage.getWidth()/2, y);
			
			//Level details in skirmish mode
			if( !campaignMode )
			{
				GameFont.set(g, FontType.FONT_DESCRIBE, Color.RED, 16);
				g.drawString("Level Size: " + universeSize, x - GameFont.getWidth("Level Size:  ", g)/2, y + spaceshipImage.getHeight() + GameFont.getHeight(g)*2);
				g.drawString("Planets: " + doPlanets, x - GameFont.getWidth("Planets: XXXX", g)/2, y + spaceshipImage.getHeight() + GameFont.getHeight(g)*3);
			}
		}
		
		//Ship description
		int x = radarBackground.getWidth() + OFFSET_X;
		int y = OFFSET_Y;
		
		//Ship name
		GameFont.set(g, FontType.FONT_MENU, Color.GREEN, 14);
		g.drawString(ship.name.toUpperCase(), x, y);
		y += GameFont.getHeight(g)*2;

		//Weapon description
		GameFont.set(g, FontType.FONT_NORMAL, Color.GREEN, 14);
		y += GameFont.getHeight(g);
		y = describeWeapon(g, x, y, ship.primary);
		y += GameFont.getHeight(g);
		y = describeWeapon(g, x, y, ship.secondary);
		y += GameFont.getHeight(g)*3;
		
		//Ship description
		g.drawString("SHIP PROPERTIES: ", x, y);
		y += GameFont.getHeight(g);
		g.drawString("Hull: " + ship.lifeMax, x, y);
		y += GameFont.getHeight(g);
		g.drawString("Shields: " + ship.shieldMax, x, y);
		y += GameFont.getHeight(g);
		g.drawString("Energy: " + ship.energyMax, x, y);
		y += GameFont.getHeight(g)*3;

		//Ship modifications
		describeSpecialMods(g, x, y);

		//Do last, draw all buttons
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().draw(g);
	}
	
	
	
	/**
	 * JJ> Describes all the special modules the current selected ship has
	 */
	private void describeSpecialMods( Graphics2D g, int x, int y ){
		
		g.drawString("SPECIAL MODIFICATIONS: ", x, y);
		y += GameFont.getHeight(g);
		if( ship.radarLevel == 0)      g.drawString("Radar: None (Level 0)", x, y);
		else if( ship.radarLevel == 1) g.drawString("Radar: Binary Scanner (Level 1)", x, y);
		else if( ship.radarLevel == 2) g.drawString("Radar: Neutron Scanner (Level 2)", x, y);
		else                           g.drawString("Radar: Quantum Bit Array (Level " + ship.radarLevel + ")", x, y);
		if( ship.autoBreaks )
		{
			y += GameFont.getHeight(g);
			g.drawString("Internal Nullifier", x, y);
		}
		if( ship.canStrafe )
		{
			y += GameFont.getHeight(g);
			g.drawString("Side Thrusters", x, y);
		}
		if( ship.interceptor != null )
		{
			y += GameFont.getHeight(g);
			g.drawString("Interceptors (" + ship.interceptor.name +")", x, y);
		}
		if( ship.canDisguise != null )
		{
			y += GameFont.getHeight(g);
			g.drawString("Stealth Disguise System", x, y);
		}
		if( ship.canWarp )
		{
			y += GameFont.getHeight(g);
			g.drawString("Subspace Warp Engine", x, y);
		}
		if( ship.canCloak )
		{
			y += GameFont.getHeight(g);
			g.drawString("Cloaking Device", x, y);
		}
		if( ship.tetiaryWeapon != null )
		{
			y += GameFont.getHeight(g);
			g.drawString("Extra Armament ("+ ship.tetiaryWeapon.name +")", x, y);
		}
		if( ship.canJam )
		{
			y += GameFont.getHeight(g);
			g.drawString("ECM Jammer", x, y);
		}
	}

	public GameState update( boolean campaign ) {
        
		//Change between campaign and skirmish mode and playin' with XOR
		if( campaign ^ campaignMode )
		{
			campaignMode ^= true;
			if( campaignMode ) 		setCampaignMode();
			else			   		setSkrimishMode();
		}
		
        //Check if the player is holding over any mouse buttons
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() )
        {
        	Button button = iterator.next();
			button.update();
			
			//Clicked as well?
			if( button.mouseOver(key) && key.mosButton1 )
			{
				key.mosButton1 = false;
				button.playClick();
				
				//Determine button effect
				switch( button.getID() )
				{
				
					case BUTTON_START_GAME:
					{
						if( campaignMode ) selectedShip = ship;
						
						if( !campaignMode && !selectOthers )
						{
							selectOthers = true;
							selectedShip = ship;
							newSkirmishFleet();
						}
						else 
						{
							if( !campaignMode )
							{
								if( doPlanets )
								{
									SpawnPoint planet = new SpawnPoint( Type.PLANET );
									planet.pos = new Vector( universeSize*GameEngine.getScreenWidth()/2, universeSize*GameEngine.getScreenHeight()/2 );
									skirmishFleet.add( planet );
								}
								buttonList.get(BUTTON_ADD_SHIP).hide();
								buttonList.get(BUTTON_TEAM).hide();
								buttonList.get(BUTTON_PLANETS).show();
								buttonList.get(BUTTON_MAP_SIZE).show();
								buttonList.get(BUTTON_START_GAME).setText("SELECT SHIP");
							}
							
							fleetList.clear();							//Free some memory
							selectOthers = false;
							return GameWindow.GameState.GAME_PLAYING;
						}
						break;
					}
					
					case BUTTON_NEXT_SHIP:
					{
						if( !select.hasNext() ) select = shipList.keySet().iterator();
						setCurrentShip( shipList.get(select.next()) );
						break;
					}
					
					case BUTTON_PLANETS:
					{
						doPlanets ^= true;
						break;
					}
					
					case BUTTON_MAP_SIZE:
					{
						universeSize++;
						if(universeSize > 10) universeSize = 1;
						break;
					}
					
					case BUTTON_ADD_SHIP:
					{
						addShipToFleet();
						break;
					}

					case BUTTON_TEAM:
					{
						if( skirmishTeam.equals("GOOD") ) skirmishTeam = "EVIL";
						else							  skirmishTeam = "GOOD";
						break;
					}
					
					case BUTTON_UPGRADE:
					{
						drawUpgradeScreen = true;
				        Iterator<Button> hider = buttonList.values().iterator();
				        while( hider.hasNext() ) hider.next().show();
				        
				        if( techList.size() == 1 ) buttonList.get(BUTTON_SPECIAL).hide();
				        if( !nullifier ) 		   buttonList.get(BUTTON_NULLFIER).hide();
				        if( !strafing ) 		   buttonList.get(BUTTON_STRAFING).hide();
				        if( weaponList.size() == 0 )
				        {
				        	buttonList.get(BUTTON_PRIMARY).hide();
				        	buttonList.get(BUTTON_SECONDARY).hide();
				        }
				        
				        buttonList.get(BUTTON_ADD_SHIP).hide();
				        buttonList.get(BUTTON_PLANETS).hide();
				        buttonList.get(BUTTON_MAP_SIZE).hide();
				        buttonList.get(BUTTON_TEAM).hide();
				        buttonList.get(BUTTON_NEXT_SHIP).hide();
						buttonList.get(BUTTON_UPGRADE).hide();
						buttonList.get(BUTTON_START_GAME).hide();
						break;
					}
					
					case BUTTON_BACK:
					{
						drawUpgradeScreen = false;
				        Iterator<Button> hider = buttonList.values().iterator();
				        while( hider.hasNext() ) hider.next().hide();

						buttonList.get(BUTTON_UPGRADE).show();
						buttonList.get(BUTTON_START_GAME).show();
						break;
					}
					
					case BUTTON_PRIMARY:
					{
						if( weaponList.isEmpty() ) break;

						int next = weaponList.indexOf(ship.primary);
						if( next == -1 ) next = 0;
						else next++;
						
						if( next >= weaponList.size() ) ship = new SpaceshipTemplate(ship, null, ship.secondary, ship.tetiaryWeapon, ship.radarLevel, ship.lifeMax, ship.shieldMax, ship.energyMax);
						else 							ship = new SpaceshipTemplate(ship, weaponList.get(next), ship.secondary, ship.tetiaryWeapon, ship.radarLevel, ship.lifeMax, ship.shieldMax, ship.energyMax);
						
						break;
					}
					
					case BUTTON_SECONDARY:
					{
						if( weaponList.isEmpty() ) break;

						int next = weaponList.indexOf(ship.secondary);
						if( next == -1 ) next = 0;
						else next++;
						
						if( next >= weaponList.size() ) ship = new SpaceshipTemplate(ship, ship.primary, null, ship.tetiaryWeapon, ship.radarLevel, ship.lifeMax, ship.shieldMax, ship.energyMax);
						else 							ship = new SpaceshipTemplate(ship, ship.primary, weaponList.get(next), ship.tetiaryWeapon, ship.radarLevel, ship.lifeMax, ship.shieldMax, ship.energyMax);
						
						break;
					}
					
					case BUTTON_RADAR:
					{
						short radar = ship.radarLevel;
						radar++;
						if( radar > maxRadarLevel ) radar = 0;
						ship = new SpaceshipTemplate(ship, ship.primary, ship.secondary, ship.tetiaryWeapon, radar, ship.lifeMax, ship.shieldMax, ship.energyMax);
						break;
					}
					
					case BUTTON_SPECIAL:
					{
						nextSpecialModule();
						break;
					}

					case BUTTON_NULLFIER:
					{
						if( ship.autoBreaks )
							ship = new SpaceshipTemplate(ship, ship.canJam, ship.canStrafe, ship.canWarp, ship.interceptor, false, ship.canCloak, ship.canDisguise);  
						else
							ship = new SpaceshipTemplate(ship, ship.canJam, ship.canStrafe, ship.canWarp, ship.interceptor, true, ship.canCloak, ship.canDisguise);  
						break;
					}

					case BUTTON_STRAFING:
					{
						if( ship.canStrafe )
							ship = new SpaceshipTemplate(ship, ship.canJam, false, ship.canWarp, ship.interceptor, ship.autoBreaks, ship.canCloak, ship.canDisguise);  
						else
							ship = new SpaceshipTemplate(ship, ship.canJam, true, ship.canWarp, ship.interceptor, ship.autoBreaks, ship.canCloak, ship.canDisguise);  
						break;
					}
				}
			}
        }
        
		return GameState.GAME_SELECT_SHIP;
	}
	
	/**
	 * JJ> This loads all the *.ship files from the data folder and puts them into a list
	 */
	private void parseShipList() {
		String[] fileList = ResourceMananger.getFileList("/data/ships/");

		shipList = new HashMap<String, SpaceshipTemplate>();
		for( String fileName : fileList )
		{
			if( !fileName.endsWith(".ship") ) continue;
			
			try
			{
				shipList.put( fileName.substring(fileName.lastIndexOf('/')+1), new SpaceshipTemplate(fileName) );
			}
			catch(Exception e)
			{
				Log.warning("Failed to load Spaceship - " + e);
			}
		}
	}
	
	/**
	 * JJ> Changes the current ship we are focusing on
	 * @param index The index number in the ship list
	 */
	public void setCurrentShip( SpaceshipTemplate ship ) {	
		//Make it look computerized green
		spaceshipImage = ship.image.clone();
		spaceshipImage.setColorTint(0, 255, 0);
		spaceshipImage.resize(GameEngine.getScreenWidth()/4, GameEngine.getScreenWidth()/4);
		spaceshipImage.setDirection((float)-Math.PI/2);
		
		this.ship = ship;
	}
	
	/**
	 * JJ> Spawns the ship that is currently selected in the Shipyard menu
	 * @param pos The Vector position in the world where it is spawned
	 * @param keys What Input controller controls this ship? (could be AI)
	 * @param world What world is this spaceship spawning in, this should be a Game object
	 * @return The new Spaceship ready to fight!
	 */
	public Spaceship spawnSelectedShip(Vector pos, Game world, aiType AI, String team) {
		
		AI produced = new AI(selectedShip, team, world);
		produced.instantiate(pos, AI);
				
		return produced;
	}

	private void setSkrimishMode() {
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().hide();

		//Reset values
		universeSize = 4;
		doPlanets = true;

        //These are visible
        buttonList.get(BUTTON_START_GAME).show();
		buttonList.get(BUTTON_NEXT_SHIP).show();
		buttonList.get(BUTTON_MAP_SIZE).show();
		buttonList.get(BUTTON_PLANETS).show();
		buttonList.get(BUTTON_START_GAME).setText("SELECT SHIP");
		
		//Reset any previous selection
		resetUpgrades();
		select = shipList.keySet().iterator();
		setCurrentShip( shipList.get(select.next()) );		
		
		campaignMode = false;
	}
	
	private void setCampaignMode() {
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().hide();

        //These are visible
		buttonList.get(BUTTON_START_GAME).show();		
		buttonList.get(BUTTON_UPGRADE).show();
		buttonList.get(BUTTON_START_GAME).setText("START GAME");
        
		//Upgrades
		resetUpgrades();
		campaignMode = true;
	}

	
	private void calculateTechLevel() {
		techLevel = ship.radarLevel*2;
		if( ship.primary != null ) 			techLevel += ship.primary.techCost;
		if( ship.secondary != null ) 		techLevel += ship.secondary.techCost;
		if( ship.tetiaryWeapon != null ) 	techLevel += ship.tetiaryWeapon.techCost;
		if( ship.autoBreaks ) 				techLevel += 6;
		if( ship.canStrafe ) 				techLevel += 2;
		if( ship.canCloak ) 				techLevel += 4;
		if( ship.canJam ) 					techLevel += 2;
		if( ship.canWarp ) 					techLevel += 3;
		if( ship.canDisguise != null ) 		techLevel += 4;
		if( ship.interceptor != null ) 		techLevel += 5;
	}
	
	private void nextSpecialModule() {
		
		//Reset if we are at the end
		if( !currentTech.hasNext() ) currentTech = techList.iterator();
		
		switch( currentTech.next() )
		{
			//Disable all special mods
			default: case NONE: ship = new SpaceshipTemplate(ship, false, ship.canStrafe, false, null, ship.autoBreaks, false, null); break;
			case WARP:			ship = new SpaceshipTemplate(ship, false, ship.canStrafe, true, null, ship.autoBreaks, false, null);  break;		
			case CLOAK:			ship = new SpaceshipTemplate(ship, false, ship.canStrafe, false, null, ship.autoBreaks, true, null);  break;
			case ECM:			ship = new SpaceshipTemplate(ship, true, ship.canStrafe, false, null, ship.autoBreaks, false, null);  break;
			case INTERCEPTOR:	
			try 
			{
				ship = new SpaceshipTemplate(ship, false, ship.canStrafe, false, new SpaceshipTemplate("heavyfighet.interceptor"), ship.autoBreaks, false, null);
			}
			catch (Exception e) 
			{
				Log.warning("Applying special module Interceptor: " + e);
			}  break;
			//TODO: disguise, tetiary weapon
		}
	}
	
	/**
	 * JJ> Removes all available upgrades
	 */
	public void resetUpgrades(){
		weaponList = new ArrayList<Weapon>();
		techList = new ArrayList<SpecialModule>();
		maxRadarLevel = 0;
		techList.add(SpecialModule.NONE);
		currentTech = techList.iterator();
		nullifier = false;
		strafing = false;
		maxTechLevel = 2;
	}

	public void addWeapon(Weapon weapon) {
		if( weaponList.contains(weapon) ) return;
		weaponList.add(weapon);
	}

	public void addModule(SpecialModule module) {
		if( techList.contains(module) ) return;
		
		//These two tech stack with other techs
		if( module == SpecialModule.STRAFING )
		{
			strafing = true; 
			return;
		}
		if( module == SpecialModule.NULLIFIER )
		{
			nullifier = true; 
			return;
		}
		
		//Other tech
		techList.add(module);
		currentTech = techList.iterator();
	}
	
	/**
	 * JJ> Adds a single ship to the list of all ships
	 */
	private void addShipToFleet() {
		
		//Image
		Image2D image = ship.image.clone();		
		image.setDirection( -(float)(Math.PI/2) );
		if(skirmishTeam.equals("GOOD")) image.setColorTint(0, 255, 0);
		else							
		{
			buttonList.get(BUTTON_START_GAME).show();
			image.setColorTint(255, 0, 0);
		}
		image.resize( GameEngine.getScreenWidth()/16, GameEngine.getScreenWidth()/16 );
		fleetList.add( image );
		
		//Spawn object
		SpawnPoint spawn = new SpawnPoint( Type.SPACESHIP );
		spawn.ai = aiType.CONTROLLER;
		spawn.name = ship.getFilePath();
		spawn.team = skirmishTeam;
		skirmishFleet.add( spawn );
	}
	
	/**
	 * JJ> Enables selection of enemy and ally ships
	 */
	private void newSkirmishFleet() {
		
		//Allow the player to add additional ships to this fleet
		buttonList.get(BUTTON_ADD_SHIP).show();
		buttonList.get(BUTTON_TEAM).show();
		buttonList.get(BUTTON_START_GAME).hide();
		buttonList.get(BUTTON_PLANETS).hide();
		buttonList.get(BUTTON_MAP_SIZE).hide();
		buttonList.get(BUTTON_START_GAME).setText("START GAME");

		//Image
		Image2D image = ship.image.clone();		
		image.setDirection( -(float)(Math.PI/2) );
		image.resize( GameEngine.getScreenWidth()/16, GameEngine.getScreenWidth()/16 );
		fleetList.clear();
		fleetList.add( image );
		
		//Spawn object
		skirmishTeam = "EVIL";
		SpawnPoint spawn = new SpawnPoint( Type.PLAYER );
		spawn.name = selectedShip.getFilePath();
		spawn.team = "GOOD";
		skirmishFleet.clear();
		skirmishFleet.add( spawn );
	}

	/**
	 * JJ> Gets both the enemy and ally fleet including the player ship as an array
	 * @return Both fleets plus the player
	 */
	public ArrayList<SpawnPoint> getSkirimishFleet(){
		return skirmishFleet;
	}
}
