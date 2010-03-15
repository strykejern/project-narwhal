package narwhal;

import gameEngine.Vector;
import gameEngine.Video;

import java.awt.Color;
import java.awt.Graphics2D;

//Skeleton class for the UI
public class UI {
	private Spaceship object;	
	
	public UI(Spaceship observe){
		object = observe;
	}
	
	public void draw(Graphics2D g) {
		
		//Calculate positions
		int width = -Video.getScreenWidth()/20;
		int x = Video.getScreenWidth() - 10;
		int y = Video.getScreenHeight() - 30;
		
		//Status bars
		drawOneBar(g, new Vector(x, y), object.life, object.lifeMax, new Color(153, 0 , 0, 200 ));
		x += width -5;
		
		drawOneBar(g, new Vector(x, y), object.shield, object.shieldMax, new Color(0, 0 , 153, 200 ));
		x += width -5;

		drawOneBar(g, new Vector(x, y), object.energy, object.energyMax, new Color(255, 153 , 0, 200 ));
		x += width -5;

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
}
