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

import gameEngine.Image2D;
import gameEngine.Log;
import gameEngine.Sound;

import java.awt.*;
import javax.swing.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Random;


/**
 * JJ> Main game class, here the important top stuff happens
 * @author Johan Jansen and Anders Eie
 * @param <OggClip>
 *
 */
public class Game extends JPanel implements Runnable, KeyListener
{
	private static final long serialVersionUID = 1L;
	private static final int TARGET_FPS = 1000 / 60;		//60 times per second
	private boolean running;
	private JFrame frame;
	private String input = "";
	Object ship, planet;
	Background bg;	
	private boolean up, down, left, right;
	
	//Player position in the universe
	Random rand = new Random();
	int x = rand.nextInt(100), y = rand.nextInt(100);
	
	// Create a new blank cursor.
	final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
			new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

	
	public static void main(String[] args) throws InterruptedException{	
		JFrame parentWindow = new JFrame("Project Narwhal");		
    	parentWindow.getContentPane().add(new Game(parentWindow));

    	parentWindow.setSize(800 , 600);
        parentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentWindow.setVisible(true);
   	}
	
	public Game(JFrame frame){
		this.frame = frame;
		Image2D icon = new Image2D("data/icon.png");
		frame.setIconImage( icon.toImage() );
		new Thread(this).start();
		running = true;
		frame.addKeyListener(this);
		bg = new Background(800, 600, addBits(x,y) );
	}
	
	//JJ> This is the main game loop
	public void run() {
		
		// Remember the starting time
    	long tm = System.currentTimeMillis();

    	//Initialize the logging system, do this first so that error logging happens correctly.
    	Log.initialize();
    	
		// Set the blank cursor to the JFrame.
		frame.getContentPane().setCursor(blankCursor);
		
		//Initialize the player ship
		ship = new Object( new Image2D("data/spaceship.png"), 400, 200 );
		ship.sprite.resize(64, 64);
		ship.enableCollision();
		keepPlayerWithinBounds( ship );
		
		//Crashable planet test
		//planet = new Object( new Image2D("data/planet/planet.png"), 400, 200 );
		//planet.enableCollision();
		
		//Play some music
    	Sound music = new Sound("data/test.wav");
    	music.play();

		// da loop
    	while(running)
    	{
    		keepPlayerWithinBounds(ship);
    		repaint();
    		
    		if (up) ship.velocity.setLength(ship.velocity.length()+0.5f);
    		else if (down) ship.velocity.setLength(ship.velocity.length()/1.05f);
    		if (left)
    		{
    			ship.sprite.rotate(-5);
    			ship.velocity.rotateToDegree(ship.sprite.getAngle()-90);
    		}
    		else if (right)
    		{
    			ship.sprite.rotate(5);
    			ship.velocity.rotateToDegree(ship.sprite.getAngle()-90);
    		}
    		
    		ship.Move();
    		
    		//if( planet.collidesWith(ship) ) Log.message("crash!");

    		try 
    		{
                tm += TARGET_FPS;
                Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
            }
            catch(InterruptedException e)
            {
            	Log.warning(e.toString());
            }
    	}
    	
    	//TODO: This never happens!?!
       	Log.close();
	}
	
	/**
	 * JJ> This function puts together the bits of two integers and returns it as one long
	 * @param a 110001
	 * @param b 000111
	 * @return 000111 + 11001 = 00011111001
	 */
	long addBits(int a, int b) {
		long x = (long)a;
		long y = (long)b;
		return (x<<32) | (y);
	}
	
	/**
	 * JJ> Keeps the specified object within the game screen
	 * @param player Who are we supposed to keep within bounds?
	 */
	void keepPlayerWithinBounds( Object player ) {
		boolean nextScreen = false;
		long seed = 0;
				
		if( player.pos.x > 800 ) 
		{
			x++;
			player.pos.x = 0;
			nextScreen = true;
		}
		else if( player.pos.x < 0 ) 
		{
			x--;
			player.pos.x = 800;
			nextScreen = true;
		}
		else if( player.pos.y > 600 ) 
		{
			y++;
			player.pos.y = 0;
			nextScreen = true;
		}
		else if( player.pos.y < 0 ) 
		{
			y--;
			player.pos.y = 600;
			nextScreen = true;
		}
		
		//Did we cross into a new screen?
		if( nextScreen ) 
		{
			seed = addBits(x, y);
			Log.message( "X: " + x + ", Y: " + y + ", Seed: " +  seed );
			bg = new Background(800, 600, seed );
		}
		
	}
	
	public void paint(Graphics g){		
		bg.draw(g);
		
		//Draw input string
		g.setColor(Color.white);
		g.drawString("Test = " + input, 20, 20);
		
		//int x = MouseInfo.getPointerInfo().getLocation().x - frame.getX();
		//int y = MouseInfo.getPointerInfo().getLocation().y - frame.getY();
		
		//Draw the little ship
		g.drawImage( ship.sprite.toImage(), ship.pos.getX()-ship.sprite.getWidth()/2, ship.pos.getY()-ship.sprite.getHeight()/2, this );
		
		//DEBUG
		//g.drawImage( planet.sprite.toImage(), planet.pos.getX()-planet.sprite.getWidth()/2, planet.pos.getY()-planet.sprite.getHeight()/2, this );
	}
	
	public void keyPressed(KeyEvent key) {
		if( key.getKeyCode() == KeyEvent.VK_UP ) 		up 	  = true;
		else if( key.getKeyCode() == KeyEvent.VK_DOWN ) down  = true;
		else if( key.getKeyCode() == KeyEvent.VK_LEFT)	left  = true;
		else if( key.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
		
	}

	public void keyReleased(KeyEvent key) {
		if( key.getKeyCode() == KeyEvent.VK_UP ) 		up 	  = false;
		else if( key.getKeyCode() == KeyEvent.VK_DOWN ) down  = false;
		else if( key.getKeyCode() == KeyEvent.VK_LEFT)	left  = false;
		else if( key.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		input += arg0.getKeyChar();
		
	}

}
