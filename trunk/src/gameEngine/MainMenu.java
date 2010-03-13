package gameEngine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import narwhal.Universe;

public class MainMenu extends JPanel implements Runnable, MouseListener {
	private static final long serialVersionUID = 1L;
	private boolean menu, mouseClicked;
	private Universe background;
	private HashMap<ButtonType, Button> buttonList;
	private JFrame frame;
	private Font menuFont;
	private Vector mousePos = new Vector(), bgScroll = new Vector(), bgSpeed;
	private Sound buttonHover, buttonClick;
	private Image2D header;
	
	public MainMenu(JFrame frame) {
		Random rand = new Random();
		this.frame = frame;
		
		//Tiny corner icon
		ImageIcon icon = new ImageIcon("data/icon.png");
		frame.setIconImage( icon.getImage() );
		
		//Initialize the background
    	background = new Universe(Video.getResolution(), 2, System.currentTimeMillis());
    	bgSpeed = new Vector(rand.nextInt(4)-2, rand.nextInt(4)-2);

    	//Load font
    	if( menuFont == null ) intializeFont();
    	
    	//Mouse control
		frame.addMouseListener(this);
    	
    	//Load buttons
    	buttonList = new HashMap<ButtonType, Button>();
    	Vector pos = new Vector( Video.getScreenWidth()/2, Video.getScreenHeight()/3 );
    	Vector size = new Vector( 200, 50 );

    	//Main Menu
    	buttonList.put( ButtonType.BUTTON_START_GAME, new Button(pos, size, "START GAME", ButtonType.BUTTON_START_GAME ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( ButtonType.BUTTON_OPTIONS, new Button(pos, size, "OPTIONS", ButtonType.BUTTON_OPTIONS) );
    	pos.y += size.y*1.1f;
    	buttonList.put( ButtonType.BUTTON_EXIT, new Button(pos, size, "EXIT GAME", ButtonType.BUTTON_EXIT) );

    	//Options Menu
    	pos = new Vector( Video.getScreenWidth()/2, Video.getScreenHeight()/3 );
    	buttonList.put( ButtonType.BUTTON_GRAPHICS, new Button(pos, size, "GRAPHICS: LOW", ButtonType.BUTTON_GRAPHICS ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( ButtonType.BUTTON_MAIN_MENU, new Button(pos, size, "BACK", ButtonType.BUTTON_MAIN_MENU ) );
    	
    	buttonList.get(ButtonType.BUTTON_GRAPHICS).hidden = true;
    	buttonList.get(ButtonType.BUTTON_MAIN_MENU).hidden = true;
    	
		//Thread (do last so that everything above is properly loaded before the main loop begins)
		menu = true;
		mouseClicked = false;
		new Thread(this).start();
	}

	//JJ> Prepares our awesome font
	private boolean intializeFont() {
		try 
		{
			//Load our awesome font
			menuFont = Font.createFont(Font.TRUETYPE_FONT, new File("data/font.ttf") );
			menuFont = menuFont.deriveFont(Font.BOLD, 14);
			return true;
		} 
		catch (Exception e) 
		{
			//Use default font instead
			Log.warning(e);
			menuFont = new Font("Arial", Font.BOLD, 14);
		}
		return false;
	}
	
		
	private int getFontWidth(String text, Graphics2D g){
		FontMetrics metric = g.getFontMetrics();
		return metric.stringWidth(text);
	}
	
	//JJ> Main menu loop
	public void run() {
    	long tm = System.currentTimeMillis();
		float headerFade = 0;
		
		//Menu music
		Sound music = new Sound("data/menu.ogg");
    	music.playLooped();
    	music.setVolume( 0.75f );
    	
    	//Load button sounds
    	buttonHover = new Sound("data/hover.au");
    	buttonHover.setVolume(0.10f);
    	buttonClick = new Sound("data/click.au");
    	
		while( menu ) {
			mousePos.x = MouseInfo.getPointerInfo().getLocation().x - frame.getX()-5;
	        mousePos.y = MouseInfo.getPointerInfo().getLocation().y - frame.getY()-20;
	        
	        //Check if the player is holding over any mouse buttons
	        Iterator<Button> iterator = buttonList.values().iterator();
	        while( iterator.hasNext() )
	        {
	        	Button button = iterator.next();
				button.update();
				if(button.mouseOver() && mouseClicked)
				{	
					mouseClicked = false;
					buttonClick.play();
					
					//Determine button effect
					switch( button.type )
					{
						case BUTTON_START_GAME:
						{
							//TODO: start Game()
							break;
						}
						
						case BUTTON_OPTIONS:
						{
							//Fade in the next buttons
					    	buttonList.get(ButtonType.BUTTON_GRAPHICS).show();
					    	buttonList.get(ButtonType.BUTTON_MAIN_MENU).show();
					    	
					    	//Fade out the existing buttons
					    	buttonList.get(ButtonType.BUTTON_START_GAME).hide();
					    	buttonList.get(ButtonType.BUTTON_OPTIONS).hide();
					    	buttonList.get(ButtonType.BUTTON_EXIT).hide();
							break;
						}
						
						case BUTTON_EXIT:
						{
							System.exit(0);
							break;
						}
						
						case BUTTON_MAIN_MENU:
						{
							//Fade in the next buttons
					    	buttonList.get(ButtonType.BUTTON_START_GAME).show();
					    	buttonList.get(ButtonType.BUTTON_OPTIONS).show();
					    	buttonList.get(ButtonType.BUTTON_EXIT).show();
					    	
					    	//Fade out the current buttons
					    	buttonList.get(ButtonType.BUTTON_GRAPHICS).hide();
					    	buttonList.get(ButtonType.BUTTON_MAIN_MENU).hide();
							break;
						}
						
						case BUTTON_GRAPHICS:
						{
							if(Video.isHighQualityMode()) 
							{
								button.text = "Graphics: Low";
								Video.disableHighQualityGraphics();
							}
							else 
							{
								button.text = "Graphics: High";
								Video.enableHighQualityGraphics();
							}
						}
					}
				}
			}
			
			//Slowly fade in the header
			if(headerFade != 1)
			{
				//Load the header image here so that it doesn't blink
				if(header == null)
				{
					header = new Image2D("data/title.png");
					header.resize(Video.getScreenWidth()/2, Video.getScreenHeight()/8);
				}
				
				headerFade += 0.0075f;
				header.setAlpha(headerFade);
			}
			
			//Make the background move around
			bgScroll.add( bgSpeed );
			
			// Quick implement of universe bounds
			float uniX = Video.getScreenWidth()*Universe.getUniverseSize();
			float uniY = Video.getScreenHeight()*Universe.getUniverseSize();
			
			if 		(bgScroll.x < 0) 	 bgScroll.x = uniX + bgScroll.x;
			else if (bgScroll.x > uniX)  bgScroll.x %= uniX;
			
			if 		(bgScroll.y < 0) 	 bgScroll.y = uniY + bgScroll.y;
			else if (bgScroll.y > uniY)  bgScroll.y %= uniY;

    		try 
    		{
                tm += 1000/60;
                Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
            }
            catch(InterruptedException e)
            {
            	Log.warning(e.toString());
            }
            
            repaint();
		}
		
		music.stop();
	}
		
	/*
	 * JJ> Paints every object of interest
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics rawGraphics) {
		//Convert to the Graphics2D object which allows us more functions
		Graphics2D g = (Graphics2D) rawGraphics;
		
		//Set quality mode
		Video.getGraphicsSettings(g);
		
		//Draw background
		background.drawBackground( g, bgScroll );
		//background.drawPlanets( g );
		
		//Draw header, but only if it is loaded
		if( header != null )
			g.drawImage( header.toImage(), Video.getScreenWidth()/2-header.getWidth()/2, header.getHeight(), null );

		//Do last, draw buttons
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().draw(g);
      
		//Done with this frame
		g.dispose();
		
		}
	
	
	enum ButtonType {
		BUTTON_START_GAME,
		BUTTON_OPTIONS,
		BUTTON_EXIT,
		BUTTON_GFX, 
		BUTTON_MAIN_MENU,
		BUTTON_GRAPHICS
	}
	
	class Button {
		private Vector pos, size, movePos;
		private String text;
		private boolean hidden;
		private boolean mouseOver;
		private float alphaAdd;
		private float alpha;
		private ButtonType type;		
		
		public Button(Vector pos, Vector size, String text, ButtonType type) {
			alpha = 0;
			hidden = false;
			mouseOver = false;
			movePos = new Vector(0, 0);
			alphaAdd = 0;
			this.pos = pos.minus( size.dividedBy(2) );
			this.size = size;
			this.text = text.toUpperCase();
			this.type = type;
		}
		
		public boolean mouseOver() {
			if( hidden || alpha != 0 )
			{
				mouseOver = false;
				return false;
			}
	        
			//Are they holding the mouse over this object?
	        if( mousePos.x > pos.x && mousePos.x < pos.x + size.x )
	        	if( mousePos.y > pos.y && mousePos.y < pos.y + size.y )
	        	{
	        		if( !mouseOver ) buttonHover.play();
	        		mouseOver = true;
	        		return true;
	        	}
	        
	        mouseOver = false;
	        return false;
		}
		
		public void hide(){
			if( hidden ) return;
			movePos.x =  Video.getScreenWidth()/2-size.x/2;
			movePos.y = Video.getScreenHeight()/2-size.y/2;
			alphaAdd = 0.5f;
		}
		
		public void show(){
			if( !hidden ) return;
			hidden = false;
			alphaAdd = -0.5f;
		}
		
		public void update() {
			
			if(hidden) return;
			
			//Is this button fading?
			if( alphaAdd != 0 )
			{
				alpha += alphaAdd;
				
				//Finished fade?
				if( Math.abs(alpha) >= 1 )
				{
					if( alpha >= 1 ) hidden = true;
					alphaAdd = 0;
					alpha = 0;
				}
			}
			
			//Make it move if needed
			if( movePos.x < pos.x ) movePos.x += 4;
			if( movePos.y < pos.y ) movePos.y += 4;
			if( movePos.x > pos.x ) movePos.x -= 4;
			if( movePos.y > pos.y ) movePos.y -= 4;
		}
		
		public void draw(Graphics2D g){
			if( hidden ) return;
				
			//Calculate fade away
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
			if( mouseOver ) g.setColor(new Color(1, 1, 1, solid) );
			else		    g.setColor(new Color(0, 0, 0, solid) );
			
			g.setFont( menuFont );
			g.drawString(text, movePos.getX()+ (size.getX()/2) - (getFontWidth(text, g)/2), movePos.getY()+ size.getY()/1.75f);
		}
		
	}



	public void mouseReleased(MouseEvent arg0) {
		mouseClicked = false;		
	}
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) mouseClicked = true;
	}
	
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}

