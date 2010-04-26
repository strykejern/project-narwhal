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

/**
 * JJ> This class works like a factory of Spaceships. First it loads all blueprints of all
 *     Spaceship when this class is constructed. Then it can produce new Spaceships using those
 *     blueprints.
 * @author Anders Eie and Johan Jansen
 *
 */
public class Shipyard {
	private Image2D bg;
	private Image2D image;
	private Image2D right;
	private SpaceshipTemplate ship;
	private Iterator<String> select;
	private Input key;
	
	//The blueprints of all ships
	private HashMap<String, SpaceshipTemplate> shipList;
	
	//Avalible upgrades
	private short maxRadarLevel;
	private ArrayList<Weapon> weaponList;
	private boolean cloaking;
	private boolean disguise;
	private boolean tetiary;
	private boolean warp;
	private boolean jamming;
	
	//Clickable buttons
	private HashMap<Integer, Button> buttonList;
	static final int BUTTON_START_GAME = 0;
	static final int BUTTON_NEXT_SHIP = 1;
	static final int BUTTON_UPGRADE = 2;
	static final int BUTTON_BACK = 3;
	static final int BUTTON_PRIMARY = 4;
	static final int BUTTON_SECONDARY = 5;
		
	public Shipyard(Input key) {
		
		//Load the list of ships and get the first ship in the list
		parseShipList();
		select = shipList.keySet().iterator();
		setCurrentShip( shipList.get(select.next()) );		
		
		//Ready background
		bg = new Image2D("/data/shipyard.png");
		bg.resize((int)(GameEngine.getScreenWidth()*0.50f), GameEngine.getScreenHeight());
		right = new Image2D("/data/interface.png");
		right.resize(GameEngine.getScreenWidth() - bg.getWidth(), GameEngine.getScreenHeight());
		
		//Ready buttons
		buttonList = new HashMap<Integer, Button>();
    	Vector size = new Vector( 200, 50 );
    	Vector pos = new Vector( GameEngine.getScreenWidth()-size.x, GameEngine.getScreenHeight()-size.y );
		buttonList.put(BUTTON_START_GAME, new Button(pos, size, "Start Game", BUTTON_START_GAME, pos));
		buttonList.put(BUTTON_BACK, new Button(pos, size, "Back", BUTTON_BACK, pos));
		pos.y -= size.y;
		buttonList.put(BUTTON_NEXT_SHIP, new Button(pos, size.dividedBy(2), "Next", BUTTON_NEXT_SHIP, pos));
		buttonList.put(BUTTON_UPGRADE, new Button(pos, size, "UPGRADE", BUTTON_UPGRADE, pos));

		//The upgrade buttons
    	pos = new Vector( bg.getWidth()/2, bg.getHeight()/4 );
    	size = new Vector( 240, 40 );
		buttonList.put(BUTTON_PRIMARY, new Button(pos, size, "Primary Weapon", BUTTON_PRIMARY, pos));
		pos.y -= size.y;
		buttonList.put(BUTTON_SECONDARY, new Button(pos, size, "Secondary Weapon", BUTTON_SECONDARY, pos));

		buttonList.get(BUTTON_PRIMARY).setColor( new Color(255, 76, 76), new Color(255, 44, 44, 128) );
		buttonList.get(BUTTON_SECONDARY).setColor( new Color(255, 76, 76), new Color(255, 44, 44, 128) );
		
		//Upgrades
		resetUpgrades();
		
		//Input controller
		this.key = key;
	}
	
	public void resetUpgrades(){
		weaponList = new ArrayList<Weapon>();
		maxRadarLevel = 1;
		cloaking = false;
		disguise = false;
		tetiary = false;
		warp = false;
		jamming = false;
		
		weaponList.add( new Weapon("protonmissile.wpn") );
		weaponList.add( new Weapon("deathray.wpn") );
		weaponList.add( new Weapon("gatlinggun.wpn") );
		weaponList.add( new Weapon("lasercannon.wpn") );
	}
	
