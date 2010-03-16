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
import org.newdawn.easyogg.OggClip;


/**
 * JJ> Sound class to make using sound effects easier
 *     Currently supports raw formats and ogg vorbis.
 * @author Johan Jansen and Anders Eie
 *
 */
public class Sound
{
	private static Sound music;
	public static void playMusic(Sound song){
		
		//Stop any existing music
		if(music != null) music.stop();
		
		//Set the new song
		music = song;
		if(song == null) return;
		
		//Play the next song
		music.setVolume( 0.75f );
		music.playLooped();
	}
	public static void stopMusic(){
		if(music != null) music.stop();
	}
	
	/** The sound itself as a audio stream */
	private OggClip ogg;
	private Clip    raw;
	
	/** JJ> Constructor that opens an input stream  to the audio file.
	 * 
	 * @param fileName: path to the file to be loaded
	 */
	public Sound( String fileName ) {
			InputStream stream = ResourceMananger.getInputStream(fileName);
		
			//First try to load it as a ogg vorbis file
			if( fileName.endsWith(".ogg") ) try 
			{
				ogg = new OggClip(stream);
   			    ogg.setGain( 0.65f );
			} 
			catch (Exception e) { Log.error( "Loading ogg file failed - " + e.toString() ); }
			
			//Nope, try to load it raw! Roar!
			else try
   			{
   				AudioInputStream audioStream = AudioSystem.getAudioInputStream( stream );
   				
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
		    setVolume(0.5f);
	}

	/**
	 * JJ> Sets the sound volume where 0.00 is no sound and 1.00 is 100% volume
	 */
	public void setVolume(float gain) {
		if( raw != null )
		{
		    FloatControl gainControl = (FloatControl)raw.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue((float)(Math.log(gain)/Math.log(10.0)*20.0));
		}
		else ogg.setGain( gain );
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
		if( raw != null )
		{
			if( raw.getMicrosecondLength() <= raw.getMicrosecondPosition() ) raw.setMicrosecondPosition(0);
			raw.start();
		}
		else if( ogg != null && ogg.stopped() ) ogg.play();
	}

	/**
	 * JJ> play the clip repeatedly forever until Sound.stop() is called
	 */
	public void playLooped() {		
		if( raw != null ) 	   raw.loop(Clip.LOOP_CONTINUOUSLY);
		else if( ogg != null ) ogg.loop();
	}
	
	/**
	 * JJ> to stop the audio.
	 */
	public void stop() {
		if( raw != null ) 	   raw.stop();
		else if( ogg == null ) ogg.stop();
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
			raw.drain();
		}
		else if( ogg == null ) 
		{
			ogg.stop();
			ogg.close();
		}
	}

}
