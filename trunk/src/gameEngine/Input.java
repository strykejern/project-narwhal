package gameEngine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class Input {
	public boolean up, down, left, right;
	public Vector mousePos;
	
	public Input(){
		up = false;
		down = false;
		left = false;
		right = false;
		mousePos = new Vector();
	}
	
	public void update(KeyEvent key, boolean pressed){
		if 		(key.getKeyCode() == KeyEvent.VK_UP) up = pressed;
		else if (key.getKeyCode() == KeyEvent.VK_DOWN) down = pressed;
		else if (key.getKeyCode() == KeyEvent.VK_LEFT) left = pressed;
		else if (key.getKeyCode() == KeyEvent.VK_RIGHT) right = pressed;
	}
	
	public void update(int x, int y){
		mousePos.x = x-5;
		mousePos.y = y-25;
	}
	
	public void drawCrosshair(Graphics g){
		g.setColor(Color.green);
		g.drawOval(mousePos.getX()-5, mousePos.getY()-5, 10, 10);
	}
}
