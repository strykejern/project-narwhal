import java.awt.*;
import javax.swing.*;


public class Game extends JPanel implements Runnable {
	private static final int FPS = 1000 / 60;
	
	public static void main(String[] args){
		JFrame parentWindow = new JFrame("The game");
    	
    	parentWindow.getContentPane().add(new Game());
    	
    	parentWindow.setSize(800 , 600);
        parentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentWindow.setVisible(true);
	}
	
	public Game()
	{
		new Thread(this).start();
		
	}
	
	public void run() 
	{
    	// Remember the starting time
    	long tm = System.currentTimeMillis();
    	
    	while(true){
    		repaint();
    		
    		try {
                tm += FPS;
                Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
            }
            catch(InterruptedException e)
            {
            	System.out.println(e);
            }
    	}
		
	}
	
	public void paint(Graphics g){
		g.setColor(Color.black);

		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setColor(Color.cyan);
		g.fillOval(50, 50, 100, 100);
	}
}
