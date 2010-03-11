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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


/**
 * JJ> Main game class, here the important top stuff happens
 * @author Johan Jansen and Anders Eie
 *
 */
public class Game extends JPanel implements Runnable, KeyListener, MouseListener {
	private static Dimension resolution = new Dimension();
	private static final long serialVersionUID = 1L;
	private static final int TARGET_FPS = 1000 / 60;		//60 times per second
	private boolean running;
	private JFrame frame;
	private GameObject ship;
	private Universe currentWorld;
	private Input keys;
	private ArrayList<Particle> particleList;
	
	static //Hardware graphic stuff
	GraphicsEnvironment graphEnv;
	static GraphicsDevice graphDevice;
	static private GraphicsConfiguration graphicConf;
	
	public static GraphicsConfiguration getGraphicsConf() {
		return graphicConf;		
	}
	
	// Create a new blank cursor.
	final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
			new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

	
	public static void main(String[] args) throws InterruptedException{	
    	//Initialize the logging system, do this first so that error logging happens correctly.
    	Log.initialize();
		resolution.setSize(800, 640);
		//resolution = Toolkit.getDefaultToolkit().getScreenSize();			//Fullscreen

		//Acquiring the current Graphics Device and Graphics Configuration
		//This ensures us proper hardware acceleration
		graphEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		graphDevice = graphEnv.getDefaultScreenDevice();
		graphicConf = graphDevice.getDefaultConfiguration();

		//Initialize the frame window where we draw stuff
    	JFrame parentWindow = new JFrame("Project Narwhal", graphicConf);		
    	parentWindow.getContentPane().add(new Game(parentWindow));
    	parentWindow.setSize(resolution);
		parentWindow.setResizable(false);
		//parentWindow.setUndecorated(true);								//Remove borders
        parentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentWindow.setVisible( true );
        
        //This ensures there is no flickering
       	parentWindow.setIgnoreRepaint( true );
  	}
	
	public Game(JFrame frame) {
    	
		//The actual frame
		this.frame = frame;
		Image2D icon = new Image2D("data/icon.png");
		frame.setIconImage( icon.toImage() );

		//Input controls
		frame.addKeyListener(this);
		frame.addMouseListener(this);
		keys = new Input();
		
		//Prepare graphics
		currentWorld = new Universe( resolution, 4, System.currentTimeMillis() );
		particleList = new ArrayList<Particle>();
       	
		//Load resources
       	Particle.loadParticles();
       	
		//Initialize the player ship		
		ship = new Spaceship(new Vector(1, 1), new Image2D("data/spaceship.png"), keys);
		
		//Thread (do last so that everything above is properly loaded before the main loop begins)
		running = true;
		new Thread(this).start();
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

 	
		while(running)
    	{
			int x = MouseInfo.getPointerInfo().getLocation().x - frame.getX();
            int y = MouseInfo.getPointerInfo().getLocation().y - frame.getY();
          
            keys.update(x, y);
			
    		//Basic collision loop (put all detection here)
            for( Planet currentPlanet : currentWorld.getPlanetList() )
    			if( currentPlanet.collidesWith( ship ) && false ) /////////////////////////////// WARNING
    			{
    		    	crash.play();
    				currentPlanet.collision(ship);
    			}
    		
    		//Calculate ship movement
    		ship.update();

    		//Update particle effects
    		for( int i = 0; i < particleList.size(); i++ )
    		{
    			particleList.get(i).update();
    			if( particleList.get(i).requestsDelete() ) particleList.remove(i--);
    		}

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
	
	public void spawnParticle( Particle prt ) {
		if( particleList.size() >= Particle.MAX_PARTICLES ) return;
		particleList.add( prt );
	}
		
	
	/*
	 * JJ> Paints every object of interest
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics rawGraphics) {
		//Convert to the Graphics2D object which allows us more functions
		Graphics2D g = (Graphics2D) rawGraphics;
		
		//Set quality mode
		if( Image2D.isHighQualityMode() )
		{
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	    	g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
			g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		   	g.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE );
		}
		else
		{
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
			g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
			g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
		   	g.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE );
		}
		
		//Draw background
		currentWorld.drawBackground( g, ship.getPosition() );
		
		//Draw all particles
		for( int i = 0; i < particleList.size(); i++ ) 
		{
			if( particleList.get(i).isOnScreen() )
				particleList.get(i).draw( g, ship.getPosition() );
		}

		//Draw every planet
		/*if(planetList != null)
			for( Planet currentPlanet : planetList )
			{
				currentPlanet.draw(g);
				currentPlanet.drawCollision(g);
			}
		*/
		
		//Draw the little ship
		ship.draw(g);
		ship.drawCollision(g);
		keys.drawCrosshair(g);
		
		g.setColor(Color.white);
		g.drawString("Ship position: X: " + ship.getPosition().x + ", Y: " + ship.getPosition().y, 5, 20);
		g.drawString("Number of particles: " + particleList.size(), 5, 40);
	}
	
	static public boolean isInScreen(Rectangle rect)
	{
		if( rect.x < -rect.width || rect.x > Game.getScreenWidth() ) return false;
		if( rect.y < -rect.height || rect.y > Game.getScreenHeight() ) return false;
		return true;
	}
	
	public void keyPressed(KeyEvent key) {
		keys.update(key, true);
	}

	public void keyReleased(KeyEvent key) {
		keys.update(key, false);
	}

	public void keyTyped(KeyEvent arg0) {	
	}

	public void mouseReleased(MouseEvent mouse) {
		keys.update(mouse, false);
	}
	
	public void mousePressed(MouseEvent mouse) {
		keys.update(mouse, true);
		spawnParticle( new Particle(ship.getPosition().clone(), "fire", 500, 1.0f, -0.005f, 1, (float)Math.toRadians(5)));
	}

	public void mouseClicked(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {	
	}


}
