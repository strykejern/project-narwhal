package gameEngine;
import  sun.audio.*;    //import the sun.audio package

import  java.io.*;

//JJ> Sound class to make using sound effects easier
public class Sound
{
	private AudioStream audio;
	
	//JJ> Constructor that opens an input stream  to the audio file.
	public Sound( String fileName ) {
		try 
		{
			// Create an AudioStream object from the input stream.
			audio = new AudioStream(new FileInputStream(fileName));
		} 
		catch (FileNotFoundException e) {
			Log.warning(e.toString());
		}
		catch (IOException e) {
			Log.warning(e.toString());
		}
	}
		
	//JJ> Use the static class member "player" from class AudioPlayer to play
	// clip.
	public void play() {		
		AudioPlayer.player.start(audio);
	}
	
	//JJ> to stop the audio.
	public void stop() {
		AudioPlayer.player.stop(audio); 			
	}

}
