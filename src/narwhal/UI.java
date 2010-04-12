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

import gameEngine.Vector;
import gameEngine.Video;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

//Skeleton class for the UI
public class UI {
	private Spaceship hud;
	private ArrayList<Spaceship> tracking;
	
	public UI(Spaceship observe){
		hud = observe;
		tracking = new ArrayList<Spaceship>();
	}
	
	public void draw(Graphics2D g) {
		
		//Calculate positions
		int width = Video.getScreenWidth()/20;
		int x = Video.getScreenWidth() - 10;
		int y = Video.getScreenHeight() - 30;
		
		//Status bars
		drawOneBar(g, new Vector(x, y), (int)hud.life, hud.lifeMax, new Color(153, 0 , 0, 200 ));
		x -= width +5;

		drawOneBar(g, new Vector(x, y), (int)hud.energy, hud.energyMax, new Color(255, 153 , 0, 200 ));
		x -= width +5;

		if(hud.shieldMax != 0)
		{
			drawOneBar(g, new Vector(x, y), (int)hud.shield, hud.shieldMax, new Color(0, 0 , 153, 200 ));
			x -= width +5;
		}
		
		for (Spaceship target : tracking)
		{
			Vector screenMid = new Vector(Video.getScreenWidth()/2, Video.getScreenHeight()/2);
			Vector diff = target.getPosCentre().minus(hud.getPosCentre());
			if (diff.x > -screenMid.x && diff.x < screenMid.x && diff.y > -screenMid.y && diff.y < screenMid.y) continue;
			int dist = (int)diff.length();
			diff.setLength(150);
			
			Vector tip = diff.clone();
			Vector botLeft = tip.clone();
			botLeft.setLength(125);
			Vector botRight = botLeft.clone();
			botLeft.rotateBy((float)-(Math.PI/32.0));
			botRight.rotateBy((float)(Math.PI/32.0));
			tip = screenMid.plus(tip);
			botLeft = screenMid.plus(botLeft);
			botRight = screenMid.plus(botRight);
			
			g.setColor(Color.green);
			//g.drawLine(screenMid.getX(), screenMid.getY(), diff.getX(), diff.getY());
			int[] xPoints = new int[]{ tip.getX(), botLeft.getX(), botRight.getX() };
			int[] yPoints = new int[]{ tip.getY(), botLeft.getY(), botRight.getY() };
			g.fillPolygon(xPoints, yPoints, 3);
			
			Vector rightMost;
			if (tip.x > botLeft.x)
			{
				if (tip.x > botRight.x)  rightMost = tip;
				else rightMost = botRight;
			}
			else
			{
				if (botLeft.x > botRight.x) rightMost = botLeft;
				else rightMost = botRight;
			}
			
			g.drawString(""+dist, rightMost.getX()+20, rightMost.getY());
		}
	}
	
	private void drawOneBar(Graphics2D g, Vector pos, int current, int max, Color clr) {
		int width = Video.getScreenWidth()/20;
		int height = Video.getScreenHeight()/4;

		//Draw full bar
		g.setColor(new Color(102, 102, 102, 150));
		g.fillRoundRect(pos.getX()-width, pos.getY()-height, width, height, 25, 25);
		
		//Draw remaining bar
		g.setColor( clr );
		height = Math.max( 0, (int)((height/(float)max) * current) );
		g.fillRoundRect(pos.getX()-width, pos.getY()-height, width, height, 25, 25);
	}
	
	public void addTracking(Spaceship target){
		tracking.add(target);
	}
}
