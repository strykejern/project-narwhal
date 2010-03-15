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

import javax.swing.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * JJ> Main game class, here the important top stuff happens
 * @author Johan Jansen and Anders Eie
 *
 */
public class GameWindow extends JPanel implements Runnable, KeyListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private static final int TARGET_FPS = 1000 / 60;		//60 times per second
	private boolean running;
	private JFrame frame;
	private Input keys;
	
	private Game theGame;
	
	public static void main(String[] args) throws InterruptedException{	
    	//Initialize the logging system, do this first so that error logging happens correctly.
    	Log.initialize();

		//Now initialize Video settings
        Video.initialize();
        Video.enableHighQualityGraphics();
        Video.setResolution(800, 640);
        //Video.setFullscreen();

		//Initialize the frame window where we draw stuff
    	JFrame parentWindow = new JFrame("Project Narwhal", Video.getGraphicsConf());		
    	parentWindow.getContentPane().add(new GameWindow(parentWindow));
    	parentWindow.setSize(Video.getResolution());
		parentWindow.setResizable(false);
		//parentWindow.setUndecorated(true);								//Remove borders
        parentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentWindow.setVisible( true );
        
        //This ensures there is no flickering
       	parentWindow.setIgnoreRepaint( true );
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
		
		theGame = new Game(keys);
		
		//Thread (do last so that everything above is properly loaded before the main loop begins)
		running = true;
		new Thread(this).start();
	}
	
	//JJ> This is the main game loop
	public void run() {
		
		// Remember the starting time
    	long tm = System.currentTimeMillis();
    	
		// Set the blank cursor to the JFrame.
		frame.getContentPane().setCursor(Video.blankCursor);
		
    	//Load game resources TODO: Move this somewhere else
		Sound music = new Sound("data/space.ogg");
    	//Sound crash = new Sound("data/crash.au");
    	music.play();
 	
		while(running)
    	{
			int x = MouseInfo.getPointerInfo().getLocation().x - frame.getX();
            int y = MouseInfo.getPointerInfo().getLocation().y - frame.getY();
          
            keys.update(x, y);
			
    		//Basic collision loop (put all detection here)
			/*if(false)
			for( Planet currentPlanet : currentWorld.getPlanetList() )
				if( currentPlanet.collidesWith( ship ) )
				{
					crash.play();
					currentPlanet.collision(ship);
				}*/
			
			theGame.update();

			try 
			{
				tm += TARGET_FPS;
				Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
			}
			catch(InterruptedException e)
			{
				Log.warning(e.toString());
			}		
			
			repaint();
		}
		
		//TODO: This never happens!?!
	   	Log.close();
	}
		
	
	/**
	 * JJ> Paints every object of interest
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics rawGraphics) {
		//Convert to the Graphics2D object which allows us more functions
		Graphics2D g = (Graphics2D) rawGraphics;
		
		//Set quality mode
		Video.getGraphicsSettings(g);
		
		theGame.draw(g);
		
		g.dispose();
	}
	
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

	public void mouseClicked(MouseEvent mouse) {
	}
	public void mouseEntered(MouseEvent mouse) {
	}
	public void mouseExited(MouseEvent mouse) {	
	}


}
