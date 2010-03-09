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
import gameEngine.Input;
import gameEngine.Log;
import gameEngine.Particle;
import gameEngine.Sound;
import gameEngine.Vector;

import java.awt.*;
import javax.swing.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;


/**
 * JJ> Main game class, here the important top stuff happens
 * @author Johan Jansen and Anders Eie
 *
 */
public class Game extends JPanel implements Runnable, KeyListener {
	static Dimension resolution = new Dimension(800, 600);
	private static final long serialVersionUID = 1L;
	private static final int TARGET_FPS = 1000 / 60;		//60 times per second
	private boolean running;
	private JFrame frame;
	GameObject ship;
	Background bg;
	private Input keys;
	private ArrayList<Planet> planetList;
	private ArrayList<Image2D> planetImages;

	//Player position in the universe
	Random rand = new Random();
	Vector playerPosition = new Vector();
	
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
		keys = new Input();
		
		//Background
		bg = new Background(resolution.width, resolution.height);
		bg.generateWorld( 4, System.currentTimeMillis() );

		//Initialize the player ship		
		ship = new Spaceship(new Vector(resolution.width/2, resolution.height/2), new Image2D("data/spaceship.png"), keys);
		
		//Thread (do last so that everything above is properly loaded before the main loop begins)
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
    	music.play();

		//Generate the planets
    	planetList = generateRandomPlanets( System.currentTimeMillis() );
    	
		while(running)
    	{
			int x = MouseInfo.getPointerInfo().getLocation().x - frame.getX();
            int y = MouseInfo.getPointerInfo().getLocation().y - frame.getY();
            keys.update(x, y);
			
    		//Basic collision loop (put all detection here)
            for( Planet currentPlanet : planetList )
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
	
	private ArrayList<Planet> generateRandomPlanets(long seed) {
		Random rand = new Random(seed);
		ArrayList<Planet> planetList = new ArrayList<Planet>();

		loadPlanets();
    	
		//Randomly generate for every screen
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
			if(rand.nextInt(100) <= 25)
			{
				planetList.add(new Planet(new Vector(resolution.width/2*i, resolution.height/2*j), planetImages, seed));
			}
		
		//All done!
		planetImages.clear();
    	return planetList;
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
		bg.draw(g, ship.getPosition());
		bg.drawBounds(g, ship.getPosition() );
		Particle.drawAllParticles(g);

		//Draw every planet
		if(planetList != null)
			for( Planet currentPlanet : planetList )
			{
				currentPlanet.draw(g);
				currentPlanet.drawCollision(g);
			}
		
		//Draw the little ship
		ship.draw(g);
		ship.drawCollision(g);
		keys.drawCrosshair(g);
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
