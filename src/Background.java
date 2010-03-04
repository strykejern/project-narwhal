import java.awt.*;
import java.util.Random;

public class Background {
	private int[] x, y, size;
	
	public Background(int width, int height){
		int s = 250;
		x = new int[s];
		y = new int[s];
		size = new int[s];
		
		for (int i = 0; i < s; ++i)
		{
			Random rand = new Random();
			x[i] = rand.nextInt(width);
			y[i] = rand.nextInt(height);
			
			size[i] = rand.nextInt(40);
		}
	}
	
	public void update(){
		
	}
	
	public void draw(Graphics g){
		
		for (int i = 0; i < size.length; ++i)
		{
			Color col = new Color(1f, 1f, 1f, (float)((float)(0.5f)/(float)(size[i]+1)));
			for (int k = 0; k < size[i]; ++k)
			{
				g.setColor(col);
				g.fillOval(x[i]+k, y[i]+k, size[i]-(2*k), size[i]-(2*k));
				col = new Color(1f, 1f, ((float)i/(float)size.length), (float)((float)(k+1)/(float)(size[i]+1)));
			}
			
			/*
			g.setColor(Color.GRAY);
			g.drawLine(x[i], y[i], x[i]+size[i], y[i]+size[i]);
			g.drawLine(x[i]+size[i], y[i], x[i], y[i]+size[i]);
			g.drawLine(x[i]+(size[i]/2), y[i], x[i]+(size[i]/2), y[i]+size[i]);
			g.drawLine(x[i], y[i]+(size[i]/2), x[i]+size[i], y[i]+(size[i]/2));
			*/
		}
	}
}
