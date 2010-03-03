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
import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;


public class Game extends JPanel implements Runnable, KeyListener
{
	private static final long serialVersionUID = 1L;
	private static final int TARGET_FPS = 1000 / 60;
	private boolean running;
	private JFrame frame;
	private String input = "";
	Image2D background;
	SpaceShip ship;
	
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
	}
	
	//JJ> This is the main game loop
	public void run() {
		
    	// Remember the starting time
    	long tm = System.currentTimeMillis();
    	
    	//Initialize the logging system
    	Log.initialize();
    	
		// Set the blank cursor to the JFrame.
		frame.getContentPane().setCursor(blankCursor);
		
		ship = new SpaceShip();
		
		// da loop
    	while(running){
    		repaint();
    		ship.Update();
    		try {
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
	
	public void paint(Graphics g){		
	
		//draw backdrop
		g.drawImage( background.toImage(), 0, 0, this);
		
		//Draw input string
		g.setColor(Color.white);
		g.drawString("Test = " + input, 20, 20);
		
		//int x = MouseInfo.getPointerInfo().getLocation().x - frame.getX();
		//int y = MouseInfo.getPointerInfo().getLocation().y - frame.getY();
		
		//Draw the little ship
		g.drawImage( ship.sprite.toImage(), (int)(ship.pos.x-ship.sprite.getWidth()/2), (int)(ship.pos.y-ship.sprite.getHeight()/2), ship.sprite.getWidth(), ship.sprite.getHeight(), this );		
	}
	
	public void keyPressed(KeyEvent arg0) {
		Vector acc = new Vector(1,1);
		Log.message(ship.speed.length() + " " + ship.speed.x + " " + ship.speed.y + " " + ship.sprite.getAngle());
		if( arg0.getKeyCode() == KeyEvent.VK_UP ) ship.speed.setLength(ship.speed.length()*1.2f);
		if( arg0.getKeyCode() == KeyEvent.VK_DOWN ) ship.speed.setLength(ship.speed.length()/1.5f);
		if( arg0.getKeyCode() == KeyEvent.VK_LEFT)
		{
			ship.sprite.rotate(-5);
			ship.speed.rotateToDegree(ship.sprite.getAngle()-90);
		}
		if( arg0.getKeyCode() == KeyEvent.VK_RIGHT) 
		{
			ship.sprite.rotate(5);
			ship.speed.rotateToDegree(ship.sprite.getAngle()-90);
			
		}
	}

	public void keyReleased(KeyEvent arg0) {
		
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		input += arg0.getKeyChar();
		
	}

}
