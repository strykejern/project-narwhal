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

import java.awt.*;
import javax.swing.*;
//import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;


/**
 * JJ> Main game class, here the important top stuff happens
 * @author Johan Jansen og Anders Eie
 *
 */
public class Game extends JPanel implements Runnable, KeyListener
{
	private static final long serialVersionUID = 1L;
	private static final int TARGET_FPS = 1000 / 60;		//60 times per second
	private boolean running;
	private JFrame frame;
	private String input = "";
	Image2D background;
	Object ship;
	Background bg;
	
	//Player position in the universe
	ArrayList<Integer> universe = new ArrayList<Integer>();
	int x = 50, y = 50;
	
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
		background = new Image2D("data/starfield.jpg");
		bg = new Background(800, 600, 100);
	}
	
	//JJ> This is the main game loop
	public void run() {
		
    	// Remember the starting time
    	long tm = System.currentTimeMillis();
    	
    	//Initialize the logging system
    	Log.initialize();
    	
		// Set the blank cursor to the JFrame.
		frame.getContentPane().setCursor(blankCursor);
		
		ship = new Object( new Image2D("data/spaceship.png"), frame.getWidth()/2, frame.getHeight()/2 );
		ship.sprite.resize(64, 64);
		generateRandomUniverse();
		
		// da loop
    	while(running)
    	{
    		repaint();
    		keepPlayerWithinBounds(ship);
    		ship.Move();
    		
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
	
	//JJ> Generate random universe
	void generateRandomUniverse() {
		Random rand = new Random();
		for(int i = 0; i < 1000; i++)
			for(int j = 0; j < 1000; j++)
				universe.add( rand.nextInt() + i*i - j*j );
	}
	
	//JJ> Keeps the specified object within the game screen
	void keepPlayerWithinBounds( Object player ) {
		boolean logMessage = false;

		if( player.pos.x > 800 ) 
		{
			x++;
			bg = new Background(800, 600, universe.get(x*y) );
			player.pos.x = 0;
			logMessage = true;
		}
		else if( player.pos.x < 0 ) 
		{
			x--;
			bg = new Background(800, 600, universe.get(x*y) );
			player.pos.x = 800;
			logMessage = true;
		}
		else if( player.pos.y > 600 ) 
		{
			y++;
			bg = new Background(800, 600, universe.get(x*y) );
			player.pos.y = 0;
			logMessage = true;
		}
		else if( player.pos.y < 0 ) 
		{
			y--;
			bg = new Background(800, 600, universe.get(x*y) );
			player.pos.y = 600;
			logMessage = true;
		}
		if( logMessage ) Log.message( "X: " + x + ", Y: " + y + ", Seed: " +  universe.get(x*y) );
	}
	
	public void paint(Graphics g){		
		//draw the backdrop
		//background.resize(frame.getWidth(), frame.getHeight() );
		//g.drawImage( background.toImage(), 0, 0, this );
		
		bg.draw(g);
		
		//Draw input string
		g.setColor(Color.white);
		g.drawString("Test = " + input, 20, 20);
		
		//int x = MouseInfo.getPointerInfo().getLocation().x - frame.getX();
		//int y = MouseInfo.getPointerInfo().getLocation().y - frame.getY();
		
		//Draw the little ship
		g.drawImage( ship.sprite.toImage(), (int)(ship.pos.x-ship.sprite.getWidth()/2), (int)(ship.pos.y-ship.sprite.getHeight()/2), ship.sprite.getWidth(), ship.sprite.getHeight(), this );
	}
	
	public void keyPressed(KeyEvent key) {
		if( key.getKeyCode() == KeyEvent.VK_UP ) ship.velocity.setLength(ship.velocity.length()*1.2f);
		else if( key.getKeyCode() == KeyEvent.VK_DOWN ) ship.velocity.setLength(ship.velocity.length()/1.5f);
		else if( key.getKeyCode() == KeyEvent.VK_LEFT)
		{
			ship.sprite.rotate(-5);
			ship.velocity.rotateToDegree(ship.sprite.getAngle()-90);
		}
		else if( key.getKeyCode() == KeyEvent.VK_RIGHT) 
		{
			ship.sprite.rotate(5);
			ship.velocity.rotateToDegree(ship.sprite.getAngle()-90);
			
		}
	}

	public void keyReleased(KeyEvent arg0) {
		
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		input += arg0.getKeyChar();
		
	}

}
