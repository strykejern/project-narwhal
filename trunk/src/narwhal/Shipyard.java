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
import gameEngine.Video;
import gameEngine.GameWindow.gameState;

import java.awt.Color;
import java.awt.Graphics2D;
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
	private GameFont font;
	private Input key;
	
	//The blueprints of all ships
	private HashMap<String, SpaceshipTemplate> shipList;
	
	//Clickable buttons
	private HashMap<Integer, Button> buttonList;
	static final int BUTTON_START_GAME = 0;
	static final int BUTTON_NEXT_SHIP = 1;
		
	public Shipyard(Input key) {
		
		//Load the list of ships and get the first ship in the list
		parseShipList();
		select = shipList.keySet().iterator();
		setCurrentShip(select.next());		
		
		//Ready font
		font = new GameFont();
		
		//Ready buttons
		buttonList = new HashMap<Integer, Button>();
    	Vector size = new Vector( 200, 50 );
    	Vector pos = new Vector( Video.getScreenWidth()-size.x, Video.getScreenHeight()-size.y );
		buttonList.put(BUTTON_START_GAME, new Button(pos, size, "Start Game", BUTTON_START_GAME, pos));
		pos.y -= size.y;
		buttonList.put(BUTTON_NEXT_SHIP, new Button(pos, size.dividedBy(2), "Next", BUTTON_NEXT_SHIP, pos));
		
		//Ready background
		bg = new Image2D("/data/shipyard.png");
		bg.resize((int)(Video.getScreenWidth()*0.50f), Video.getScreenHeight());
		right = new Image2D("/data/interface.png");
		right.resize(Video.getScreenWidth() - bg.getWidth(), Video.getScreenHeight());
		
		//Input controller
		this.key = key;
	}
	
	/**
	 * JJ> This function gets a spaceship from the list of all spaceships that are current loaded into 
	 *     memory by this Shipyard object and instantiates it into a Universe as a new unique object.
	 * @param name The String reference to the list of spaceships currently loaded ("raptor.ship" for example)
	 * @param pos The Vector position in the world where it is spawned
	 * @param keys What Input controller controls this ship? (could be AI)
	 * @param universeSize A Vector that tells us how big is the world around us is
	 * @param particleEngine Which ParticleEngine are we supposed to use to spawn particles
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
		
		g.drawString("WEAPON SYSTEM: " + wpn.name, x, y);
		y += font.getHeight(g);
		g.drawString("Damage: " + wpn.damage, x, y);
		y += font.getHeight(g);
		g.drawString("Hull Penentration: " + (int)(wpn.lifeMul*100) + "%", x, y);
		y += font.getHeight(g);
		g.drawString("Shield Penentration: " + (int)(wpn.shieldMul*100) + "%", x, y);
		y += font.getHeight(g);
		g.drawString("Energy Cost: " + wpn.cost, x, y);
		return y;
	}
	
	public void draw(Graphics2D g) {
		final int OFFSET_X = Video.getScreenWidth()/32;
		final int OFFSET_Y = Video.getScreenHeight()/16;
		
		//Do first, draw background
		bg.draw(g, 0, 0);
		right.draw(g, bg.getWidth(), 0);
		
		//Draw the ship
		image.rotate(0.01f);
		image.draw(g, bg.getWidth()/2-image.getWidth()/2, bg.getHeight()/2-image.getHeight()/2);
		
		//Ship description
		int x = bg.getWidth() + OFFSET_X;
		int y = OFFSET_Y;
		g.setColor(Color.GREEN);	
		
		//Ship name
		font.set(g, FontType.FONT_MENU);
		g.drawString(ship.name.toUpperCase(), x, y);
		y += font.getHeight(g)*2;

		//Weapon description
		font.set(g, FontType.FONT_NORMAL);
		y += font.getHeight(g);
		y = describeWeapon(g, x, y, ship.primary);
		y += font.getHeight(g);
		y = describeWeapon(g, x, y, ship.secondary);
		y += font.getHeight(g)*3;
		
		//Ship description
		g.drawString("SHIP PROPERTIES: ", x, y);
		y += font.getHeight(g);
		g.drawString("Hull: " + ship.lifeMax, x, y);
		y += font.getHeight(g);
		g.drawString("Shields: " + ship.shieldMax, x, y);
		y += font.getHeight(g);
		g.drawString("Energy: " + ship.energyMax, x, y);
		y += font.getHeight(g)*3;

		//Ship modifications
		g.drawString("SPECIAL MODIFICATIONS: ", x, y);
		y += font.getHeight(g);
		if( ship.radarLevel == 0)      g.drawString("Radar: None (Level 0)", x, y);
		else if( ship.radarLevel == 1) g.drawString("Radar: Binary Scanner (Level 1)", x, y);
		else if( ship.radarLevel == 2) g.drawString("Radar: Neutron Scanner (Level 2)", x, y);
		else                           g.drawString("Radar: Quantum Bit Array (Level " + ship.radarLevel + ")", x, y);
		if( ship.autoBreaks )
		{
			y += font.getHeight(g);
			g.drawString("Internal Nullifier", x, y);
		}
		if( ship.strafe )
		{
			y += font.getHeight(g);
			g.drawString("Side Thrusters", x, y);
		}
		if( ship.interceptor != null )
		{
			y += font.getHeight(g);
			g.drawString("Interceptors (" + ship.interceptor.name +")", x, y);
		}
		if( ship.canDisguise != null )
		{
			y += font.getHeight(g);
			g.drawString("Stealth Disguise System", x, y);
		}

		//Do last, draw all buttons
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().draw(g);
	}

	public gameState update() {
        
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
						return GameWindow.gameState.GAME_PLAYING;
					}
					case BUTTON_NEXT_SHIP:
					{
						if( !select.hasNext() ) select = shipList.keySet().iterator();
						setCurrentShip(select.next());
						break;
					}
				}
			}
        }
        
		return gameState.GAME_SELECT_SHIP;
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
	private void setCurrentShip( String name ) {
				
		//Change current ship
		ship = shipList.get(name);
		
		//Make it look computerized green
		image = ship.image.clone();
		image.setColorTint(0, 255, 0);
		image.resize(Video.getScreenWidth()/4, Video.getScreenWidth()/4);
		image.setDirection((float)-Math.PI/2);
	}
	
	/**
	 * JJ> Spawns the ship that is currently selected in the Shipyard menu
	 * @param pos The Vector position in the world where it is spawned
	 * @param keys What Input controller controls this ship? (could be AI)
	 * @param universeSize A Vector that tells us how big is the world around us is
	 * @param particleEngine Which ParticleEngine are we supposed to use to spawn particles
	 * @return The new Spaceship ready to fight!
	 */
	public Spaceship spawnSelectedShip(Vector pos, Game world, aiType AI, String team) {
		
		AI produced = new AI(ship, team, world);
		produced.instantiate(pos, AI);
				
		return produced;
		
	}


}
