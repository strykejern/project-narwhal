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
	
	private HashMap<Integer, Button> buttonList;
	static final int BUTTON_START_GAME = 0;
	static final int BUTTON_NEXT_SHIP = 1;
	
	public Shipyard(Input key) {
		parseShipList();
		ship = shipList.get(0);
		image = ship.getImage().clone();
		
		//Ready font
		font = new GameFont();
		
		//Ready buttons
		buttonList = new HashMap<Integer, Button>();
    	Vector size = new Vector( 200, 50 );
    	Vector pos = new Vector( Video.getScreenWidth()-size.x, Video.getScreenHeight()-size.y );
		buttonList.put(BUTTON_START_GAME, new Button(pos, size, "Start Game", BUTTON_START_GAME, pos));

		//Make it look computerized green
		image.setColorTint(0, 255, 0);
		image.resize(Video.getScreenWidth()/4, Video.getScreenWidth()/4);
		image.setDirection((float)-Math.PI/2);
		
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
		g.drawImage(bg.toImage(), 0, 0, null);	
		g.drawImage(right.toImage(), bg.getWidth(), 0, null);
		
		//Draw the ship
		image.rotate(0.01f);
		g.drawImage(image.toImage(), bg.getWidth()/2-image.getWidth()/2, bg.getHeight()/2-image.getHeight()/2, null);
		
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

}
