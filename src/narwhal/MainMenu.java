package narwhal;

import gameEngine.*;
import gameEngine.GameWindow.gameState;
import gameEngine.Video.VideoQuality;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;


public class MainMenu {
	private Background background;
	private Vector bgScroll = new Vector(), bgSpeed;
	private Input key;
	
	private Image2D header;
	private float headerFade = 0;
	
	//Buttons used in the main menu
	private HashMap<Integer, Button> buttonList;
	static final int BUTTON_START_GAME = 0;
	static final int BUTTON_OPTIONS = 1;
	static final int BUTTON_EXIT = 2;
	static final int BUTTON_GFX = 3;
	static final int BUTTON_MAIN_MENU = 4;
	static final int BUTTON_GRAPHICS = 5;
	static final int BUTTON_SOUND = 6;
	
	
	public MainMenu(Input key) {
		Random rand = new Random();
		this.key = key;
			
		//Menu music
		//Music.play( new Sound("menu.ogg") );
		
		//Initialize the background
    	background = new Background(2, System.currentTimeMillis());
    	bgSpeed = new Vector(rand.nextInt(4)-2, rand.nextInt(4)-2);
    	    	
    	//Load buttons
    	buttonList = new HashMap<Integer, Button>();
    	Vector pos = new Vector( Video.getScreenWidth()/2, Video.getScreenHeight()/3 );
    	Vector size = new Vector( 200, 50 );
    	Vector startPos = new Vector( rand.nextInt(Video.getScreenWidth()), rand.nextInt(Video.getScreenHeight()) );
    	
    	//Main Menu
    	buttonList.put( BUTTON_START_GAME, new Button(pos, size, "START GAME", BUTTON_START_GAME, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( BUTTON_OPTIONS, new Button(pos, size, "OPTIONS", BUTTON_OPTIONS, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( BUTTON_EXIT, new Button(pos, size, "EXIT GAME", BUTTON_EXIT, startPos ) );

    	//Options Menu
    	String gfxText = "Graphics: Normal";
		if( Video.getQualityMode() == VideoQuality.VIDEO_LOW ) gfxText = "Graphics: Low";
		else if( Video.getQualityMode() == VideoQuality.VIDEO_HIGH ) gfxText = "Graphics: High";
		
    	String sndText = "Sound: On";
		if( !Sound.enabled ) sndText = "Sound: Off";

		pos = new Vector( Video.getScreenWidth()/2, Video.getScreenHeight()/3 );
    	startPos = new Vector(Video.getScreenWidth()/2, Video.getScreenHeight()/2 );
    	buttonList.put( BUTTON_GRAPHICS, new Button(pos, size, gfxText, BUTTON_GRAPHICS, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( BUTTON_SOUND, new Button(pos, size, sndText, BUTTON_SOUND, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put(BUTTON_MAIN_MENU, new Button(pos, size, "BACK", BUTTON_MAIN_MENU, startPos ) );
    	buttonList.get(BUTTON_GRAPHICS).hide();
    	buttonList.get(BUTTON_SOUND).hide();
    	buttonList.get(BUTTON_MAIN_MENU).hide();
	}
	
	//JJ> Main menu loop
	public GameWindow.gameState update(boolean newGame) {
		
		if( newGame ) buttonList.get(BUTTON_START_GAME).setText("Start New Game");
		else		  buttonList.get(BUTTON_START_GAME).setText("Resume Game");
			
        //Check if the player is holding over any mouse buttons
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() )
        {
        	Button button = iterator.next();
			button.update();
			
			//Clicked as well?
			if( button.mouseOver(key) && key.mosButton1 )
			{
				key.mosButton1 = false;
				button.playClick();
				
				//Determine button effect
				switch( button.getID() )
				{
					case BUTTON_START_GAME:
					{
						//return GameWindow.gameState.GAME_PLAYING;
						return GameWindow.gameState.GAME_SELECT_SHIP;		//Ship selection
					}
					
					case BUTTON_OPTIONS:
					{
						//Fade in the next buttons
				    	buttonList.get(BUTTON_GRAPHICS).show();
				    	buttonList.get(BUTTON_SOUND).show();
				    	buttonList.get(BUTTON_MAIN_MENU).show();
				    	
				    	//Fade out the existing buttons
				    	buttonList.get(BUTTON_START_GAME).hide();
				    	buttonList.get(BUTTON_OPTIONS).hide();
				    	buttonList.get(BUTTON_EXIT).hide();
						break;
					}
					
					case BUTTON_EXIT:
					{
						return GameWindow.gameState.GAME_EXIT;
					}
					
					case BUTTON_MAIN_MENU:
					{
						//Fade in the next buttons
				    	buttonList.get(BUTTON_START_GAME).show();
				    	buttonList.get(BUTTON_OPTIONS).show();
				    	buttonList.get(BUTTON_EXIT).show();
				    	
				    	//Fade out the current buttons
				    	buttonList.get(BUTTON_GRAPHICS).hide();
				    	buttonList.get(BUTTON_SOUND).hide();
				    	buttonList.get(BUTTON_MAIN_MENU).hide();
						break;
					}
					
					case BUTTON_SOUND:
					{
						Sound.enabled = !Sound.enabled;
						if( Sound.enabled )
							buttonList.get(BUTTON_SOUND).setText("SOUND: ON");
						else
							buttonList.get(BUTTON_SOUND).setText("SOUND: OFF");
						break;
					}
					
					case BUTTON_GRAPHICS:
					{
						if( Video.getQualityMode() == VideoQuality.VIDEO_LOW )
						{
							button.setText("Graphics: Normal");
							Video.setVideoQuality( VideoQuality.VIDEO_NORMAL );
						}
						else if( Video.getQualityMode() == VideoQuality.VIDEO_NORMAL )
						{
							button.setText("Graphics: High");
							Video.setVideoQuality( VideoQuality.VIDEO_HIGH );
						}
						else if( Video.getQualityMode() == VideoQuality.VIDEO_HIGH )
						{
							button.setText("Graphics: Low");
							Video.setVideoQuality( VideoQuality.VIDEO_LOW );
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
				header = new Image2D("/data/title.png");
				header.resize(Video.getScreenWidth()/2, Video.getScreenHeight()/8);
				header.setAlpha(0.1f);
			}
			
			//headerFade += 0.0075f;
			//header.setAlpha(0.1f);
		}
				
		// Quick implement of universe bounds
		if(bgScroll.x < 0 || bgScroll.x > background.getUniverseSize().x- Video.getScreenWidth()) 	   bgSpeed.negate();
		else if(bgScroll.y < 0 || bgScroll.y > background.getUniverseSize().y- Video.getScreenHeight()) bgSpeed.negate();
		bgScroll.add( bgSpeed );
		
		return gameState.GAME_MENU;
	}
		
	public void draw(Graphics2D g) {
		
		//Draw background
		background.drawBackground( g, bgScroll, bgSpeed );
		
		//Draw header, but only if it is loaded
		if( header != null ) header.draw(g, (Video.getScreenWidth()/2) - header.getWidth()/2, header.getHeight() );

		//Do last, draw buttons
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().draw(g);
	}
	
}

