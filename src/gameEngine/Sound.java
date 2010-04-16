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
	protected static float soundVolume = 0.5f;
		
	/**
	 * JJ> Attempt to set the global gain (volume ish) for the all sound effects. If the control is not supported
	 *     this method has no effect.
	 * 
	 * @param gain The gain value, 1.0 will set maximum gain, 0.0 minimum gain
	 */
	public static void setVolume(float gain) {
		//Clip the volume to between 0.00 and 1.00
		soundVolume = Math.max(0.00f, Math.min(gain, 1.00f));
	}
	
	/** The sound itself as a audio stream */
	private boolean looping = false;
	private boolean silence = false;
	protected boolean valid = false;
	protected boolean oggFile = false;
	protected String file;

	/** JJ> Constructor that opens an input stream to the audio file and ready all data so that it can
	 * 		be played.
	 * @param fileName Path to the file to be loaded
	 */
	public Sound( String fileName ) {
		
		file = "/data/sounds/" + fileName;
		if( !ResourceMananger.fileExists( file ) )
		{
			Log.warning("Sound file does not exist: " + file);
			valid = false;
		}
		else
		{
			//Figure out if we are loading a ogg file
			if( fileName.endsWith(".ogg") ) oggFile = true;		
			valid = true;
		}
	}

	private Mixer getMixer() {
		
		//This gets the proper Mixer system that supports audio panning
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		
		/*for( Mixer.Info mix : mixerInfo)
		{
			Log.message(mix.getName() + ": " + mix.getDescription());
		}*/
		return AudioSystem.getMixer(mixerInfo[0]);
	}	

	private AudioInputStream getAudioStream() {
		
		//Load the sound
		try
		{
			//Try to open a stream to it
			AudioInputStream rawstream = AudioSystem.getAudioInputStream(ResourceMananger.getInputStream(file));
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
	        return AudioSystem.getAudioInputStream(baseFormat, rawstream);
		}
	    catch (Exception e) { Log.warning( "Loading audio file failed - " + e.toString() ); }
	    return null;
	}
		
	/**
	 * JJ> Play the sound clip with full default volume and centered balance
	 */
	public void playFull( float volume ) {
		play(soundVolume*volume, 0);
	}

	/**
	 * JJ> Plays a sound and adjust volume and panning depending on the sound origin
	 *     position relative to the camera position.
	 */
	public void play3D(Vector origin, Vector cameraPos) {
		
		if( !enabled || !valid ) return;
		
		Vector screenCenter = cameraPos.plus( new Vector(Video.getScreenWidth()/2, Video.getScreenHeight()/2) );
		float maxDist = (Video.getScreenWidth() + Video.getScreenHeight())/8;
		float dist = origin.minus(screenCenter).length();

		//Calculate how loud the sound is
		float volume = ( maxDist / dist ) * soundVolume;
				
		//Too far way to hear sound?
		if( volume < 0.075f ) return;
		
		//Calculate if the sound is left or right oriented
		float panning = (2.00f/Video.getScreenWidth()) * -(screenCenter.x - origin.x);
		
		//Play the sound!
		play( Math.min(volume, soundVolume), panning );
	}

	/**
	 * JJ> Play the sound clip with all specified effects (volume, looping, etc.)
	 */
	protected void play( float volume, float panning ) {
		if( !enabled || !valid || volume == 0 ) return;
				
		//This sound is no longer silent
		silence = false;

		//Calculate sound
		final float fVolume = volume;
		final float fPanning = panning;

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
					AudioInputStream stream = getAudioStream();
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat(), ((int) stream.getFrameLength() * stream.getFormat().getFrameSize()));
					try 
					{
						//Open the line to the stream
						if( getMixer().isLineSupported(info) ) line = (SourceDataLine) getMixer().getLine(info);
						else 				 			  	   line = (SourceDataLine) AudioSystem.getLine(info);
						
						line.open( stream.getFormat() );
						line.start();
						mixSoundEffects(line, fVolume, fPanning );
									
						//This actually plays the sound
						int len = 0;
						byte[] buffer = new byte[1024 * stream.getFormat().getFrameSize()];
						
						//Keep playing as long as there is data left and sound has not been stopped
						while ( enabled && (len = stream.read(buffer, 0, buffer.length)) != -1 ) 
						{
							if( silence ) return;
							line.write(buffer, 0, len);
						}
					
						//Done playing sound
						line.drain();
						line.stop();
						line.close();
						line.flush();
						
						//Finished with this stream
						stream.close();
					} 
					catch (Exception e) 
					{ 
						Log.warning("Error playing sound: " + e); 
						return;
					}
					
				} while( looping );
				
			}
		};
		
		//Begin said thread
		channel.setPriority( Thread.MIN_PRIORITY );
		channel.setDaemon(true);
		channel.start();		
	}

	/**
	 * JJ> play the clip repeatedly forever until Sound.stop() is called
	 */
	protected void playLooped(float volume, float panning) {		
		if( !enabled ) return;
		looping = true;
		play(volume, panning);
	}
	
	/**
	 * JJ> Stops all instances of this sound
	 */
	protected void silence() {
		looping = false;
		silence = true;
	}
		
	/**
	 * JJ> This adds sound mixer effects like volume and sound balance to a audio line
	 * @param line Which audio line to adjust
	 */
	private void mixSoundEffects(SourceDataLine line, float volume, float panning) {
		
		//Clip them to some valid values
		panning = Math.max(-1, Math.min(1, panning));
		volume = Math.max(0, Math.min(1, volume));
		
		//Adjust sound balance
		if( panning != 0 )
		{
			try
			{
				FloatControl gainControl = (FloatControl)line.getControl(FloatControl.Type.PAN);
				gainControl.setValue(panning);
			}
			catch (IllegalArgumentException e) { /*Ignore: we cant change the balance*/};
		}
		
		//Set sound volume
		try
		{
			FloatControl gainControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);	
			float gain = (float)(Math.log(volume)/Math.log(10.0f)*20.0f);
			gain = Math.max(gainControl.getMinimum(), Math.min(gain, gainControl.getMaximum()));
			gainControl.setValue(gain);	
		}
		catch (IllegalArgumentException e) {/*Ignore: we cant change the volume*/ };
	}
}
