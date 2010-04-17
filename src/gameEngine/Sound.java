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
import java.io.InputStream;

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
	public static int channelsPlaying = 0;
	
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

	private AudioInputStream getAudioStream() throws UnsupportedAudioFileException, IOException {
		
		//Try to open a stream to it
		InputStream in = ResourceMananger.getInputStream(file);
		AudioInputStream rawstream = AudioSystem.getAudioInputStream(in);
		AudioFormat format = rawstream.getFormat();
        
		//Decode it if it is in OGG Vorbis format
		if( oggFile )
		{
			//The ogg Vorbis format
	        format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(),
                16,
                2,
                format.getChannels() * 2,
                format.getSampleRate(),
                false);
		}
		
		//Convert sound from Mono to Stereo so that we can adjust panning
		else if(format.getChannels() == 1 )
		{
	        format = new AudioFormat(
                format.getEncoding(),
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                2,
                format.getFrameSize(),
                format.getFrameRate(),
                format.isBigEndian());			
		}
					
        //Get AudioInputStream that will be decoded by underlying SPI using the specified format
        return AudioSystem.getAudioInputStream(format, rawstream);
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
		if( volume < 0.1f ) return;
		
		//Calculate if the sound is left or right oriented
		float panning = (2.00f/Video.getScreenWidth()) * -(screenCenter.x - origin.x);
		
		//Play the sound!
		play( Math.min(volume, soundVolume), panning );
	}

	/**
	 * JJ> Play the sound clip with all specified effects (volume, looping, etc.)
	 */
	protected void play( final float volume, final float panning ) {
		if( !enabled || !valid || volume == 0 ) return;

		//This sound is no longer silent
		silence = false;

		//Create a new thread for this sound to be played within
		new SoundThread(volume, panning).start();
		channelsPlaying++;
	}
	
	public class SoundThread extends Thread {
		float volume;
		float panning;
		
		public SoundThread(float volume, float panning) {
			super(file);
			this.volume = volume;
			this.panning = panning;
			this.setPriority( Thread.MIN_PRIORITY );
			this.setDaemon(true);
		}
		
		public void run() {
			try 
			{
				//This might be do once or in infinity, depending on the loop variable
				do
				{
					//Try to open the sound
					SourceDataLine line;
					AudioInputStream stream = getAudioStream();		
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat(), ((int) stream.getFrameLength() * stream.getFormat().getFrameSize()));
					
					//Open the line to the stream
					line = (SourceDataLine) AudioSystem.getLine(info);
					line.open( stream.getFormat() );
					line.start();
					mixSoundEffects(line, volume, panning );
								
					//This actually plays the sound
					int len = 0;					
					int bytesPerFrame = stream.getFormat().getFrameSize();
					  
				    // some audio formats may have unspecified frame size
				    // in that case we may read any amount of bytes
	  			    if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) bytesPerFrame = 1;
						
				    // Set an arbitrary buffer size of 1024 frames.
				    int numBytes = 1024 * bytesPerFrame; 
				    byte[] audioBytes = new byte[numBytes];
					
					//Keep playing as long as there is data left and sound has not been stopped
					while ( enabled && (len = stream.read(audioBytes) ) != -1 ) 
					{
						if( silence ) break;
						line.write(audioBytes, 0, len);
					}
					
					//Done playing sound
					line.drain();
					line.stop();
					//line.close();
					//line.flush();
					
					//Finished with this stream
					stream.close();
				
				} while( looping && !silence );
				
			}
			catch (Exception e) 
			{ 
				Log.warning("Error playing sound: " + e);
			}
			finally
			{
				//This thread should be killed now
				channelsPlaying--;
			}			
		}	
	}
	
	public static int getActiveSounds() {
		return channelsPlaying;
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
	private void mixSoundEffects(Line line, float volume, float panning) {
		
		//Clip them to some valid values
		panning = Math.max(-1, Math.min(1, panning));
		volume = Math.max(0, Math.min(1, volume));
		
		//Adjust sound balance
		if( panning != 0 && line.isControlSupported(FloatControl.Type.PAN) )
		{
			FloatControl gainControl = (FloatControl)line.getControl(FloatControl.Type.PAN);
			gainControl.setValue(panning);
		}
		
		//Set sound volume
		if(line.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			FloatControl gainControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);	
			float gain = (float)(Math.log(volume)/Math.log(10.0f)*20.0f);
			gain = Math.max(gainControl.getMinimum(), Math.min(gain, gainControl.getMaximum()));
			gainControl.setValue(gain);	
		}
	}
}
