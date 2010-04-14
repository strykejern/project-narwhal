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

import gameEngine.Camera;
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

	//Radar colors
	private final static Color ENEMY 		= new Color(255, 0 , 0, 75 );
	private final static Color FRIEND    	= new Color(0, 255 , 0, 75 );
	private final static Color NEUTRAL   	= new Color(0, 0 , 255, 75 );
	private final static Color RADAR_LIFE   = new Color(255, 0, 0, 128); 
	private final static Color RADAR_SHIELD = new Color(0, 0, 255, 128); 
	private final static Color RADAR_ENERGY = new Color(164, 100, 0, 128); 
	private final static Color RADAR_BACK   = new Color(102, 102, 102, 64); 

	//HUD data
	private final static Vector SCREEN_MID = new Vector(Video.getScreenWidth()/2, Video.getScreenHeight()/2);
	private Spaceship observer;
	private ArrayList<Spaceship> tracking;
	private Camera viewPort;
	
	/**
	 * JJ> Constructs a new HUD object which is the overlay that shows the player how much
	 * life, shield and energy he has left. Also does radar and displays weapon.
	 * @param observer This should be the player's ship or whatever other ship you want to follow
	 */
	public HUD( Spaceship observer){
		this.observer = observer;
		tracking = new ArrayList<Spaceship>();
	}
	
	public void setCamera(Camera viewPort) {
		this.viewPort = viewPort;		
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
		if(observer.radarLevel > 0)
		{
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
	}
		
	public void addTracking(Spaceship target){
		tracking.add(target);
	}
	
	//JJ> Draws one tracking polygon for the specified spaceship
	private void drawRadar(Spaceship target, Graphics2D g) {
		
		//No need to draw if we can see them
		if( viewPort.isInFrame(target) )
		{
			//But we might need to draw their life, shield and energy bars
			drawRadarStatus(g, target);
			return;
		}
		
		//Calculate arrow position
		Vector diff = target.getPosCentre().minus(observer.getPosCentre());
		int dist = (int)diff.length();
		diff.setLength(150);
		
		Vector tip = diff.clone();
		Vector botLeft = tip.clone();
		botLeft.setLength(125);
		Vector botRight = botLeft.clone();
		botLeft.rotateBy((float)-(Math.PI/32.0));
		botRight.rotateBy((float)(Math.PI/32.0));
		tip = SCREEN_MID.plus(tip);
		botLeft = SCREEN_MID.plus(botLeft);
		botRight = SCREEN_MID.plus(botRight);
		
		//Good radars make difference from enemies and allies
		if( observer.radarLevel >= 2 )
		{
			//Allies get green arrow
			if(observer.team.equals(target.team)) g.setColor( FRIEND );
			else						          g.setColor( ENEMY );
		}
		else g.setColor( NEUTRAL );
		
		//Draw the arrow pointer
		int[] xPoints = new int[]{ tip.getX(), botLeft.getX(), botRight.getX() };
		int[] yPoints = new int[]{ tip.getY(), botLeft.getY(), botRight.getY() };
		g.fillPolygon(xPoints, yPoints, 3);
				
		//Draw distance to target (Radar level 2 or higher)
		if( observer.radarLevel >= 2 )
		{
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
	}
	
	private void drawRadarStatus(Graphics2D g, Spaceship target) {
		
		if( observer.radarLevel < 2 ) return;
				
		int height = target.getImage().getHeight();
		int width = target.getImage().getWidth();
		
		//Calculate position
		Vector diff = target.getPosCentre().minus(viewPort.getCameraPos());
		int drawX = diff.getX() - width/2;
		int drawY = diff.getY() - height/2 - height/4;
		
		//Shield
		g.setColor(RADAR_BACK);
		g.fillRoundRect(drawX, drawY, width, height/8, 25, 25);
		
		g.setColor( RADAR_SHIELD );
		width = Math.max( 0, (int)((width/target.shieldMax) * target.shield) );
		g.fillRoundRect(drawX, drawY, width, height/8, 25, 25);

		//Life and Energy is only drawn on radar level 4 or higher
		if( observer.radarLevel < 3 ) return;

		//Life
		width = target.getImage().getWidth();
		drawY -= height/8;
		g.setColor(RADAR_BACK);
		g.fillRoundRect(drawX, drawY, width, height/8, 25, 25);
		
		g.setColor( RADAR_LIFE );
		width = Math.max( 0, (int)((width/target.lifeMax) * target.life) );
		g.fillRoundRect(drawX, drawY, width, height/8, 25, 25);

		//Energy
		width = target.getImage().getWidth();
		drawY -= height/8;
		g.setColor(RADAR_BACK);
		g.fillRoundRect(drawX, drawY, width, height/8, 25, 25);
		
		g.setColor( RADAR_ENERGY );
		width = Math.max( 0, (int)((width/target.energyMax) * target.energy) );
		g.fillRoundRect(drawX, drawY, width, height/8, 25, 25);
		
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
