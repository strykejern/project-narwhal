package narwhal;

import gameEngine.Video;

import java.awt.Color;
import java.awt.Graphics2D;

//Skeleton class for the UI
public class UI {
	private int life;
	private int maxLife;
	
	public UI(){
		life = 500;
		maxLife = 500;
	}
	
	public void draw(Graphics2D g) {
		
		//Calculate positions
		int width = -Video.getScreenWidth()/20;
		int height = -Video.getScreenHeight()/4;
		int x = Video.getScreenWidth() - 10;
		int y = Video.getScreenHeight() - 30;
		
		//LIFE BAR
		//Draw full bar
		g.setColor(Color.DARK_GRAY);
		g.fillRect(x, y, width, height);
		life--;
		
		//Draw remaining bar
		g.setColor( Color.RED );
		g.fillRect( x, y, width, Math.min(0, (int)((height/(float)maxLife) * life)) );
		
		x += width -5;
		
		//SHIELD BAR
		//Draw full bar
		g.setColor(Color.DARK_GRAY);
		g.fillRect(x, y, width, height);
		life--;
		
		//Draw remaining bar
		g.setColor( Color.BLUE );
		g.fillRect( x, y, width, Math.min(0, (int)((height/(float)maxLife) * life)) );
		
		x += width -5;
		
		//ENERGY BAR
		//Draw full bar
		g.setColor(Color.DARK_GRAY);
		g.fillRect(x, y, width, height);
		life--;
		
		//Draw remaining bar
		g.setColor( Color.ORANGE );
		g.fillRect( x, y, width, Math.min(0, (int)((height/(float)maxLife) * life)) );
	}
	
}
