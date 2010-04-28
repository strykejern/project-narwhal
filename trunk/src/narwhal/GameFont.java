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
	private static Font menuFont;
	private static Font normalFont;
	private static Font descriptionFont;
	private static boolean initialized = false;
	
	public static enum FontType {
		FONT_MENU,
		FONT_DESCRIBE,
		FONT_NORMAL
	}
	
	//JJ> Prepares our awesome font
	private static void initializeFont() {
		try 
		{
			//Make sure the file exists
			if( !ResourceMananger.fileExists("/data/menu.ttf") ) throw new Exception("Missing font file! (/data/menu.ttf)");
			
			//Load our awesome font
			menuFont = Font.createFont(Font.TRUETYPE_FONT, ResourceMananger.getInputStream("/data/menu.ttf") );
		} 
		catch (Exception e) 
		{
			//Use default font instead
			Log.warning(e);
			menuFont = new Font("Arial", Font.BOLD, 14);
		}

		try 
		{
			//Make sure the file exists
			if( !ResourceMananger.fileExists("/data/description.ttf") ) throw new Exception("Missing font file! (/data/description.ttf)");
			
			descriptionFont = Font.createFont(Font.TRUETYPE_FONT, ResourceMananger.getInputStream("/data/description.ttf") );
		} 
		catch (Exception e) 
		{
			//Use default font instead
			Log.warning(e);
			descriptionFont = new Font("Arial", Font.BOLD, 14);
		}

		//Do the normal font as well
		normalFont = new Font("Arial", Font.BOLD, 14);
		initialized = true;
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
			case FONT_MENU: 
				{
					menuFont = menuFont.deriveFont(Font.BOLD, size);
					g.setFont(menuFont); return;
				}
			default: 
			case FONT_NORMAL: 
				{
					normalFont = normalFont.deriveFont(Font.BOLD, size);
					g.setFont(normalFont); 
					return;
				}
			case FONT_DESCRIBE: 
			{
				descriptionFont = descriptionFont.deriveFont(Font.BOLD, size);
				g.setFont(descriptionFont); 
				return;
			}
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
