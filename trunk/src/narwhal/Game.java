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
import gameEngine.Particle;
import gameEngine.Planet;
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
import java.util.Hashtable;
import java.util.Random;


/**
 * JJ> Main game class, here the important top stuff happens
 * @author Johan Jansen and Anders Eie
 *
 */
public class Game extends JPanel implements Runnable, KeyListener
{
	static Dimension resolution;
	private static final long serialVersionUID = 1L;
	private static final int TARGET_FPS = 1000 / 60;		//60 times per second
	private boolean running;
	private JFrame frame;
	GameObject ship, currentPlanet;
	Background bg;
	private Keyboard keys;
	private Hashtable<Long, Planet> planetList;
	private ArrayList<Image2D> planetImages;


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

		resolution.setSize(800, 600);
		parentWindow.setSize(resolution);
        parentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentWindow.setVisible(true);
   	}
	
	public Game(JFrame frame) {
    	
		//The actual frame
		this.frame = frame;
		Image2D icon = new Image2D("data/icon.png");
		frame.setIconImage( icon.toImage() );
				
		//Input controls
		frame.addKeyListener(this);
		keys = new Keyboard();
		
		//Background
		bg = new Background(resolution.width, resolution.height);
		bg.generateWorld( 4, generateSeed(x, y) );

		//Initialize the player ship		
		ship = new Spaceship(new Vector(), new Image2D("data/spaceship.png"), keys);
		
		//Thread
		new Thread(this).start();
		running = true;
	}
	
	static public int getScreenWidth()	{
		return resolution.width;
	}
	static public int getScreenHeight()	{
		return resolution.height;
	}
	
	//JJ> This is the main game loop
	public void run() {
		
		// Remember the starting time
    	long tm = System.currentTimeMillis();
    	
		// Set the blank cursor to the JFrame.
		frame.getContentPane().setCursor(blankCursor);
		    	
    	//Load game resources
		Sound music = new Sound("data/space.ogg");
    	Sound crash = new Sound("data/crash.au");
		planetList = new Hashtable<Long, Planet>(0, 0.75f);
    	loadPlanets();
    	music.play();

		//Generate the first background
    	generateNewScreen();
    	
		while(running)
    	{
    		//Basic collision loop (put all detection here
    		if(currentPlanet != null)
    			if( currentPlanet.collidesWith( ship ) )
    			{
    		    	crash.play();
    				currentPlanet.collision(ship);
    			}
    		
    		//Calculate ship movement
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

		
	/**
	 * JJ> This function generates a random unique number from two variables
	 */
	long generateSeed(int a, int b) {
		return a*a - b*(b-1);
	}
	
	private void generateNewScreen() {
		long seed = generateSeed(x, y);
		if (planetList.containsKey(seed))
		{
			currentPlanet = planetList.get(seed);
		}
		else
		{
			Random rand = new Random(seed);
			currentPlanet = null;
			if(rand.nextInt(100) <= 125)
			{
				planetList.put(seed, new Planet(new Vector(resolution.width/2, resolution.height/2), planetImages, seed));
				currentPlanet = planetList.get(seed);
			}
		}
	}
	
	private void loadPlanets() {
		File[] fileList = new File("data/planets").listFiles();
		
		//Load planets into memory
		planetImages = new ArrayList<Image2D>();
		for( File f : fileList )
		{
			if( !f.isFile() ) continue;
			planetImages.add( new Image2D( f.toString()) ) ;
		}
	}

	
	/*
	 * JJ> Paints every object of interest (not background)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {		
		bg.draw(g);
		Particle.drawAllParticles(g);

		//Draw the planet
		if(currentPlanet != null)
		{
			currentPlanet.draw(g);
			currentPlanet.drawCollision(g);
		}
		
		//Draw the little ship
		ship.draw(g);
		ship.drawCollision(g);
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
