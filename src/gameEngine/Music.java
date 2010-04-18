package gameEngine;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

public abstract class Music {
	
	public static boolean musicEnabled = true;
	public static float musicVolume = 0.1f;
	private static MusicThread music;

	/**
	 * JJ> Starts looping a music track. only one music track can 
	 *     be played at the same time
	 * @param song The Sound object to be looped.
	 */
	public static void play( String song ){
		if(!musicEnabled ) return;
				
		//Stop any existing music
		stopMusic();
	
		//Set the new song
		music = new MusicThread("data/sounds/" + song);
		music.start();		
	}
	
	/**
	 * JJ> Stops playing the music
	 */
	public static void stopMusic() {
		if( music != null )
		{
			music.halt();
			music = null;
		}
	}
			
	private static class MusicThread extends Thread {
		private boolean stop;
		String song;
		
		public MusicThread(String song) {
			this.setPriority( Thread.MIN_PRIORITY );
			this.setDaemon(true);
			this.song = song;
		}

		public void halt() {
			stop = true;
		}
		
		public void run() {
			try 
			{
				//This might be do once or in infinity, depending on the loop variable
				do
				{
					//Try to open the sound
					SourceDataLine line;
					AudioInputStream stream = Sound.getAudioStream(song);
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat(), ((int) stream.getFrameLength() * stream.getFormat().getFrameSize()));
					
					//Open the line to the stream
					line = (SourceDataLine) AudioSystem.getLine(info);
					line.open( stream.getFormat() );
					line.start();

					//Set sound volume
					if(line.isControlSupported(FloatControl.Type.MASTER_GAIN))
					{
						FloatControl gainControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);	
						float gain = (float)(Math.log(musicVolume)/Math.log(10.0f)*20.0f);
						gain = Math.max(gainControl.getMinimum(), Math.min(gain, gainControl.getMaximum()));
						gainControl.setValue(gain);	
					}
					
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
					while ( !stop && (len = stream.read(audioBytes) ) != -1 ) 
					{
						line.write(audioBytes, 0, len);
					}
					
					//Done playing sound
					line.drain();
					line.stop();
					line.flush();
					line.close();
					
					//Finished with this stream
					stream.close();
				
				} while( !stop );
				
			}
			catch (Exception e) 
			{ 
				Log.warning("Error playing music: " + e);
			}
		}	
	}
}
