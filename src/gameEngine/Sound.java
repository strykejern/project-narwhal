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

import java.util.HashMap;

import javax.sound.sampled.*;


/**
 * JJ> Sound class to make using sound effects easier
 *     Currently supports raw formats and ogg vorbis.
 * @author Johan Jansen and Anders Eie
 *
 */
public class Sound
{
	//Global settings
	public static boolean enabled = true;
	private static float soundVolume = 0.75f;
	private static HashMap<String, Sound> soundList;
	
	/**
	 * JJ> Loads all sounds into memory, done only once to save time and memory
	 */
	private static void loadAllSounds() {
		soundList = new HashMap<String, Sound>();
		String[] fileList = ResourceMananger.getFileList("/data/sounds/");
		for(String file : fileList) 
		{
			soundList.put(file.substring(file.lastIndexOf('/')+1), new Sound(file) );
		}
	}	

	/**
	 * JJ> This gets the specified sound that is loaded globally into memory
	 *     Sounds loaded this way are only loaded once in memory and thus saves both
	 *     memory and processing time.
	 * @param fileName Which sound to get
	 * @return The Sound if it was found or null otherwise.
	 */
	public static Sound loadSound( String fileName ) {

		//First make sure sounds are loaded to memory
		if( soundList == null ) loadAllSounds();
		
		//First make sure the file actually exists
		if( soundList.get(fileName) == null )
		{
			Log.warning("Could not find file - " + fileName );
			return null;
		}
				
		return soundList.get(fileName);
	}

	
	/** The sound itself as a audio stream */
	private AudioInputStream stream;
	private boolean looping = false;
	private boolean playing = true;
	private float balance;
	private float volume;
	
	/** JJ> Constructor that opens an input stream to the audio file and ready all data so that it can
	 * 		be played.
	 * @param fileName Path to the file to be loaded
	 */
	private Sound( String fileName ) {
		Log.message("Loading sound - " + fileName);
		//Figure out if we are loading a ogg file
		boolean oggFile = false;		
		if( fileName.endsWith(".ogg") ) oggFile = true;

		//Load the sound
		try
		{			
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
	        stream.mark( Integer.MAX_VALUE );						//Mark it so it can be reset
	        
			//Set default sound volume
			setVolume(1.00f);
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
		//Clip the volume to between 0.00 and 1.00
		volume = Math.max(0.00f, Math.min(gain, 1.00f));
	}

	/**
	 * JJ> Attempt to set the balance between the two speakers. -1.0 is full left speak, 1.0 if full right speaker.
	 * 	   Anywhere in between moves between the two speakers. 
	 * 
	 * @param balance The balance value
	 */
	public void setBalance(float balance) {
		//Clip the balance to between -1.00 and 1.00
		this.balance = Math.max(-1.00f, Math.min(balance, 1.00f));
	}

	/**
	 *  JJ> Figures out if the sound is currently playing
	 */
	public boolean isPlaying() {
		return playing;
	}
	
	/**
	 * JJ> Play the clip once, but only if it has finished playing
	 */
	public void play() {
		if( !enabled || stream == null ) return;

		//Create a new thread for this sound to be played within
		Thread channel = new Thread()
		{
			public void run()
			{
				
				//This might be do once or in infinity, depending on the loop variable
				do
				{
					//Try to open the sound
					SourceDataLine line;
					try 
					{
						//Reset position to the start first
						if ( stream.markSupported() )
						stream.reset();
						
						//Open the line to the stream
						DataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat(), ((int) stream.getFrameLength() * stream.getFormat().getFrameSize()));
						line = (SourceDataLine) AudioSystem.getLine(info);
						line.open(stream.getFormat());
						line.start();
						mixSoundEffects(line);
					} 
					catch (Exception e) 
					{
						Log.warning("Cannot play sound: " + e);
						return;
					}
									
					//This actually plays the sound
					try 
					{
						playing = true;
						int len = 0;
						byte[] buffer = new byte[1024 * stream.getFormat().getFrameSize()];
						//Keep playing as long as there is data left and sound has not been stopped
						while ( playing && enabled && (len = stream.read(buffer, 0, buffer.length)) != -1 ) 
							line.write(buffer, 0, len);
					} 
					catch (Exception e) { Log.warning("Error playing sound: " + e); }
					
					//Done playing sound
					line.drain();
					line.stop();
					line.close();
					line.flush();
				} while( looping );
				
				playing = false;
			}
		};
		
		//Begin said thread
		channel.setPriority(Thread.MIN_PRIORITY);
		channel.setDaemon(true);
		channel.start();
	}

	/**
	 * JJ> play the clip repeatedly forever until Sound.stop() is called
	 */
	public void playLooped() {		
		if( !enabled ) return;
		looping = true;
		play();
	}
	
	/**
	 * JJ> to stop the audio.
	 */
	public void stop() {
		looping = false;
		playing = false;
	}
	
	/**
	 * JJ> Disposes this Sound freeing any resources it previously used. It will flush 
	 *     any AudioStreams referenced to it as well.
	 */
	/*public void dispose() {
		try
		{
			this.stop();
			stream.close();	
		}
		catch( Exception e )
		{
			Log.warning("Disposing of sound: " + e);
		}
	}*/
	
	/**
	 * JJ> This adds sound mixer effects like volume and sound balance to a audio line
	 * @param line Which audio line to adjust
	 */
	private void mixSoundEffects(SourceDataLine line) {
		
		//Adjust sound balance
		if( balance != 0 )
		{
			try
			{
				FloatControl gainControl = (FloatControl)line.getControl(FloatControl.Type.BALANCE);
				gainControl.setValue(balance);
			}
			catch (IllegalArgumentException e) {/*Ignore: we cant change the balance*/};
		}
		
		//Set sound volume
		try
		{
			FloatControl gainControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);	
			float gain = (float)(Math.log(volume*soundVolume)/Math.log(10.0f)*20.0f);
			gain = Math.max(0.00f, Math.min(gain, 1.00f));
			gainControl.setValue(gain);
		}
		catch (IllegalArgumentException e) {/*Ignore: we cant change the volume*/};
	}
}
