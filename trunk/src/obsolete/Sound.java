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

import java.io.InputStream;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;


/**
 * JJ> Sound class to make using sound effects easier
 *     Currently supports raw formats and ogg vorbis.
 * @author Johan Jansen and Anders Eie
 *
 */
public class Sound
{
	public static boolean enabled = true;
	private static float soundVolume = 0.75f;
	private static float musicVolume = 0.50f;
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
	private OggClip ogg = null;
	private Clip    raw = null;
	
	/** JJ> Constructor that opens an input stream  to the audio file.
	 * 
	 * @param fileName: path to the file to be loaded
	 */
	public Sound( String fileName ) {
		
		//First try to load it as a ogg vorbis file
/*		if( fileName.endsWith(".ogg") ) try 
		{
			InputStream stream = ResourceMananger.getInputStream(fileName);
			ogg = new OggClip(stream);			
		} 
		catch (Exception e) { Log.error( "Loading ogg file failed - " + e.toString() ); }
		
		//Nope, try to load it raw! Roar!
		else */try
		{
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(
					ResourceMananger.getFilePath(fileName) );

		    // At present, ALAW and ULAW encodings must be converted
		    // to PCM_SIGNED before it can be played
		    AudioFormat format = audioStream.getFormat();
		    if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
		        format = new AudioFormat(
		                AudioFormat.Encoding.PCM_SIGNED,
		                format.getSampleRate(),
		                format.getSampleSizeInBits()*2,
		                format.getChannels(),
		                format.getFrameSize()*2,
		                format.getFrameRate(),
		                true);        // big endian
		        audioStream = AudioSystem.getAudioInputStream(format, audioStream);
		    }

		    // Create the clip
		    DataLine.Info info = new DataLine.Info(
		        Clip.class, audioStream.getFormat(), ((int)audioStream.getFrameLength()*format.getFrameSize()));
		    raw = (Clip) AudioSystem.getLine(info);

		    // This method does not return until the audio file is completely loaded
		    raw.open(audioStream);
   		}
	    catch (Exception e) { Log.warning( "Loading audio file failed - " + e.toString() ); }
	    
