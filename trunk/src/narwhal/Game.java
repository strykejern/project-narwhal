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

import gameEngine.GameObject;
import gameEngine.Image2D;
import gameEngine.Keyboard;
import gameEngine.Log;
import gameEngine.Planet;
import gameEngine.Profiler;
import gameEngine.Sound;
import gameEngine.Spaceship;
import gameEngine.Vector;

import java.awt.*;
import javax.swing.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


/**
 * JJ> Main game class, here the important top stuff happens
 * @author Johan Jansen and Anders Eie
 *
 */
public class Game extends JPanel implements Runnable, KeyListener
{
	private static int SCREEN_X = 800, SCREEN_Y = 600;
	private static final long serialVersionUID = 1L;
	private static final int TARGET_FPS = 1000 / 60;		//60 times per second
	private boolean running;
	private JFrame frame;
	GameObject ship, planet;
	Background bg;	
	private boolean up, down, left, right;
	private Keyboard keys;
	private HashMap<Long, Planet> planets;
	private ArrayList<Image2D> planetList;
	
	//Player position in the universe
	Random rand = new Random();
	int x = rand.nextInt(100), y = rand.nextInt(100);
	
	// Create a new blank cursor.
	final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
			new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

	
	public static void main(String[] args) throws InterruptedException{	
    	//Initialize the logging system, do this first so that error logging happens correctly.
    	Log.initialize();

    	JFrame parentWindow = new JFrame("Project Narwhal");		
    	parentWindow.getContentPane().add(new Game(parentWindow));

    	parentWindow.setSize(SCREEN_X , SCREEN_Y);
        parentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentWindow.setVisible(true);
   	}
	
	public Game(JFrame frame){
		bg = new Background(SCREEN_X, SCREEN_Y, addBits(x, y));
    	this.frame = frame;
		Image2D icon = new Image2D("data/icon.png");
		frame.setIconImage( icon.toImage() );
		new Thread(this).start();
		running = true;
		frame.addKeyListener(this);
		keys = new Keyboard();
		planets = new HashMap<Long, Planet>();
		//Initialize the player ship		
		ship = new Spaceship(new Vector(SCREEN_X/2, SCREEN_Y/2+100), new Image2D("data/spaceship.png"), keys);
	}
	
	static public int getScreenWidth()	{
		return SCREEN_X;
	}
	static public int getScreenHeight()	{
		return SCREEN_Y;
	}
	
	//JJ> This is the main game loop
	public void run() {
		
		// Remember the starting time
    	long tm = System.currentTimeMillis();
    	
		// Set the blank cursor to the JFrame.
		frame.getContentPane().setCursor(blankCursor);

		//Generate the first background
		generateNewScreen();
		

		//Play some music
    	Sound music = new Sound("data/space.ogg");
    	//music.play();

    	//Load crash sound
    	Sound crash = new Sound("data/crash.au");

		// da loop
    	while(running)
    	{
    		//Keep the player from moving outside the screen
    		//keepPlayerWithinBounds(ship);

    		//Basic collision loop (put all detection here
    		if(planet != null)
    		//if( planet.isCollidable() && ship.isCollidable() ) 
    			if( planet.collidesWith( ship ) )
    		    	crash.play();
    		
    		//Calculate ship movement
    		//calculateShipMovement();
    		ship.update();
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

	
	/*private void calculateShipMovement()
	{
		if (up && ship.speed.length() < 15f) ship.speed.setLength(ship.speed.length()+0.5f);
		else if (down) ship.speed.setLength(ship.speed.length()/1.05f);

		if (left)
		{
			ship.rotate(-5);
			ship.speed.rotateToDegree(ship.getAngle());
		}
		else if (right)
		{
			ship.rotate(5);
			ship.speed.rotateToDegree(ship.getAngle());
		}
	}*/
	
	
	/**
	 * JJ> This function puts together the bits of two integers and returns it as one long
	 * @param a 110001
	 * @param b 000111
	 * @return 000111 + 11001 = 00011111001
	 */
	long addBits(int a, int b) {
/*		long x = (long)a;
		long y = (long)b;
		return (x<<32) | (y);*/
		return a*a - b*(b-1);
	}
	
	/**
	 * JJ> Keeps the specified object within the game screen
	 * @param player Who are we supposed to keep within bounds?
	 */
	void keepPlayerWithinBounds( Object player ) {
		boolean nextScreen = false;
				
		if( player.pos.x > SCREEN_X ) 
		{
			x++;
			player.pos.x = 0;
			nextScreen = true;
		}
		else if( player.pos.x < 0 ) 
		{
			x--;
			player.pos.x = SCREEN_X;
			nextScreen = true;
		}
		else if( player.pos.y > SCREEN_Y ) 
		{
			y++;
			player.pos.y = 0;
			nextScreen = true;
		}
		else if( player.pos.y < 0 ) 
		{
			y--;
			player.pos.y = SCREEN_Y;
			nextScreen = true;
		}
		
		//Did we cross into a new screen?
		if( nextScreen ) generateNewScreen();
	}
	
	private void generateNewScreen()
	{
		long seed = addBits(x, y);
		if (planets.keySet().contains(seed))
			planet = planets.get(seed);
		else
		{
			Random rand = new Random(seed);
			bg.generate(seed);
			
			planet = null;
			if(rand.nextInt(100) <= 25)
			{
				planets.put(seed, new Planet(new Vector(), planetList, seed));
				planet = planets.get(seed);
			}
		}
	}
	
	private void loadPlanets(){
		File[] fileList = new File("data/planets").listFiles();
		
		//Load planets into memory
		planetList = new ArrayList<Image2D>();
		for( File f : fileList )
		{
			if( !f.isFile() ) continue;
			planetList.add( new Image2D( f.toString()) ) ;
			
		}
	}

	
	/*
	 * JJ> Paints every object of interest (not background)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g){		
		bg.draw(g);

		//Draw the planet
		if(planet != null)
		//	g.drawImage( planet.getImage(), planet.pos.getX()-planet.getWidth()/2, planet.pos.getY()-planet.getHeight()/2, this );		
			planet.drawCollision(g);
			
		//Draw the little ship
		//g.drawImage( ship.getImage(), ship.pos.getX()-ship.getWidth()/2, ship.pos.getY()-ship.getHeight()/2, this );
		ship.drawCollision(g);
		
		ship.drawCollision(g);	
		if(planet != null)	
		planet.drawCollision(g);
	}
	
	public void keyPressed(KeyEvent key) {
		keys.update(key, true);
	}

	public void keyReleased(KeyEvent key) {
		keys.update(key, false);
	}

	public void keyTyped(KeyEvent arg0) {
		
	}

}
