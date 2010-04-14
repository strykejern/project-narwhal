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
package gameEngine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class Input {
	public boolean up, down, left, right, escape;
	public boolean mosButton1, mosButton2, mosButton3;
	public Vector mousePos;
	private Vector cameraPos;
	
	public Input(){
		up = false;
		down = false;
		left = false;
		right = false;
		mousePos = new Vector();
		cameraPos = new Vector();
	}
	
	public void update(KeyEvent key, boolean pressed){
		int code = key.getKeyCode();
		if 		(code == KeyEvent.VK_UP || code == KeyEvent.VK_W) up = pressed;
		else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) down = pressed;
		else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) left = pressed;
		else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) right = pressed;
		else if (code == KeyEvent.VK_ESCAPE) escape = pressed;
	}

	public void update(MouseEvent key, boolean pressed){
		int code = key.getButton();
		if(code == MouseEvent.BUTTON1) mosButton1 = pressed;
		if(code == MouseEvent.BUTTON3) mosButton2 = pressed;
		//if(key.getButton() == MouseEvent.BUTTON2) mosButton3 = pressed;		//mouse wheel pressed
	}

	public void update(Point mouse){
		if( mouse == null ) return;
		mousePos.x = mouse.x;
		mousePos.y = mouse.y-15;
	}
	
	public void drawCrosshair(Graphics g){
		g.setColor(Color.green);
		g.drawOval(mousePos.getX()-5, mousePos.getY()-5, 10, 10);
	}
	
	public Vector mouseUniversePos(){
		return cameraPos.plus(mousePos);
	}
	
	public void setCameraPos(Vector cameraPos){
		this.cameraPos = cameraPos;
	}
}