	/**
	 * JJ> This function gets a spaceship from the list of all spaceships that are current loaded into 
	 *     memory by this Shipyard object and instantiates it into a Universe as a new unique object.
	 * @param name The String reference to the list of spaceships currently loaded ("raptor.ship" for example)
	 * @param pos The Vector position in the world where it is spawned
	 * @param keys What Input controller controls this ship? (could be AI)
	 * @param world What world is this spaceship spawning in, this should be a Game object
	 * @return The new Spaceship ready to fight!
	 */
	public Spaceship spawnShip(String name, Vector pos, Game world, aiType AI, String team) {
		
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

	public Spaceship spawnShip(SpaceshipTemplate template, Vector pos, Game world, aiType AI, String team) {
		AI produced;
		produced = new AI(template, team, world);
		produced.instantiate(pos, AI);
				
		return produced;
	}
	
	public String[] getShipList(){
		return shipList.keySet().toArray( new String[0] );
	}

	private int describeWeapon(Graphics2D g, int x, int y, Weapon wpn){
		
		//No weapon
		if( wpn == null )
		{
			g.drawString("WEAPON SYSTEM: N/A", x, y);
			return y;
		}
		
		//Draw weapon image
		Image icon = GameEngine.getParticleEngine().getParticleIcon(wpn.particle).getImage();
		if(icon.getWidth(null) < 175)
		{
			if(icon.getHeight(null) < 100)
				 g.drawImage(icon, GameEngine.getScreenWidth()-icon.getWidth(null)-30, y, null);
			else g.drawImage(icon, GameEngine.getScreenWidth()-icon.getWidth(null)-30, y, icon.getWidth(null), 80, null);
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
		
		//Do first, draw background
		bg.draw(g, 0, 0);
		right.draw(g, bg.getWidth(), 0);
		
		//Draw the ship
		image.rotate(0.01f);
		image.draw(g, bg.getWidth()/2-image.getWidth()/2, bg.getHeight()/2-image.getHeight()/2);
		
		//Ship description
		int x = bg.getWidth() + OFFSET_X;
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
	
	
	
	public void describeSpecialMods( Graphics2D g, int x, int y ){
		
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

	public GameState update() {
        
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
						return GameWindow.GameState.GAME_PLAYING;
					}
					case BUTTON_NEXT_SHIP:
					{
						if( !select.hasNext() ) select = shipList.keySet().iterator();
						setCurrentShip( shipList.get(select.next()) );
						break;
					}
					case BUTTON_UPGRADE:
					{
						buttonList.get(BUTTON_UPGRADE).hide();
						buttonList.get(BUTTON_START_GAME).hide();
						buttonList.get(BUTTON_BACK).show();
						buttonList.get(BUTTON_PRIMARY).show();
						buttonList.get(BUTTON_SECONDARY).show();
						break;
					}
					case BUTTON_BACK:
					{
						buttonList.get(BUTTON_UPGRADE).show();
						buttonList.get(BUTTON_START_GAME).show();
						buttonList.get(BUTTON_BACK).hide();
						buttonList.get(BUTTON_PRIMARY).hide();
						buttonList.get(BUTTON_SECONDARY).hide();
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
		image = ship.image.clone();
		image.setColorTint(0, 255, 0);
		image.resize(GameEngine.getScreenWidth()/4, GameEngine.getScreenWidth()/4);
		image.setDirection((float)-Math.PI/2);
		
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
		
		AI produced = new AI(ship, team, world);
		produced.instantiate(pos, AI);
				
		return produced;
	}

	public void enableSelection() {
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().hide();
		
        //These are visible
        buttonList.get(BUTTON_START_GAME).show();
		buttonList.get(BUTTON_NEXT_SHIP).show();
		
		//Reset any previous selection
		select = shipList.keySet().iterator();
		setCurrentShip( shipList.get(select.next()) );		
	}
	
	public void disableSelection() {
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().hide();

        //These are visible
		buttonList.get(BUTTON_START_GAME).show();		
		buttonList.get(BUTTON_UPGRADE).show();
	}
	
}
