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

import gameEngine.Input;
import gameEngine.Sound;
import gameEngine.Vector;
import gameEngine.Video;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import narwhal.GameFont.FontType;

public class Button {
	static private Sound buttonHover, buttonClick;
	
	private Vector pos, size, movePos;
	private String text;
	private boolean hidden;
	private boolean mouseOver;
	private float alpha;
	private int id;
	
	public Button(Vector pos, Vector size, String text, int id, Vector startPos) {
		
    	//Load static button sounds if they haven't been loaded yet
    	if( buttonHover == null )
    		buttonHover = new Sound("hover.au");
    	if( buttonClick == null )
        	buttonClick = new Sound("click.wav");
		
		alpha = 0;
		hidden = false;
		mouseOver = false;
		movePos = startPos.clone();
		this.pos = pos.minus( size.dividedBy(2) );
		this.size = size;
		this.text = text.toUpperCase();
		this.id = id;
	}
	
	public int getID(){
		return id;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public boolean mouseOver(Input key) {
		if( hidden )
		{
			mouseOver = false;
			return false;
		}
       
		//Are they holding the mouse over this object?
        if( key.mousePos.x > pos.x && key.mousePos.x < pos.x + size.x )
        	if( key.mousePos.y > pos.y && key.mousePos.y < pos.y + size.y )
        	{
        		if( !mouseOver ) buttonHover.playFull(0.1f);
        		mouseOver = true;
        		return true;
        	}
        
        mouseOver = false;
        return false;
	}
	
	public void playClick(){
		buttonClick.playFull(1);
	}
	
	public void hide(){
		if( hidden ) return;
		hidden = true;
		movePos.x =  Video.getScreenWidth()/2-size.x/2;
		movePos.y = Video.getScreenHeight()/2-size.y/2;
	}
	
	public void show(){
		hidden = false;
	}
	
	public void update() {
		if( hidden ) return;
		
		//Make it move if needed
		if( movePos.x < pos.x ) movePos.x += 4;
		if( movePos.y < pos.y ) movePos.y += 4;
		if( movePos.x > pos.x ) movePos.x -= 4;
		if( movePos.y > pos.y ) movePos.y -= 4;
	}
	
	public Rectangle getButtonArea() {
		return new Rectangle(pos.getX(), pos.getY(), size.getX(), size.getY());
	}
	
	public void draw(Graphics2D g){
		if( hidden ) return;
		
		//Calculate fade away (not implemented)
		float trans = Math.min(1, Math.max(0, 0.5f-alpha));
		float solid = Math.min(1, Math.max(0, 1.0f-alpha));
		
		//Button borders
		g.setColor(new Color(0.0015f, 0.8f, 0.0015f, trans) );
		g.fillRoundRect(movePos.getX(), movePos.getY(), size.getX(), size.getY(), 25, 25);

		//Button
		Vector v = new Vector(size.times(0.95f).getX(), size.times(0.8f).getY());
		if( mouseOver ) g.setColor(new Color(0.1f, 0.9f, 0.1f, solid) );
		else		    g.setColor(new Color(0.1f, 0.9f, 0.1f, trans) );
		g.fillRoundRect(movePos.getX()+size.minus(v).getX()/2, movePos.getY()+size.minus(v).getY()/2, v.getX(), v.getY(), 25, 25);
		
		//Text	
		GameFont.set(g, FontType.FONT_MENU, Color.BLACK, 15);
		if( mouseOver ) g.setColor(new Color(0, 0, 0) );
		g.drawString(text, movePos.getX()+ (size.getX()/2) - (GameFont.getWidth(text, g)/2), movePos.getY()+ size.getY()/1.75f);
	}
	
}