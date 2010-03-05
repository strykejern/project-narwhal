package gameEngine;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;

/**
 * JJ> Sound class to make using sound effects easier
 * @author Johan Jansen and Anders Eie
 *
 */
public class Sound
{
	/** The sound itself as a audio stream */
	private AudioClip audio;
	
	/** JJ> Constructor that opens an input stream  to the audio file.
	 * 
	 * @param fileName: path to the file to be loaded
	 */
	public Sound( String fileName ) {
		try
        {
        	audio = Applet.newAudioClip( new URL("file:" + fileName) ); // Load the Sound
        }
        catch(Exception e){ Log.warning( e.toString() ); } // Satisfy the catch
	}
		
	/**
	 * JJ> Play the clip once
	 */
	public void play() {
		audio.play();
	}

	/**
	 * JJ> play the clip repeatedly forever until Sound.stop() is called
	 */
	public void playLooped() {		
		audio.loop();
	}
	
	/**
	 * JJ> to stop the audio.
	 */
	public void stop() {
		audio.stop();
	}

}
