package narwhal;

import gameEngine.*;
import gameEngine.Configuration.VideoQuality;
import gameEngine.GameWindow.GameState;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;


public class MainMenu {
	private ArrayList<Image2D> background;
	private int currentBackground;
	
	private Input key;
	private Image2D header;
	
	//Buttons used in the main menu
	private HashMap<Integer, Button> buttonList;
	static final int BUTTON_START_GAME = 0;
	static final int BUTTON_OPTIONS = 1;
	static final int BUTTON_EXIT = 2;
	static final int BUTTON_GFX = 3;
	static final int BUTTON_MAIN_MENU = 4;
	static final int BUTTON_GRAPHICS = 5;
	static final int BUTTON_SOUND = 6;
	static final int BUTTON_MUSIC = 7;
	static final int BUTTON_RESUME_GAME = 8;
	static final int BUTTON_FULL_SCREEN = 9;
	static final int BUTTON_START_SKIRMISH = 10;
	static final int BUTTON_START_CAMPAIGN = 11;
	static final int BUTTON_RETRY = 12;
	
	public MainMenu(Input key) {
		Random rand = new Random();
		this.key = key;
			
		//Menu music
		Music.play( "menu.ogg" );
		
		//Initialize background
		loadBackgrounds();
		
		//Load game title
		header = new Image2D("/data/title.png");
		header.resize(GameEngine.getScreenWidth()/2, GameEngine.getScreenHeight()/8);
		header.setAlpha(0.1f);

    	//Load buttons
    	buttonList = new HashMap<Integer, Button>();
    	Vector pos = new Vector( GameEngine.getScreenWidth()/2, GameEngine.getScreenHeight()/3 );
    	Vector size = new Vector( 200, 50 );
    	Vector startPos = new Vector( rand.nextInt(GameEngine.getScreenWidth()), rand.nextInt(GameEngine.getScreenHeight()) );
    	
    	//Main Menu
    	buttonList.put( BUTTON_START_GAME, new Button(pos, size, "START GAME", BUTTON_START_GAME, startPos ) );
    	buttonList.put( BUTTON_RESUME_GAME, new Button(pos, size, "RESUME GAME", BUTTON_RESUME_GAME, startPos ) );
    	buttonList.put( BUTTON_RETRY, new Button(pos, size, "RETRY MISSION", BUTTON_RETRY, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( BUTTON_OPTIONS, new Button(pos, size, "OPTIONS", BUTTON_OPTIONS, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( BUTTON_EXIT, new Button(pos, size, "EXIT GAME", BUTTON_EXIT, startPos ) );
    	pos.y += size.y*1.1f;

    	//Options Menu
    	String gfxText = "Graphics: Normal";
		if( GameEngine.getConfig().getQualityMode() == VideoQuality.VIDEO_LOW ) gfxText = "Graphics: Low";
		else if( GameEngine.getConfig().getQualityMode() == VideoQuality.VIDEO_HIGH ) gfxText = "Graphics: High";
		
    	String sndText = "Sound: On";
		if( !Sound.enabled ) sndText = "Sound: Off";

    	String musText = "Music: On";
		if( !Music.musicEnabled ) musText = "Music: Off";

    	String screenText = "Fullscreen: Yes";
		if( !GameEngine.getConfig().fullScreen ) screenText = "Fullscreen: No";

		pos = new Vector( GameEngine.getScreenWidth()/2, GameEngine.getScreenHeight()/3 );
    	startPos = new Vector(GameEngine.getScreenWidth()/2, GameEngine.getScreenHeight()/2 );
    	buttonList.put( BUTTON_GRAPHICS, new Button(pos, size, gfxText, BUTTON_GRAPHICS, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( BUTTON_SOUND, new Button(pos, size, sndText, BUTTON_SOUND, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( BUTTON_MUSIC, new Button(pos, size, musText, BUTTON_MUSIC, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( BUTTON_FULL_SCREEN, new Button(pos, size, screenText, BUTTON_FULL_SCREEN, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( BUTTON_MAIN_MENU, new Button(pos, size, "BACK", BUTTON_MAIN_MENU, startPos ) );

		pos = new Vector( GameEngine.getScreenWidth()/2, GameEngine.getScreenHeight()/3 );
    	startPos = new Vector(GameEngine.getScreenWidth()/2, GameEngine.getScreenHeight()/2 );
    	buttonList.put( BUTTON_START_SKIRMISH, new Button(pos, size, "SKIRMISH GAME", BUTTON_START_SKIRMISH, startPos ) );
    	pos.y += size.y*1.1f;
    	buttonList.put( BUTTON_START_CAMPAIGN, new Button(pos, size, "BEGIN CAMPAIGN", BUTTON_START_CAMPAIGN, startPos ) );

    	//Hide the buttons
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().hide();
    	
        //But show the top main menu buttons
    	buttonList.get(BUTTON_START_GAME).show();
    	buttonList.get(BUTTON_OPTIONS).show();
    	buttonList.get(BUTTON_EXIT).show();
	}
	
	private void loadBackgrounds() {
		String[] fileList = ResourceMananger.getFileList("/data/backgrounds/");
		
		//Ready array lists
		background = new ArrayList<Image2D>();

		//Load all particles into the hash map
		for( String fileName : fileList )
		{	
			Image2D load = new Image2D(fileName);
			load.setAlpha(0);
			load.resize( GameEngine.getScreenWidth(), GameEngine.getScreenHeight() );
			background.add( load );
		}
		
		//Prepare the first image
		Random rand = new Random();
		currentBackground = rand.nextInt(background.size()-1);
		background.get(currentBackground).setAlpha(1.00f);
	}
	
	public GameWindow.GameState showMainMenu( boolean gameActive ) {
    	//Hide the buttons
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().hide();
    	
        //But show the top main menu buttons
    	if(!gameActive) 
    	{
    		buttonList.get(BUTTON_START_GAME).show();
	       	Music.play("menu.ogg");
    	}
    	else buttonList.get(BUTTON_RESUME_GAME).show();
    	buttonList.get(BUTTON_OPTIONS).show();
    	buttonList.get(BUTTON_EXIT).show();
    	
    	return GameState.GAME_MENU;
	}
	
	//JJ> Main menu loop
	public GameWindow.GameState update( boolean gameActive ) {
				
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
						//Display new buttons
				    	buttonList.get(BUTTON_START_CAMPAIGN).show();
				    	buttonList.get(BUTTON_START_SKIRMISH).show();
				    	buttonList.get(BUTTON_MAIN_MENU).show();
				    	
				    	//Hide the existing buttons
				    	buttonList.get(BUTTON_START_GAME).hide();
				    	buttonList.get(BUTTON_OPTIONS).hide();
				    	buttonList.get(BUTTON_EXIT).hide();
				    	break;
					}
					
					case BUTTON_RETRY:
					{
						return GameState.GAME_SELECT_SHIP;
					}
					
					case BUTTON_RESUME_GAME:
					{
						return GameState.GAME_PLAYING;
					}
					
					case BUTTON_START_SKIRMISH:
					{
				    	//Hide the existing buttons
				    	buttonList.get(BUTTON_START_CAMPAIGN).hide();
				    	buttonList.get(BUTTON_START_SKIRMISH).hide();
				    	buttonList.get(BUTTON_MAIN_MENU).hide();
				    	
						//Display new buttons
				    	buttonList.get(BUTTON_RESUME_GAME).show();
				    	buttonList.get(BUTTON_OPTIONS).show();
				    	buttonList.get(BUTTON_EXIT).show();
				    	
				    	//Play selection music
				       	Music.play( "space.ogg" );
				       	
						return GameWindow.GameState.GAME_SELECT_SHIP;
					}
					
					case BUTTON_START_CAMPAIGN:
					{
				    	//Hide the existing buttons
				    	buttonList.get(BUTTON_START_CAMPAIGN).hide();
				    	buttonList.get(BUTTON_START_SKIRMISH).hide();
				    	buttonList.get(BUTTON_MAIN_MENU).hide();
				    	
						//Display new buttons
				    	buttonList.get(BUTTON_RESUME_GAME).show();
				    	buttonList.get(BUTTON_OPTIONS).show();
				    	buttonList.get(BUTTON_EXIT).show();
			       	
						return GameWindow.GameState.GAME_CAMPAIGN_SCREEN;
					}

					
					case BUTTON_OPTIONS:
					{
						//Fade in the next buttons
				    	buttonList.get(BUTTON_GRAPHICS).show();
				    	buttonList.get(BUTTON_SOUND).show();
				    	buttonList.get(BUTTON_MUSIC).show();
				    	buttonList.get(BUTTON_FULL_SCREEN).show();
				    	buttonList.get(BUTTON_MAIN_MENU).show();
				    	
				    	//Fade out the existing buttons
				    	buttonList.get(BUTTON_RESUME_GAME).hide();
				    	buttonList.get(BUTTON_START_GAME).hide();
				    	buttonList.get(BUTTON_OPTIONS).hide();
				    	buttonList.get(BUTTON_EXIT).hide();
						break;
					}
					
					case BUTTON_EXIT:
					{
						if( gameActive ) 
						{
							buttonList.get(BUTTON_RESUME_GAME).hide();
							buttonList.get(BUTTON_START_GAME).show();
							return GameState.GAME_END_CURRENT;
						}
						return GameWindow.GameState.GAME_EXIT;
					}
					
					case BUTTON_MAIN_MENU:
					{
						//Fade in the next buttons
				    	buttonList.get(BUTTON_OPTIONS).show();
				    	buttonList.get(BUTTON_EXIT).show();
						if(gameActive) buttonList.get(BUTTON_RESUME_GAME).show();
						else 		   buttonList.get(BUTTON_START_GAME).show();
				    	
				    	//Fade out the current buttons
				    	buttonList.get(BUTTON_START_SKIRMISH).hide();
				    	buttonList.get(BUTTON_START_CAMPAIGN).hide();
				    	buttonList.get(BUTTON_GRAPHICS).hide();
				    	buttonList.get(BUTTON_SOUND).hide();
				    	buttonList.get(BUTTON_MUSIC).hide();
				    	buttonList.get(BUTTON_FULL_SCREEN).hide();
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

					case BUTTON_MUSIC:
					{
						Music.musicEnabled = !Music.musicEnabled;
						if( Music.musicEnabled )
						{
							Music.play("menu.ogg");
							buttonList.get(BUTTON_MUSIC).setText("MUSIC: ON");
						}
						else
						{
							Music.stopMusic();
							buttonList.get(BUTTON_MUSIC).setText("MUSIC: OFF");
						}
						break;
					}

					case BUTTON_FULL_SCREEN:
					{
						GameEngine.getConfig().fullScreen ^= true;			//Awesome fast method to invert a boolean
						if( GameEngine.getConfig().fullScreen ) button.setText("Fullscreen: Yes");
						else				   					button.setText("Fullscreen: No");
						
				    	GameEngine.startNewGame("Narwhal");		

						break;
					}
					
					case BUTTON_GRAPHICS:
					{
						Configuration config = GameEngine.getConfig();
						if( config.getQualityMode() == VideoQuality.VIDEO_LOW )
						{
							button.setText("Graphics: Normal");
							config.setVideoQuality( VideoQuality.VIDEO_NORMAL );
						}
						else if( config.getQualityMode() == VideoQuality.VIDEO_NORMAL )
						{
							button.setText("Graphics: High");
							config.setVideoQuality( VideoQuality.VIDEO_HIGH );
						}
						else if( config.getQualityMode() == VideoQuality.VIDEO_HIGH )
						{
							button.setText("Graphics: Low");
							config.setVideoQuality( VideoQuality.VIDEO_LOW );
						}
						break;
					}
				}
			}
		}
		
		//Slowly fade in the header
		if(header.getAlpha() != 1) 	header.setAlpha( header.getAlpha() + 0.0075f );
		
		//Update background effects
		if( !gameActive )
		{
			int nextBackground = currentBackground + 1;
			if( nextBackground >= background.size() ) nextBackground = 0;
			Image2D currentBg = background.get(currentBackground);
			Image2D nextBg = background.get(nextBackground);
			
			//Fade out the current one and fade in the new one
			currentBg.setAlpha ( currentBg.getAlpha() - 0.00125f );
			nextBg.setAlpha    ( nextBg.getAlpha() + 0.00125f );
			
			//We have reached a new background
			if( currentBg.getAlpha() == 0 )
			{
				currentBg.horizontalFlip();		//This little trick makes images seem more random
				currentBackground = nextBackground;
			}
		}
		
		return GameState.GAME_MENU;
	}
		
	public void draw(Graphics2D g, Game inGame) {
		
		//Draw background
		if(inGame == null)
		{
			int nextBackground = currentBackground + 1;
			if( nextBackground >= background.size() ) nextBackground = 0;
			background.get(currentBackground).draw(g, 0, 0);
			background.get(nextBackground).draw(g, 0, 0);
		}
		else inGame.draw(g);
		
		//Draw header, but only if it is loaded
		if( header != null ) header.draw(g, (GameEngine.getScreenWidth()/2) - header.getWidth()/2, header.getHeight() );
		
		//Do last, draw buttons
		String hint = null;
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) 
        {
        	Button button = iterator.next(); 
        	button.draw(g);
        	
        	//Draw description hint text for specific buttons
        	if( button.getID() == BUTTON_START_SKIRMISH && button.mouseOver(key) )
        		hint = "Play against the computer in a single Skirmish game.";
        	else if( button.getID() == BUTTON_START_CAMPAIGN && button.mouseOver(key) )
        		hint = "Play against the AI in series of battles while upgrading your ship.";
        }
        
        //Draw any button hint
        if( hint != null )
        {
       		Rectangle txt = buttonList.get(BUTTON_START_CAMPAIGN).getButtonArea();
    		g.setColor(Color.WHITE);
    		g.drawString(hint, GameEngine.getScreenWidth()/2 - GameFont.getWidth(hint, g)/2, txt.height*2 + txt.y);
        }
	}

	public void showRetry() {
        Iterator<Button> iterator = buttonList.values().iterator();
        while( iterator.hasNext() ) iterator.next().hide();
    	
		buttonList.get(BUTTON_RETRY).show();
		buttonList.get(BUTTON_EXIT).show();
	}
	
}

