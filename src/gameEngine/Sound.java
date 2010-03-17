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
package gameEngine;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;


/**
 * JJ> Sound class to make using sound effects easier
 *     Currently supports raw formats and ogg vorbis.
 * @author Johan Jansen and Anders Eie
 *
 */
public class Sound
{
	public static boolean enabled = false;
	private static float soundVolume = 0.75f;
	private static float musicVolume = 0.25f;
	private static Sound music;
	
	/**
	 * JJ> Starts looping a music track. only one music track can 
	 *     be played at the same time
	 * @param song The Sound object to be looped.
	 */
	public static void playMusic(Sound song){
		if(!enabled) return;
		
		//Stop any existing music
		if(music != null) music.stop();
		
		//Set the new song
		music = song;
		if(song == null) return;
		
		//Play the next song
		music.playLooped();
		music.setVolume(musicVolume);
	}
	public static void stopMusic(){
		if(music != null) music.stop();
	}
	
	/** The sound itself as a audio stream */
	private Thread audio;
	private AudioInputStream stream;
	private SourceDataLine line;
	private boolean looping = false;
	private boolean stopped = true;
	
	/** JJ> Constructor that opens an input stream to the audio file and ready all data so that it can
	 * 		be played.
	 * @param fileName Path to the file to be loaded
	 */
	public Sound( String fileName ) {
		
		//Figure out if we are loading a ogg file
		boolean oggFile = false;		
		if( fileName.endsWith(".ogg") ) oggFile = true;

		try
		{
			//First make sure the file actually exists
			URL path = ResourceMananger.getFilePath(fileName);
			if( path == null ) throw new Exception("Could not find file - " + fileName );
			
			//Try to open a stream to it
			AudioInputStream rawstream = AudioSystem.getAudioInputStream(ResourceMananger.getInputStream(fileName));
			AudioFormat baseFormat = rawstream.getFormat();
	        
			//Decode it if it is in OGG Vorbis format
			if( oggFile )
			{
				//The ogg Vorbis format
		        baseFormat = new AudioFormat(
	                AudioFormat.Encoding.PCM_SIGNED,
	                baseFormat.getSampleRate(),
	                16,
	                baseFormat.getChannels(),
	                baseFormat.getChannels() * 2,
	                baseFormat.getSampleRate(),
	                false);
			}
			
	         //Get AudioInputStream that will be decoded by underlying VorbisSPI
	        stream = AudioSystem.getAudioInputStream(baseFormat, rawstream);
	        stream.mark( Integer.MAX_VALUE );
	        
			//Open the line to the stream
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat(), ((int) stream.getFrameLength() * stream.getFormat().getFrameSize()));
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(stream.getFormat());
			line.start();

			//Set default sound volume
			setVolume(soundVolume);
   		}
	    catch (Exception e) { Log.warning( "Loading audio file failed - " + e.toString() ); }
	}

	
	/**
	 * JJ> Attempt to set the global gain (volume ish) for the play back. If the control is not supported
	 *     this method has no effect. 1.0 will set maximum gain, 0.0 minimum gain
	 * 
	 * @param gain The gain value
	 */
	public void setVolume(float gain) {
		if(line == null) return;

		//Clip the volume to between 0.00 and 1.00
		gain = Math.max(0.00f, Math.min(gain, 1.00f));
		FloatControl gainControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
	
		//Now set it
		gainControl.setValue((float)(Math.log(gain)/Math.log(10.0)*20.0));
	}

	/**
	 * JJ> Attempt to set the balance between the two speakers. -1.0 is full left speak, 1.0 if full right speaker.
	 * 	   Anywhere in between moves between the two speakers. 
	 * 
	 * @param balance The balance value
	 */
	public void setBalance(float balance) {
		if(line == null) return;
		
		//Clip the volume to between -1.00 and 1.00
		balance = Math.max(-1.00f, Math.min(balance, 1.00f));
		FloatControl gainControl = (FloatControl)line.getControl(FloatControl.Type.BALANCE);
	
		//Now set it
		gainControl.setValue(balance);
	}

	/**
	 *  JJ> Figures out if the sound is currently playing
	 */
	public boolean isPlaying() {
		return !stopped;
	}
	
	/**
	 * JJ> Play the clip once, but only if it has finished playing
	 */
	public void play() {
		if(!enabled || line == null || !stopped ) return;
		
		//Create a new thread for this sound to be played within
		audio = new Thread()
		{
			public void run()
			{
				//Try to open the sound
				try 
				{
					line.open(stream.getFormat());
				} 
				catch (Exception e) 
				{
					Log.warning("Cannot play sound: " + e);
					return;
				}
				line.start();
				stopped = false;
				
				//This might be do once or in infinity, depending on the loop variable
				do
				{
					//This actually plays the sound
					int len = 0;
					byte[] buffer = new byte[1024 * stream.getFormat().getFrameSize()];
					try 
					{
						//Keep playing as long as there is data left and sound has not been stopped
						while (!stopped && enabled && (len = stream.read(buffer, 0, buffer.length)) != -1) 
							line.write(buffer, 0, len);
					} 
					catch (Exception e) { Log.warning("Error playing sound: " + e); }
				} while(looping);
				
				//Done playing sound
				line.drain();
				line.stop();
				line.close();
				try { stream.reset(); } 
				catch (IOException e) { /*Ignore*/ }
				stopped = true;
			}
		};
		
		//Begin said thread
		audio.setDaemon(true);
		audio.start();
	}

	/**
	 * JJ> play the clip repeatedly forever until Sound.stop() is called
	 */
	public void playLooped() {		
		if(!enabled) return;
		looping = true;
		play();
	}
	
	/**
	 * JJ> to stop the audio.
	 */
	public void stop() {
		looping = false;
		stopped = true;
	}
	
	/**
	 * JJ> Disposes this Sound freeing any resources it previously used. It will flush 
	 *     any AudioStreams referenced to it as well.
	 */
	public void dispose() {
		try
		{
			audio = null;
			line.stop();
			line.close();
			line.flush();
			stream.close();	
		}
		catch( Exception e )
		{
			Log.warning("Disposing of sound: " + e);
		}
	}
	
}