	    //Set to default sound volume
	    setVolume(soundVolume);
	}

	/**
	 * JJ> Attempt to set the global gain (volume ish) for the play back. If the control is not supported
	 *     this method has no effect. 1.0 will set maximum gain, 0.0 minimum gain
	 * 
	 * @param gain The gain value
	 */
	public void setVolume(float gain) {

		//Clip the volume to between 0.00 and 1.00
		gain = Math.max(0.00f, Math.min(gain, 1.00f));
		FloatControl gainControl = null;
				
		//Get the correct controller (ogg or raw)
		if( raw != null )    gainControl = (FloatControl)raw.getControl(FloatControl.Type.MASTER_GAIN);
		else if(ogg != null ) gainControl = (FloatControl)ogg.getControl(FloatControl.Type.MASTER_GAIN);
		
		//Now set it
		if( gainControl != null ) gainControl.setValue((float)(Math.log(gain)/Math.log(10.0)*20.0));
	}

	/**
	 * JJ> Attempt to set the balance between the two speakers. -1.0 is full left speak, 1.0 if full right speaker.
	 * 	   Anywhere in between moves between the two speakers. 
	 * 
	 * @param balance The balance value
	 */
	public void setBalance(float balance) {
		
		//Clip the volume to between -1.00 and 1.00
		balance = Math.max(-1.00f, Math.min(balance, 1.00f));
		FloatControl gainControl = null;
		
		//Get the correct controller (ogg or raw)
		if( raw != null )    gainControl = (FloatControl)raw.getControl(FloatControl.Type.BALANCE);
		else if(ogg != null) gainControl = (FloatControl)ogg.getControl(FloatControl.Type.BALANCE);
		
		//Now set it
		if(gainControl != null) gainControl.setValue(balance);
	}

	/**
	 *  JJ> Figures out if the sound is currently playing
	 */
	public boolean isPlaying() {
		if( raw != null ) 	   return raw.isRunning();
		else if( ogg != null ) return !ogg.stopped();
		return false;
	}
	
	/**
	 * JJ> Play the clip once, but only if it has finished playing
	 */
	public void play() {
		if(!enabled) return;
		
		if( raw != null )
		{
			//Reset position
			if( raw.getMicrosecondLength() <= raw.getMicrosecondPosition() ) raw.setMicrosecondPosition(0);
			raw.start();			
		}
		else if( ogg != null && ogg.stopped() ) ogg.play();
	}

	/**
	 * JJ> play the clip repeatedly forever until Sound.stop() is called
	 */
	public void playLooped() {		
		if(!enabled) return;
		
		if( raw != null ) 	   raw.loop(Clip.LOOP_CONTINUOUSLY);
		else if( ogg != null ) ogg.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	/**
	 * JJ> to stop the audio.
	 */
	public void stop() {
		if( raw != null ) 	   raw.stop();
		else if( ogg != null ) ogg.stop();
	}
	
	/**
	 * JJ> Disposes this Sound freeing any resources it previously used. It will flush 
	 *     any AudioStreams referenced to it as well.
	 */
	public void dispose()
	{
		if( raw != null ) 	   
		{
			raw.stop();
			raw.close();
			raw.flush();
		}
		else if( ogg == null ) 
		{
			ogg.stop();
			ogg.close();
		}
	}
	
	/**
	 * Simple Clip player for OGG's. 
	 * @author kevin, heavily modified by Johan Jansen
	 */
	class OggClip {
		private BufferedInputStream bitStream;
		Thread player;
		private JOrbisPlayer oggPlay;
		
		/**
		 * Create a new clip based on a reference into the class path
		 * 
		 * @param in The stream from which the ogg can be read from
		 * @throws IOException Indicated a failure to read from the stream
		 */
		public OggClip(InputStream in) throws IOException {
			if (in == null) throw new IOException("Couldn't find input source.");
			
			//Convert the input stream to a bit stream
			bitStream = new BufferedInputStream(in);
			bitStream.mark(Integer.MAX_VALUE);
			
			//Init our player
			oggPlay = new JOrbisPlayer( bitStream );
			try 
			{
				oggPlay.playStream(this, Thread.currentThread());
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		public Control getControl(FloatControl.Type type) {
			
			//We need a stream of sound to get control over
			SourceDataLine line = oggPlay.getOutputLine();
			if(line == null) return null;
			
			return line.getControl(type);
		}
				
		/**
		 * Check if the clip has been stopped
		 * 
		 * @return True if the clip has been stopped
		 */
		public boolean stopped() {
			return ( (player == null) || ( !player.isAlive() ) );
		}
					
		/**
		 * JJ> Play the clip once
		 */
		public void play() { 
			loop(1);
		}
		
		/**
		 * JJ> Loop clip
		 * @param loop How many times
		 */
		public void loop(final int loop) {
			
			//Stop any existing stream
			stop();
			
			try 
			{
				bitStream.reset();
			} 
			catch (IOException e) 
			{
				// ignore if no mark
			}			
			
			//Create a new seperate thread to play sound
			final OggClip ogg = this;
			player = new Thread() 
			{
				public void run() 
				{
					int loopCount = 0;
					while (player == Thread.currentThread()) 
					{
						//Stop looping if we reached the correct amount
						if(loop != Clip.LOOP_CONTINUOUSLY)
						{
							if(loop < loopCount) break;
							loopCount++;
						}
						
						//Play the sound!
						try 
						{
							oggPlay.playStream(ogg, Thread.currentThread());
							bitStream.reset();
						} 
						catch (Exception e) 
						{
							Log.warning("Play ogg sound - " + e);
							player = null;
							break;
						}							
					}
				}
			};
			
			player.setDaemon(true);
			player.start();
		}
		
		/**
		 * Stop the clip playing
		 */
		public void stop() 
		{
			if ( stopped() ) return;
			
			player = null;
		}
		
		/**
		 * Close the stream being played from
		 */
		public void close() 
		{
			try 
			{
				if (bitStream != null) bitStream.close();
			} 
			catch (IOException e) 
			{
				Log.warning("Could not close ogg bitstream: " + e);
			}
		}
	}
}
