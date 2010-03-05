package gameEngine;
import  sun.audio.*;    //import the sun.audio package

import  java.io.*;

/**
 * JJ> Sound class to make using sound effects easier
 * @author Johan Jansen and Anders Eie
 *
 */
public class Sound
{
	/** The sound itself as a audio stream */
	private AudioStream audio;			
	
	/** When it last started playing */
	private long progress;
	
	/** JJ> Constructor that opens an input stream  to the audio file.
	 * 
	 * @param fileName: path to the file to be loaded
	 */
	public Sound( String fileName ) {
		try 
		{
			// Create an AudioStream object from the input stream.
			audio = new AudioStream( new FileInputStream(fileName) );
		} 
		catch (FileNotFoundException e) {
			Log.warning(e.toString());
		}
		catch (IOException e) {
			Log.warning(e.toString());
		}
	}
		
	/**
	 * JJ> Use the static class member "player" from class AudioPlayer to play
	 *     the clip
	 */
	public void play() {		
		AudioPlayer.player.start(audio);
		progress = System.currentTimeMillis();
	}
	
	/**
	 * JJ> to stop the audio.
	 */
	public void stop() {
		AudioPlayer.player.stop(audio);
		progress = 0;
	}
	
	/**
	 * JJ> Returns True if the sound file is currently being played. False otherwise.
	 */
	public boolean isPlaying(){
		return progress + audio.getLength() > System.currentTimeMillis();
	}
	
	/**
	 * JJ> Gets the length of this sound
	 * @return the length of this AudioStream in milliseconds
	 */
	public int getLength(){
		return audio.getLength();
	}

}
