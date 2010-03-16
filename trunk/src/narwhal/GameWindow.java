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

import gameEngine.*;

import java.awt.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;


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
	private gameState state;
	
	public static enum gameState {
		GAME_MENU,
		GAME_PLAYING, 
		GAME_EXIT
	}
	
	public static void main(String[] args) {
    	//Initialize the logging system, do this first so that error logging happens correctly.
    	Log.initialize();

		//Now initialize Video settings
        Video.initialize();
        Video.disableHighQualityGraphics();
        Video.setResolution(800, 600);
        //Video.setFullscreen();

		//Initialize the frame window where we draw stuff
    	JFrame parentWindow = new JFrame("Project Narwhal", Video.getGraphicsConf());		
    	parentWindow.getContentPane().add(new GameWindow(parentWindow));
    	parentWindow.setSize(Video.getResolution());
		parentWindow.setResizable(false);
		//parentWindow.setUndecorated(true);								//Remove borders
        parentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentWindow.setVisible( true );
       	parentWindow.setIgnoreRepaint( true );								//This ensures there is no flickering       	
  	}
	
	public GameWindow(JFrame frame) {
    	
		//The actual frame
		this.frame = frame;
		Image2D icon = new Image2D("data/icon.png");
		frame.setIconImage( icon.toImage() );

		//Input controls
		frame.addKeyListener(this);
		frame.addMouseListener(this);
		keys = new Input();	
		
       	//Start in the main menu
       	state = gameState.GAME_MENU;
       	theMenu = new MainMenu(keys);
       	theGame = new Game(keys);
		
		//Thread (do last so that everything above is properly loaded before the main loop begins)
		new Thread(this).start();
	}
	
	//JJ> This is the main game loop
	public void run() {
		
		// Remember the starting time
    	long tm = System.currentTimeMillis();
    	
		// Set the blank cursor to the JFrame.
		//frame.getContentPane().setCursor(Video.blankCursor);
		
    	//Load game resources TODO: Move this somewhere else
		//Sound crash = new Sound("data/crash.au");
 	
		while( state != gameState.GAME_EXIT )
    	{
			//Update mouse position within the frame
			Point mouse = frame.getMousePosition();
			if(mouse != null) keys.update(mouse.x, mouse.y);
			
			try
			{
				while (painting) Thread.sleep(1);
			}
			catch (Exception e) { Log.warning(e); }
			
			if(state == gameState.GAME_PLAYING) state = theGame.update();
			else if(state == gameState.GAME_MENU) state = theMenu.update();
			repaint();
			
			try 
			{
				tm += TARGET_FPS;
				Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
			}
			catch (Exception e) { Log.warning(e); }
		}
		
		//TODO: This never happens!?!
	   	Log.close();
	}
		
	
	/**
	 * JJ> Paints every object of interest
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics rawGraphics) {
		painting = true;
		//Convert to the Graphics2D object which allows us more functions
		Graphics2D g = (Graphics2D) rawGraphics;
		
		//Set quality mode
		Video.getGraphicsSettings(g);
		
		if(state == gameState.GAME_PLAYING) theGame.draw(g);
		else if(state == gameState.GAME_MENU) theMenu.draw(g);

		//Done drawing this frame
		g.dispose();
		painting = false;
	}
	private boolean painting = false;
	
	static public boolean isInScreen(Rectangle rect)
	{
		if( rect.x < -rect.width || rect.x > Video.getScreenWidth() ) return false;
		if( rect.y < -rect.height || rect.y > Video.getScreenHeight() ) return false;
		return true;
	}
	
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
