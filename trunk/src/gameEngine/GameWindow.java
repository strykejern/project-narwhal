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


import java.awt.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import narwhal.CampaignScreen;
import narwhal.Game;
import narwhal.MainMenu;
import narwhal.Shipyard;
import narwhal.SpaceshipTemplate;


/**
 * JJ> Main game class, here the important top stuff happens
 * @author Johan Jansen and Anders Eie
 *
 */
public class GameWindow extends JPanel implements Runnable, KeyListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private static final int TARGET_FPS = 1000 / 60;		//60 times per second
	private JFrame frame;
	private Input keys;
	
	private Game theGame;
	private MainMenu theMenu;
	private GameState state;
	private Shipyard selectShip;
	private CampaignScreen campaign;
	
	public static enum GameState {
		GAME_MENU,
		GAME_PLAYING, 
		GAME_EXIT,
		GAME_END_CURRENT,
		GAME_START_SKIRMISH,
		GAME_START_CAMPAIGN,
		GAME_SELECT_SHIP,
		GAME_CAMPAIGN_SCREEN,
	}
		
	public GameWindow(JFrame frame) {
    	
		//The actual frame
		this.frame = frame;
		Image2D icon = new Image2D("/data/icon.png");
		frame.setIconImage( icon.getSnapshot() );

		//Input controls
		frame.addKeyListener(this);
		frame.addMouseListener(this);
		keys = new Input();	
		
       	//Start in the main menu
       	state = GameState.GAME_MENU;
       	theMenu = new MainMenu(keys);
       	selectShip = new Shipyard(keys);
       	campaign = new CampaignScreen(keys);
       	
		//Thread (do last so that everything above is properly loaded before the main loop begins)
       	Thread mainLoop = new Thread(this);
       	mainLoop.setPriority(Thread.MAX_PRIORITY);
       	mainLoop.start();
	}
	
	//JJ> This is the main game loop
	public void run() {
		
		// Remember the starting time
    	long tm = System.currentTimeMillis();
    			 
		while( state != GameState.GAME_EXIT )
    	{
			//Update mouse position within the frame
			keys.update(frame.getMousePosition());			

			try
			{
				while (painting) Thread.sleep(0, 5000);
			}
			catch (Exception e) { Log.warning(e); }

			if(state == GameState.GAME_PLAYING)
				frame.getContentPane().setCursor(GameEngine.BLANK_CURSOR);	//TODO: bad change cursor every frame?
			else
		    	frame.getContentPane().setCursor( null );
				
			if(state == GameState.GAME_PLAYING)		state = theGame.update();
			else if(state == GameState.GAME_MENU)	state = theMenu.update( theGame != null );
			else if(state == GameState.GAME_SELECT_SHIP) state = selectShip.update();
			else if( state == GameState.GAME_START_SKIRMISH )
			{
				selectShip.enableSelection();
				theGame = new Game(keys, selectShip, null, 4);
		       	state = GameState.GAME_SELECT_SHIP;
			}
			else if( state == GameState.GAME_START_CAMPAIGN )
			{
				selectShip.disableSelection();
				theGame = new Game( keys, selectShip, campaign.getLevelSpawnList(), campaign.getUniverseSize() );
				
				try 
				{
					selectShip.setCurrentShip( new SpaceshipTemplate(campaign.getPlayerShipName()) );
				} 
				catch (Exception e) 
				{
					Log.error("Could not start campaign: " + e);
				}
				
		       	state = GameState.GAME_SELECT_SHIP;
			}
			else if( state == GameState.GAME_END_CURRENT )
			{
		       	theGame = null;
		       	campaign.active = false;
		       	state = theMenu.showMainMenu(false);
			}
			else if( state == GameState.GAME_CAMPAIGN_SCREEN )
			{
				if( !campaign.active ) campaign.loadMission("data/campaign/level4.mission");
				state = campaign.update();
			}
			
			//This is so that the player won't open the menu after the current game has ended
			if( theGame != null && state == GameState.GAME_MENU && (theGame.isEnded() || theGame.victory()) )
			{
				screenFade = 1;
				state = GameState.GAME_PLAYING;
			}

			repaint();
						
			try 
			{
				tm += TARGET_FPS;
				Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
			}
			catch (Exception e) { Log.warning(e); }
		}
		
		GameEngine.exitGame(0);
	}
	
		
	/**
	 * JJ> Paints every object of interest
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	private float screenFade = 0;
	private boolean painting = false;
	public void paint(Graphics rawGraphics) {
		painting = true;
		
		//Convert to the Graphics2D object which allows us more functions
		Graphics2D g = (Graphics2D) rawGraphics;
				
		//Set quality mode
		GameEngine.getGraphicsSettings(g);
				
		if(state == GameState.GAME_PLAYING)
		{
			theGame.draw(g);
			
			//Fade out the screen
			if( theGame.isEnded() || theGame.victory() )
			{
				screenFade = Math.min(1, screenFade+0.002f);
				g.setColor( new Color(0, 0, 0, screenFade) );
				g.fillRect(0, 0, GameEngine.getScreenWidth(), GameEngine.getScreenHeight());
				
				if( screenFade == 1 || keys.escape )
				{
					screenFade = 0;
					
					//Prepeare next level
					if( campaign.active && ( campaign.alwaysWin || theGame.victory() ) )
					{
						state = GameState.GAME_CAMPAIGN_SCREEN;
						campaign.next();
					}
					
					//Go back to the menu
					else state = GameState.GAME_END_CURRENT;
				}
			}
		}
		else if(state == GameState.GAME_MENU) 								theMenu.draw(g, theGame);
		else if(state == GameState.GAME_SELECT_SHIP) 						selectShip.draw(g);
		else if(state == GameState.GAME_CAMPAIGN_SCREEN) 					campaign.draw(g);

		//Done drawing this frame
		g.dispose();
		rawGraphics.dispose();
		painting = false;
	}
	
	//Functions handling input update
	public void keyPressed(KeyEvent key) {
		keys.update(key, true);
	}

	public void keyReleased(KeyEvent key) {
		keys.update(key, false);
	}

	public void keyTyped(KeyEvent key) {	
	}

	public void mouseReleased(MouseEvent mouse) {
		keys.update(mouse, false);
	}
	
	public void mousePressed(MouseEvent mouse) {
		keys.update(mouse, true);
	}

	public void mouseClicked(MouseEvent mouse) 	{}
	public void mouseEntered(MouseEvent mouse) 	{}
	public void mouseExited	(MouseEvent e) 		{}
}
