package narwhal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import narwhal.GameFont.FontType;

import gameEngine.*;
import gameEngine.GameWindow.GameState;

public class CampaignScreen {
	Image2D background;
	Button begin;
	Input key;
	String mission;
	ArrayList<String> description;
	Sound narrator;
	String nextMission;
	public boolean active;
	
	public CampaignScreen(Input key) {
		this.key = key;
		active = false;
		
		Vector pos = new Vector(Video.getScreenWidth()-100, Video.getScreenHeight()-100);
		begin = new Button(pos, new Vector(150, 100 ), "Begin", 0, pos);
		
		description = new ArrayList<String>();
	}
	
	public void loadMission(String fileName) {
		BufferedReader parse = new BufferedReader(
				new InputStreamReader(
				ResourceMananger.getInputStream(fileName)));
		
		description.clear();
		
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
				if     (line.startsWith("[MISSION]:"))    	  mission = parse(line);
				else if(line.startsWith("[DESCRIPTION]:"))
				{
					String text = parse(line);
					while( text.length() > 36 )
					{
						int c = 36;
						for(; text.charAt(c) != ' '; c--);
						description.add(text.substring(0, c).trim());
						text = text.substring(c);
					}
					description.add(text.trim());
				}
				else if(line.startsWith("[VOICE]:")) narrator = new Sound( parse(line) );
				else if(line.startsWith("[MUSIC]:")) Music.play( parse(line ) );
				else if(line.startsWith("[IMAGE]:"))
				{
					background = new Image2D(parse(line));
					background.resize(Video.getScreenWidth(), Video.getScreenHeight());
				}
				else if(line.startsWith("[NEXT]:")) nextMission = parse(line);
				
				//Could not figure it out
				else	Log.warning("Loading mission " + fileName + " - unrecognized line: " + line);
			}
		} 
		catch (IOException e) 
		{
			Log.warning("Loading mission " + fileName + " - " + e);
		}
		
		narrator.playFull(0.75f);
		active = true;
	}
	
	public void draw(Graphics2D g){
		if(mission == null) return;
		
		background.draw(g, 0, 0);
		begin.draw(g);
		
		int size = Video.getScreenWidth() / (Video.getScreenWidth()/60);
		GameFont.set(g, FontType.FONT_MENU, Color.YELLOW, size);
		int y = GameFont.getHeight(g);
		
		g.drawString(mission, 20, y);
		y += GameFont.getHeight(g) + 20;
		
		for( String text : description )
		{
			size = Video.getScreenWidth() / (Video.getScreenWidth()/30);
			GameFont.set(g, FontType.FONT_DESCRIBE, Color.YELLOW, size);
			g.drawString(text, 20, y);
			y += GameFont.getHeight(g);
		}
	}

	public GameState update() {
		begin.update();

		//The next button
		if( begin.mouseOver(key) && key.mosButton1 ) 
		{
			key.mosButton1 = false;
			narrator.silence();						//Shut up that voice
			return GameState.GAME_START_CAMPAIGN;
		}

		return GameState.GAME_CAMPAIGN_SCREEN;
	}
	
	public void next(){
		loadMission(nextMission);
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
}
