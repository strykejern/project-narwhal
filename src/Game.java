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


public class Game extends JPanel implements Runnable 
{
	private static final long serialVersionUID = 1L;
	private static final int FPS = 1000 / 60;
	boolean running = true;
	
	public static void main(String[] args)
	{
		JFrame parentWindow = new JFrame("The game");
    	
    	parentWindow.getContentPane().add(new Game());
    	
    	parentWindow.setSize(800 , 600);
        parentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentWindow.setVisible(true);
	}
	
	public Game()
	{
		new Thread(this).start();
	}
	
	public void run() 
	{
    	// Remember the starting time
    	long tm = System.currentTimeMillis();
    	Log.initialize();
    	while(running){
    		repaint();
    		
    		try {
                tm += FPS;
                Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
            }
            catch(InterruptedException e)
            {
            	System.out.println(e);
            }
    	}
    	Log.close();
		
	}
	
	public void paint(Graphics g)
	{
		g.setColor(Color.black);

		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setColor(Color.cyan);
		int x = MouseInfo.getPointerInfo().getLocation().x;
		int y = MouseInfo.getPointerInfo().getLocation().y;
		g.fillOval(x, y, 100, 100);
	}
}
