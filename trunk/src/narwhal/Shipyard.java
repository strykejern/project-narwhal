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
import gameEngine.ResourceMananger;
import gameEngine.Vector;
import gameEngine.Video;
import gameEngine.GameWindow.gameState;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import narwhal.GameFont.FontType;

public class Shipyard {
	private Image2D bg;
	private Image2D image;
	private Image2D right;
	private Spaceship ship;
	private GameFont font;
	private Input key;
	
	ArrayList<Spaceship> shipList;
	private int currentShip;
	
	private HashMap<Integer, Button> buttonList;
	static final int BUTTON_START_GAME = 0;
	static final int BUTTON_NEXT_SHIP = 1;
		
	public Shipyard(Input key) {
		
		//Load the list of ships and get the first ship in the list
		parseShipList();
		setCurrentShip(0);
		
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
		g.drawString("WEAPON SYSTEMS: " + ship.weapon.name, x, y);
		y += font.getHeight(g);
		g.drawString("Damage: " + ship.weapon.damage, x, y);
		y += font.getHeight(g);
		g.drawString("Hull Penentration: " + ship.weapon.lifeMul*100 + "%", x, y);
		y += font.getHeight(g);
		g.drawString("Shield Penentration: " + ship.weapon.shieldMul*100 + "%", x, y);
		y += font.getHeight(g);
		g.drawString("Energy Cost: " + ship.weapon.cost, x, y);
		y += font.getHeight(g)*3;
		
		//Ship description
		g.drawString("SHIP PROPERTIES: ", x, y);
		y += font.getHeight(g);
		g.drawString("Hull: " + ship.lifeMax, x, y);
		y += font.getHeight(g);
		g.drawString("Shields: " + ship.shieldMax, x, y);
		y += font.getHeight(g);
		g.drawString("Energy: " + ship.energyMax, x, y);
		y += font.getHeight(g);
		
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
						setCurrentShip(++currentShip);
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

		shipList = new ArrayList<Spaceship>();
		for( String fileName : fileList )
		{
			if( !fileName.endsWith(".ship") ) continue;
			shipList.add( new Spaceship(fileName) );
		}
	}
	
	/**
	 * JJ> Changes the current ship we are focusing on
	 * @param index The index number in the ship list
	 */
	private void setCurrentShip( int index ) {
		
		//Clip to valid value
		if(index < 0 || index >= shipList.size()) index = 0;
		
		//Change current ship
		currentShip = index;
		ship = shipList.get(index);
		
		//Make it look computerized green
		image = ship.getImage().clone();	
		image.setColorTint(0, 255, 0);
		image.resize(Video.getScreenWidth()/4, Video.getScreenWidth()/4);
		image.setDirection((float)-Math.PI/2);
	}


}
