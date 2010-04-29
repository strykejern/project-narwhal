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
import narwhal.GameFont;
import narwhal.MainMenu;
import narwhal.Shipyard;
import narwhal.GameFont.FontType;


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
       	campaign = new CampaignScreen(keys, selectShip);
       	
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

			//TODO: bad change cursor every frame?
			if(state == GameState.GAME_PLAYING)
				frame.getContentPane().setCursor(GameEngine.BLANK_CURSOR);	
			else
		    	frame.getContentPane().setCursor( null );
				
			
			if(state == GameState.GAME_PLAYING)		
			{
				//Start a new game if needed
				if(theGame == null)
				{
					if( campaign.active ) theGame = new Game( keys, selectShip, campaign.getLevelSpawnList(), campaign.getUniverseSize() );
					else				  theGame = new Game( keys, selectShip, null, 4 );
				}
				
				state = theGame.update();				
			}
			else if( state == GameState.GAME_SELECT_SHIP) 
			{
				theGame = null;
				state = selectShip.update( campaign.active );
			}
			else if( state == GameState.GAME_END_CURRENT )
			{
		       	theGame = null;
		       	campaign.active = false;
		       	state = theMenu.showMainMenu(false);
			}
			else if( state == GameState.GAME_CAMPAIGN_SCREEN )
			{
				//Start new campaign
				if( !campaign.active )
				{
					selectShip.resetUpgrades();
					campaign.loadMission("data/campaign/level1.mission");
				}
				
				state = campaign.update();
			}
			else if( state == GameState.GAME_MENU)	      state = theMenu.update( theGame != null );

			if(!painting) repaint();
						
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
				
		if(state == GameState.GAME_PLAYING && theGame != null)
		{
			theGame.draw(g);
			
			//Fade out the screen
			if( theGame.isEnded() || theGame.victory() )
			{
				screenFade = Math.min(1, screenFade+0.004f);
				g.setColor( new Color(0, 0, 0, screenFade) );
				g.fillRect(0, 0, GameEngine.getScreenWidth(), GameEngine.getScreenHeight());
				
				GameFont.set(g, FontType.FONT_MENU, new Color(0.05f, 1.0f, 0.05f, 1-screenFade), 42);
				if( theGame.victory() ) g.drawString("VICTORY", GameEngine.getScreenWidth()/2-GameFont.getWidth("VICTORY", g)/2, GameEngine.getScreenHeight()/2);
				else					g.drawString("DEFEAT", GameEngine.getScreenWidth()/2-GameFont.getWidth("DEFEAT", g)/2, GameEngine.getScreenHeight()/2);
				
				if( screenFade == 1 || keys.escape )
				{
					screenFade = 0;
					
					//Prepare next level
					if( campaign.active )
					{
						if ( campaign.alwaysWin() || theGame.victory() )
						{
							state = GameState.GAME_CAMPAIGN_SCREEN;
							campaign.next();
						}
						else
						{
							state = GameState.GAME_MENU;
							theMenu.showRetry();
						}
						theGame = null;
					}
					//Go back to the menu
					else state = GameState.GAME_END_CURRENT;
				}
			}
		}
		else if(state == GameState.GAME_MENU) 					theMenu.draw(g, theGame);
		else if(state == GameState.GAME_SELECT_SHIP) 			selectShip.draw(g);
		else if(state == GameState.GAME_CAMPAIGN_SCREEN) 		campaign.draw(g);

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
