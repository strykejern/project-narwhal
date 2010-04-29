package narwhal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import narwhal.AI.aiType;
import narwhal.GameFont.FontType;
import narwhal.Shipyard.SpecialModule;
import narwhal.SpawnPoint.Type;

import gameEngine.*;
import gameEngine.GameWindow.GameState;

public class CampaignScreen {
	private Image2D background;
	private Button begin;
	private Input key;
	private Shipyard spawnShip;
		
	private ArrayList<SpawnPoint> spawnList;
	
	private String mission;
	private ArrayList<String> description;
	private Sound narrator;
	public boolean active;
	private boolean alwaysWin;
	private int universeSize;

	private String nextMission;
	private boolean doNarrator;

	public CampaignScreen(Input key, Shipyard spawnShip) {
		this.key = key;
		this.spawnShip = spawnShip;
		active = false;
		doNarrator = true;
		
		Vector pos = new Vector(GameEngine.getScreenWidth()-100, GameEngine.getScreenHeight()-100);
		begin = new Button(pos, new Vector(150, 100 ), "Begin", 0, pos);
		
		//Set default values
		spawnList = new ArrayList<SpawnPoint>();		
		description = new ArrayList<String>();
	}
	
	public void loadMission(String fileName) {
		
		if( !ResourceMananger.fileExists(fileName) ) 
		{
			Log.error( "Could not load mission - " + fileName );
			return;
		}
		
		BufferedReader parse = new BufferedReader(
				new InputStreamReader(
				ResourceMananger.getInputStream(fileName)));
		
		//Clear stuff from previous levels
		description.clear();
		spawnList.clear();
		narrator = null;
		alwaysWin = false;
		universeSize = 4;
		
		//Parse the ship file
		try 
		{
			while(true)
			{
				String line;
				line = parse.readLine();
				
				//Reached end of file
				if(line == null) break;
				
				//Ignore comments
				if( line.startsWith("//") || line.equals("") || line.indexOf("NONE") != -1 ) continue;
				
				//Translate line into data
				if     (line.startsWith("[MISSION]:"))    	  	mission = parse(line);
				else if(line.startsWith("[DESCRIPTION]:"))
				{
					String text = parse(line);
					
					//This splits the long string into multiple strings
					while( text.length() > 36 )
					{
						int c = 36;
						for(; text.charAt(c) != ' '; c--);
						description.add(text.substring(0, c).trim());
						text = text.substring(c);
					}
					description.add(text.trim());
				}
				else if(line.startsWith("[SET_PLAYER_SHIP]:")) 	spawnShip.setCurrentShip( new SpaceshipTemplate( parse(line) ) );
				else if(line.startsWith("[VOICE]:")) 			narrator = new Sound( parse(line) );
				else if(line.startsWith("[MUSIC]:")) 			Music.play( parse(line ) );
				else if(line.startsWith("[IMAGE]:"))
				{
					background = new Image2D(parse(line));
					background.resize(GameEngine.getScreenWidth(), GameEngine.getScreenHeight());
				}
				else if(line.startsWith("[NEXT]:")) 			nextMission = parse(line);
				else if(line.startsWith("[SHIP]:"))
				{
					String[] load = parse(line).split(" ");
					
					//Make sure we have all we need first
					if(load.length != 5)
					{
						Log.warning("Could not spawn ship (" + fileName + ") missing data - " + parse(line));
						continue;
					}
					
					//Load it and add it to the list
					SpawnPoint spawn = new SpawnPoint( Type.SPACESHIP );
					
					//Get AI type
					if( load[3].equals("CONTROLLER") ) 		spawn.ai = aiType.CONTROLLER;
					else if( load[3].equals("BRUTE") ) 		spawn.ai = aiType.BRUTE;
					else if( load[3].equals("AMBUSHER") ) 	spawn.ai = aiType.AMBUSH;
					else spawn.ai = aiType.FOOL;
					
					spawn.pos = new Vector(Integer.parseInt(load[1]), Integer.parseInt(load[2]) );
					spawn.team = load[4];
					spawn.name = load[0];
					
					spawnList.add( spawn );	
				}
				else if(line.startsWith("[PLAYER]:"))
				{
					String[] load = parse(line).split(" ");
					
					//Make sure we have all we need first
					if(load.length != 3)
					{
						Log.warning("Could not spawn player (" + fileName + ") missing data - " + parse(line));
						continue;
					}
					
					//Load it and add it to the list
					SpawnPoint spawn = new SpawnPoint(Type.PLAYER);
					spawn.pos = new Vector(Integer.parseInt(load[0]), Integer.parseInt(load[1]) );
					spawn.ai = aiType.PLAYER;
					spawn.team = load[2];
					spawnList.add( spawn );
				}
				else if(line.startsWith("[PLANET]:"))
				{
					String[] load = parse(line).split(" ");
					
					//Make sure we have all we need first
					if(load.length != 4)
					{
						Log.warning("Could not spawn ship (" + fileName + ") missing data - " + parse(line));
						continue;
					}
					
					//Load it and add it to the list
					SpawnPoint spawn = new SpawnPoint(Type.PLANET);
					spawn.pos = new Vector(Integer.parseInt(load[1]), Integer.parseInt(load[2]) );
					spawn.name = load[0];
					spawn.size = Integer.parseInt(load[3]);
					
					spawnList.add( spawn );					
				}
				else if(line.startsWith("[SIZE]:")) universeSize = Integer.parseInt( parse(line) );
				else if(line.startsWith("[ALWAYS_WIN]:")) alwaysWin = Boolean.parseBoolean( parse(line) );

				else if(line.startsWith("[ADD_TECH_WEAPON]:"))
				{
					String wpn = parse(line);
					if( ResourceMananger.fileExists("data/weapons/" + wpn) )
						spawnShip.addWeapon( new Weapon( wpn ) );
					else Log.warning("Mission " + fileName + " - Invalid weapon: " + wpn);
				}
				else if(line.startsWith("[ADD_TECH_RADAR]:")) spawnShip.maxRadarLevel = Short.parseShort(parse(line));
				else if(line.startsWith("[ADD_TECH_MODULE]:")) 
				{
					String module = parse(line);
					if( module.equals("ECM") ) spawnShip.addModule( SpecialModule.ECM );
					else if( module.equals("CLOAKING") ) spawnShip.addModule( SpecialModule.CLOAK );
					else if( module.equals("DISGUISE") ) spawnShip.addModule( SpecialModule.DISGUISE );
					else if( module.equals("STRAFING") ) spawnShip.addModule( SpecialModule.STRAFING );
					else if( module.equals("WARP") ) spawnShip.addModule( SpecialModule.WARP );
					else if( module.equals("NULLIFIER") ) spawnShip.addModule( SpecialModule.NULLIFIER );
					else if( module.equals("TETIARY") ) spawnShip.addModule( SpecialModule.TETIARY );
					else if( module.equals("INTERCEPTOR") ) spawnShip.addModule( SpecialModule.INTERCEPTOR );
				}

				//Could not figure it out
				else	Log.warning("Loading mission " + fileName + " - unrecognized line: " + line);
			}			
		} 
		catch (Exception e) 
		{
			Log.warning("Loading mission " + fileName + " - " + e);
		}
		
		if( spawnList.size() == 0 ) begin.setText("Continue");
		else						begin.setText("Start");
		active = true;
	}
	
