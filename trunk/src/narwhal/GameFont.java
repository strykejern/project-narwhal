package narwhal;

import gameEngine.Log;
import gameEngine.ResourceMananger;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class GameFont {
	private static Font menuFont;
	private static Font normalFont;
	private static boolean initialized = false;
	
	public static enum FontType {
		FONT_MENU,
		FONT_NORMAL
	}
	
	//Empty constructor
	public GameFont() {
		
	}

	//JJ> Prepares our awesome font
	private static void initializeFont() {
		try 
		{
			//Make sure the file exists
			if( !ResourceMananger.fileExists("/data/font.ttf") ) throw new Exception("Missing font file! (/data/font.ttf)");
			
			//Load our awesome font
			menuFont = Font.createFont(Font.TRUETYPE_FONT, ResourceMananger.getInputStream("/data/font.ttf") );
			menuFont = menuFont.deriveFont(Font.BOLD, 14);
		} 
		catch (Exception e) 
		{
			//Use default font instead
			Log.warning(e);
			menuFont = new Font("Arial", Font.BOLD, 14);
		}
		
		//Do the normal font as well
		normalFont = new Font("Arial", Font.BOLD, 14);
		initialized = true;
	}
	
	//JJ> Changes the font type
	public void set(Graphics2D g, FontType fnt) {
		if( !initialized ) initializeFont();
		
		//Determine font type
		switch( fnt )
		{
			case FONT_MENU: g.setFont(menuFont); return;
			default: 
			case FONT_NORMAL: g.setFont(normalFont); return;
		}
		
	}

	public int getWidth(String text, Graphics2D g) {
		FontMetrics metric = g.getFontMetrics();
		return metric.stringWidth(text);
	}
	
	public int getHeight(Graphics2D g) {
		FontMetrics metric = g.getFontMetrics();
		return metric.getHeight();
	}

}
