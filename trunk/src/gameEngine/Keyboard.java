package gameEngine;

import java.awt.event.KeyEvent;

public class Keyboard {
	public boolean up, down, left, right;
	
	public Keyboard(){
		up = false;
		down = false;
		left = false;
		right = false;
	}
	
	public void update(KeyEvent key, boolean pressed){
		if 		(key.getKeyCode() == KeyEvent.VK_UP) up = pressed;
		else if (key.getKeyCode() == KeyEvent.VK_DOWN) down = pressed;
		else if (key.getKeyCode() == KeyEvent.VK_LEFT) left = pressed;
		else if (key.getKeyCode() == KeyEvent.VK_RIGHT) right = pressed;
	}
}