	public void draw(Graphics2D g){
		if(mission == null) return;
		
		background.draw(g, 0, 0);
		begin.draw(g);
		int size = GameEngine.getScreenWidth() / 24;
		GameFont.set(g, FontType.FONT_MENU, Color.YELLOW, size);
		int y = GameFont.getHeight(g);
		
		//Mission name
		g.drawString(mission, 20, y);
		y += GameFont.getHeight(g) + 20;
		
		//Description text
		size = GameEngine.getScreenWidth() / 36;
		GameFont.set(g, FontType.FONT_DESCRIBE, Color.YELLOW, size);
		for( String text : description )
		{
			g.drawString(text, 20, y);
			y += GameFont.getHeight(g);
		}
	}
	public GameState update() {
		begin.update();
		if( narrator != null && doNarrator ) 
		{
			doNarrator = false;
			narrator.playFull(1.25f);
		}

		//The next button
		if( begin.mouseOver(key) && key.mosButton1 ) 
		{
			key.mosButton1 = false;
			narrator.silence();						//Shut up that voice
			
			if( spawnList.size() == 0  ) 
			{
				loadMission(nextMission);
				doNarrator = true;
			}
			else 						 return GameState.GAME_SELECT_SHIP;
		}

		return GameState.GAME_CAMPAIGN_SCREEN;
	}
	
	public void next(){
		spawnShip.maxTechLevel += 2;
		loadMission(nextMission);
		doNarrator = true;
	}
	
	/**
	 * JJ> This is simply to make parsing easier. Gets whatever is behind the colon and trims all
	 *     whitespace before and after the text.
	 * @param line The String to parse
	 * @return The parsed String
	 */
	private String parse(String line) {
		return line.substring(line.indexOf(':')+1).trim();
	}
	
	public ArrayList<SpawnPoint> getLevelSpawnList(){
		return spawnList;
	}

	public int getUniverseSize(){
		return universeSize;
	}
	
	public boolean alwaysWin(){
		return alwaysWin;
	}
}
