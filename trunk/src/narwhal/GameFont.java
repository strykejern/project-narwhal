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

import gameEngine.Log;
import gameEngine.ResourceMananger;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public abstract class GameFont {
	private static Font menuFont, creepyFont, normalFont, descriptionFont, crystalFont;
	private static boolean initialized = false;
	
	public static enum FontType {
		FONT_MENU,
		FONT_CREEPY,
		FONT_DESCRIBE,
		FONT_NORMAL,
		FONT_CRYSTAL,
	}
	
	//JJ> Prepares our awesome font
	private static void initializeFont() {

		//Load special fonts
		menuFont = loadFont("menu.ttf");
		creepyFont = loadFont("creepy.ttf");
		descriptionFont = loadFont("description.ttf");
		crystalFont = loadFont("crystal.ttf");
		
		//Do the normal font as well
		normalFont = new Font("Arial", Font.BOLD, 14);
		initialized = true;
	}
	
	private static Font loadFont( String name ){
		name = "/data/" + name;
		Font retval;
		try
		{
			//Make sure the file exists
			if( !ResourceMananger.fileExists(name) ) throw new Exception("Missing font file! (" + name + ")");
			retval = Font.createFont(Font.TRUETYPE_FONT, ResourceMananger.getInputStream(name) );
		} 
		catch (Exception e) 
		{
			//Use default font instead
			Log.warning(e);
			retval = new Font("Arial", Font.BOLD, 14);
		}
		
		return retval;
	}
	
	public static void setSize(int size) {
		menuFont = menuFont.deriveFont(Font.BOLD, size);
	}
	
	//JJ> Changes the font type
	public static void set(Graphics2D g, FontType fnt, Color color, int size) {
		if( !initialized ) initializeFont();
		g.setColor(color);
		
		//Determine font type
		switch( fnt )
		{
			case FONT_MENU: 	g.setFont(menuFont.deriveFont(Font.BOLD, size)); return;
			case FONT_DESCRIBE: g.setFont( descriptionFont.deriveFont(Font.BOLD, size) ); return;
			case FONT_CREEPY:   g.setFont( creepyFont.deriveFont(Font.PLAIN, size) ); return;
			case FONT_CRYSTAL:  g.setFont( crystalFont.deriveFont(Font.PLAIN, size) ); return;
			
			default: 
			case FONT_NORMAL:   g.setFont( normalFont.deriveFont(Font.BOLD, size) ); 			
		}
		
	}

	public static int getWidth(String text, Graphics2D g) {
		FontMetrics metric = g.getFontMetrics();
		return metric.stringWidth(text);
	}
	
	public static int getHeight(Graphics2D g) {
		FontMetrics metric = g.getFontMetrics();
		return metric.getHeight();
	}

}
