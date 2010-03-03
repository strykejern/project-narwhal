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
	
	public static void main(String[] args){
		JFrame parentWindow = new JFrame("Project Narwhal");		
    	parentWindow.getContentPane().add(new Game(parentWindow));
    	
    	parentWindow.setSize(800 , 600);
        parentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentWindow.setVisible(true);
 	}
		
	public Game(JFrame frame){
		this.frame = frame;
		frame.setIconImage( loadImage("data/icon.png") );
		new Thread(this).start();
		running = true;
		frame.addKeyListener(this);
	}
	
	public void run() {
    	// Remember the starting time
    	long tm = System.currentTimeMillis();
    	Log.initialize();
    	
    	while(running){
    		repaint();
    		
    		try {
                tm += TARGET_FPS;
                Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
            }
            catch(InterruptedException e)
            {
            	Log.println(e);
            }
    	}
       	Log.close();
	}
	
	public void paint(Graphics g){
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.drawImage(loadImage("data/starfield.jpg"), 0, 0, getWidth(), getHeight(), Color.black, null);

		g.setColor(Color.cyan);
		int x = MouseInfo.getPointerInfo().getLocation().x - frame.getX();
		int y = MouseInfo.getPointerInfo().getLocation().y - frame.getY();
		
		// Transparent 16 x 16 pixel cursor image.
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
		    cursorImg, new Point(0, 0), "blank cursor");

		// Set the blank cursor to the JFrame.
		frame.getContentPane().setCursor(blankCursor);
		
		g.setColor(Color.white);
		g.drawString("Test = " + input, 20, 20);
		
		//g.fillOval(x, y, 100, 100);
		g.drawImage( loadImage("data/spaceship.png"), x, y, 64, 64, Color.black, null );
	}
	
	private Image loadImage( String fileName )	{
		return Toolkit.getDefaultToolkit().getImage( fileName );
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		input += arg0.getKeyChar();
		Log.println(arg0.getKeyChar()+"1");
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		Log.println(arg0.getKeyChar()+"2");
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		input += arg0.getKeyChar();
		Log.println(arg0.getKeyChar()+"3");
	}

}
