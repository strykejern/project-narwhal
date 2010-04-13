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
public class HUD {

	//HUD colors
	private final static Color SHIELD 		= new Color(0, 0 , 153 );
	private final static Color LIFE   		= new Color(153, 0 , 0 );
	private final static Color BACKGROUND   = new Color(10, 10 , 10 );
	private final static Color ENERGY   	= new Color(150, 150 , 0 );

	//HUD data
	private Spaceship observer;
	private ArrayList<Spaceship> tracking;
	
	
	/**
	 * JJ> Constructs a new HUD object which is the overlay that shows the player how much
	 * life, shield and energy he has left. Also does radar and displays weapon.
	 * @param observer This should be the player's ship or whatever other ship you want to follow
	 */
	public HUD( Spaceship observer ){
		this.observer = observer;
		tracking = new ArrayList<Spaceship>();
	}
	
	public void draw(Graphics2D g) {
		
		//Don't draw HUD for players who lost
		if( !observer.active() ) return;
		
		//Calculate positions		
		Vector hudPos = Video.getResolutionVector().minus(new Vector(200, 200));
				
		//Shield background
		g.setColor(BACKGROUND);
		g.fillArc(hudPos.getX(), hudPos.getY(), 400, 400, 180, -90);

		//Shield
		if(observer.shieldMax != 0)
		{
			g.setColor(SHIELD);
			g.fillArc(hudPos.getX()+10, hudPos.getY()+10, 380, 380, 180, -(int) (90f*(observer.shield/observer.shieldMax)));
		}

		//Life background
		g.setColor(BACKGROUND);
		g.fillArc(hudPos.getX()+50, hudPos.getY()+50, 300, 300, 180, -90);
		
		//Life
		g.setColor(LIFE);
		g.fillArc(hudPos.getX()+60, hudPos.getY()+60, 280, 280, 180, -(int) (90f*(observer.life/observer.lifeMax)));
		
		g.setColor(BACKGROUND);
		g.fillArc(hudPos.getX()+100, hudPos.getY()+100, 200, 200, 180, -90);
		
		//Draw separation lines in the bars
		for (int i = 1; i < 22; ++i)
			g.drawLine(Video.getScreenWidth(), Video.getScreenHeight(), Video.getScreenWidth()-(int)(Math.cos((Math.PI/44)*i)*200.0), Video.getScreenHeight()-(int)(Math.sin((Math.PI/44)*i)*200.0));
		
		hudPos.sub(new Vector(Video.getScreenWidth(), 0));
		
		//Energy background
		g.setColor(BACKGROUND);
		g.fillArc(hudPos.getX(), hudPos.getY(), 400, 400, 0, 90);
		
		//Energy
		g.setColor(ENERGY);
		g.fillArc(hudPos.getX()+10, hudPos.getY()+10, 380, 380, 0, (int) (90f*(observer.energy/observer.energyMax)));
		
		g.setColor(BACKGROUND);
		g.fillArc(hudPos.getX()+50, hudPos.getY()+50, 300, 300, 0, 90);
		
		//Draw separation lines in the bars
		for (int i = 1; i < 22; ++i)
			g.drawLine(0, Video.getScreenHeight(), (int)(Math.cos((Math.PI/44)*i)*200.0), Video.getScreenHeight()-(int)(Math.sin((Math.PI/44)*i)*200.0));
		
		//Draw ships that are tracked
		for( int i = 0; i < tracking.size(); i++ )
		{
			Spaceship target = tracking.get(i);
			
			//Remove destroyed ships from the list
			if( !target.active() )
			{
				tracking.remove(i--);
				continue;
			}
			
			//Track it
			drawRadar(target, g);
		}
	}
		
	public void addTracking(Spaceship target){
		tracking.add(target);
	}
	
	//JJ> Draws one tracking polygon for the specified spaceship
	private void drawRadar(Spaceship target, Graphics2D g) {
		Vector screenMid = new Vector(Video.getScreenWidth()/2, Video.getScreenHeight()/2);
		Vector diff = target.getPosCentre().minus(observer.getPosCentre());
		
		//No need to draw if we can see them
		if (diff.x > -screenMid.x && diff.x < screenMid.x && diff.y > -screenMid.y && diff.y < screenMid.y) return;
		
		//Calculate arrow position
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
		
		//Allies get green arrow
		if(observer.team.equals(target.team)) g.setColor( new Color(0, 255, 0, 75) );
		else						     g.setColor( new Color(255, 0, 0, 75) );
		
		
		//Draw the arrow pointer
		int[] xPoints = new int[]{ tip.getX(), botLeft.getX(), botRight.getX() };
		int[] yPoints = new int[]{ tip.getY(), botLeft.getY(), botRight.getY() };
		g.fillPolygon(xPoints, yPoints, 3);
		
		
		//Draw distance to target
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
		
		g.drawString(Integer.toString(dist), rightMost.getX()+20, rightMost.getY());
	}
	
	//Depecrated old status bars, keep here for reference sake
	@SuppressWarnings("unused")
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

}
