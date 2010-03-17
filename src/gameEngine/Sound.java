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
import com.jcraft.jogg.*;
import com.jcraft.jorbis.*;

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
	private static boolean soundEnabled = false;
	private static float soundVolume = 0.75f;
	private static float musicVolume = 0.5f;
	private static Sound music;
	
	/**
	 * JJ> Starts looping a music track. only one music track can 
	 *     be played at the same time
	 * @param song The Sound object to be looped.
	 */
	public static void playMusic(Sound song){
		if(!soundEnabled) return;
		
		//Stop any existing music
		if(music != null) music.stop();
		
		//Set the new song
		music = song;
		if(song == null) return;
		
		//Play the next song
		music.setVolume( musicVolume );
		music.playLooped();
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
		if( fileName.endsWith(".ogg") ) try 
		{
			InputStream stream = ResourceMananger.getInputStream(fileName);
			ogg = new OggClip(stream);
		} 
		catch (Exception e) { Log.error( "Loading ogg file failed - " + e.toString() ); }
		
		//Nope, try to load it raw! Roar!
		else try
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
	 * JJ> Sets the sound volume where 0.00 is no sound and 1.00 is 100% volume
	 */
	public void setVolume(float gain) {
		if( raw != null )
		{
			//Clip the volume to between 0.00 and 1.00
			gain = Math.max(0.00f, Math.min(gain, 1.00f));
			
		    FloatControl gainControl = (FloatControl)raw.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue((float)(Math.log(gain)/Math.log(10.0)*20.0));
		}
		else ogg.setGain( gain );
	}

	/**
	 * JJ> Attempt to set the balance between the two speakers. -1.0 is full left speak, 1.0 if full right speaker.
	 * 	   Anywhere in between moves between the two speakers. 
	 * 
	 * @param balance The balance value
	 */
	public void setBalance(float balance) {
		if( raw != null )
		{
			//Clip the volume to between -1.00 and 1.00
			balance = Math.max(-1.00f, Math.min(balance, 1.00f));
			
		    FloatControl gainControl = (FloatControl)raw.getControl(FloatControl.Type.BALANCE);
			gainControl.setValue(balance);
		}
		else ogg.setBalance( balance );
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
		if(!soundEnabled) return;
		
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
		if(!soundEnabled) return;
		
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
		}
		else if( ogg == null ) 
		{
			ogg.stop();
			ogg.close();
		}
	}
	
	/**
	 * Simple Clip like player for OGG's. Code is mostly taken from the example provided with 
	 * JOrbis.
	 * 
	 * @author kevin, heavily modified by Johan Jansen
	 */
	class OggClip {
		private SourceDataLine outputLine;
		private BufferedInputStream bitStream;
		Thread player;
		private OggPlayer oggPlay;
		
		private float balance;
		private float gain;
		private boolean paused;
		private float oldGain;
		
		/**
		 * Create a new clip based on a reference into the class path
		 * 
		 * @param ref The reference into the class path which the ogg can be read from
		 * @throws IOException Indicated a failure to find the resource
		 */
		public OggClip(String ref) throws IOException {
			try 
			{
				init(Thread.currentThread().getContextClassLoader().getResourceAsStream(ref));
			} 
			catch (IOException e) 
			{
				throw new IOException("Couldn't find: " + ref);
			}
		}

		/**
		 * Create a new clip based on a reference into the class path
		 * 
		 * @param in The stream from which the ogg can be read from
		 * @throws IOException Indicated a failure to read from the stream
		 */
		public OggClip(InputStream in) throws IOException {
			init(in);
		}
		
		/**
		 * Initialise the ogg clip
		 * 
		 * @param in The stream we're going to read from
		 * @throws IOException Indicates a failure to read from the stream
		 */
		private void init(InputStream in) throws IOException {
			if (in == null) throw new IOException("Couldn't find input source.");
			bitStream = new BufferedInputStream(in);
			bitStream.mark(Integer.MAX_VALUE);
			
			//Set defaults
			gain = 1;
			balance = 0;
			oggPlay = new OggPlayer();
		}
			
		/**
		 * Attempt to set the global gain (volume ish) for the play back. If the control is not supported
		 * this method has no effect. 1.0 will set maximum gain, 0.0 minimum gain
		 * 
		 * @param gain The gain value
		 */
		public void setGain(float gain) 
		{				
			//Clip it to a valid value
			this.gain = Math.min(1, Math.max(0, gain) );
			
			//We need something to play to
			if (outputLine == null) return;
			
		    FloatControl gainControl = (FloatControl)outputLine.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue((float)(Math.log(gain)/Math.log(10.0)*20.0));
		}
					
		/**
		 * Attempt to set the balance between the two speakers. -1.0 is full left speak, 1.0 if full right speaker.
		 * Anywhere in between moves between the two speakers. 
		 * 
		 * @param balance The balance value
		 */
		public void setBalance(float balance) {
			
			//Clip it to a valid value
			this.balance = Math.min(1.00f, Math.max(-1.00f, balance));
			
			//We need something to play to			
			if (outputLine == null) return;

			//now set the balance
			FloatControl control = (FloatControl) outputLine.getControl(FloatControl.Type.BALANCE);
			control.setValue(this.balance);
		}
			
		/**
		 * Check the state of the play back
		 * 
		 * @return True if the playback has been stopped
		 */
		boolean checkState() {
			while (paused && (player != null)) 
			{
				synchronized (player) 
				{
					if (player != null) try 
					{
						player.wait();
					} catch (InterruptedException e) 
					{
						// ignored
					}
				}
			}				
			return stopped();
		}
		
		/**
		 * Pause the play back
		 */
		public void pause() {
			paused = true;
			oldGain = gain;
			setGain(0);
		}
		
		/**
		 * Check if the stream is paused
		 * 
		 * @return True if the stream is paused
		 */
		public boolean isPaused() {
			return paused;
		}
		
		/**
		 * Resume the play back
		 */
		public void resume() {
			if (!paused) {
				play();
				return;
			}
			
			paused = false;
			
			synchronized (player) 
			{
				if (player != null) player.notify();
			}
			setGain(oldGain);
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
		 * Play the clip once
		 */
		public void play() {
			stop();
			
			try 
			{
				bitStream.reset();
			} 
			catch (IOException e) 
			{
				// ignore if no mark
			}
			
			player = new Thread() 
			{
				public void run() 
				{
					try 
					{
						oggPlay.playStream(Thread.currentThread());
						bitStream.reset();
					} 
					catch (Exception e) 
					{
						Log.warning(e);
					}	
				}
			};
			
			player.setDaemon(true);
			player.start();
		}

		/**
		 * Loop the clip - maybe for background music
		 */
		public void loop() {
			stop();
			
			try 
			{
				bitStream.reset();
			} 
			catch (IOException e) 
			{
				// ignore if no mark
			}
			
			player = new Thread() 
			{
				public void run() 
				{
					while (player == Thread.currentThread()) 
					{
						try 
						{
							oggPlay.playStream(Thread.currentThread());
							bitStream.reset();
						} 
						catch (Exception e) 
						{
							Log.warning(e);
							player = null;
						} 
					}
				};
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
			outputLine.drain();
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
			
		/****************************************************************************************
		 * TODO: move this elsewhere
		 * JJ> Everything below is taken from the JOrbis Player
		 ***************************************************************************************/
		class OggPlayer
		{
			private final int BUFSIZE = 4096 * 2;
			private int convsize = BUFSIZE * 2;
			private byte[] convbuffer = new byte[convsize];
			private SyncState oy;
			private StreamState os;
			private Page og;
			private Packet op;
			private Info vi;
			private Comment vc;
			private DspState vd;
			private Block vb;
			private byte[] buffer = null;
			private int bytes = 0;
			private int rate;
			private int channels;
	
			//JJ> Constructor
			public OggPlayer(){
				resetData();
			}
	
			private void initJavaSound(int channels, int rate) {
				try 
				{
					AudioFormat audioFormat = new AudioFormat(
							rate, 
							16,
							channels, true, // PCM_Signed
							false // littleEndian
					);
					DataLine.Info info = new DataLine.Info(SourceDataLine.class,
							audioFormat, AudioSystem.NOT_SPECIFIED);
					
					//Make sure the AudioLine is supported
					if (!AudioSystem.isLineSupported(info))  throw new Exception("Line " + info + " not supported.");
	
					try 
					{
						outputLine = (SourceDataLine) AudioSystem.getLine(info);
						outputLine.open(audioFormat);
					} 
					catch (LineUnavailableException ex) 
					{
						throw new Exception("Unable to open the sourceDataLine: " + ex);
					} 
					catch (IllegalArgumentException ex) 
					{
						throw new Exception("Illegal Argument: " + ex);
					}
	
					this.rate = rate;
					this.channels = channels;
					
					setBalance(balance);
					setGain(gain);
				} 
				catch (Exception ee) 
				{
					System.out.println(ee);
				}
			}
	
			private SourceDataLine getOutputLine(int channels, int rate) 
			{
				if (outputLine == null || this.rate != rate
						|| this.channels != channels) 
				{
					if (outputLine != null) 
					{
						outputLine.drain();
						outputLine.stop();
						outputLine.close();
					}
					initJavaSound(channels, rate);
					outputLine.start();
				}
				return outputLine;
			}
			
			//JJ> Resets the data for this player
			private void resetData(){
			    oy = new SyncState();
			    os = new StreamState();
			    og = new Page();
			    op = new Packet();
			  
			    vi = new Info();
			    vc = new Comment();
			    vd = new DspState();
			    vb = new Block(vd);
			  
			    buffer = null;
			    bytes = 0;
	
			    oy.init();
			}
	
			public void playStream(Thread me) throws Exception {
				boolean chained = false;
	
				resetData();
	
				while (true) 
				{
					if (checkState()) 
					{
						return;
					}
					
					int eos = 0;
	
					int index = oy.buffer(BUFSIZE);
					buffer = oy.data;
					try 
					{
						bytes = bitStream.read(buffer, index, BUFSIZE);
					} 
					catch (Exception e) 
					{
						Log.warning(e);
					}
					oy.wrote(bytes);
	
					if (chained) chained = false;  
					else if (oy.pageout(og) != 1) 
					{
						if (bytes < BUFSIZE) break;
						throw new Exception("Input does not appear to be an Ogg bitstream.");
					}
					os.init(og.serialno());
					os.reset();
	
					vi.init();
					vc.init();
	
					if (os.pagein(og) < 0) 
					{
						// error; stream version mismatch perhaps
						throw new Exception("Error reading first page of Ogg bitstream data.");
					}
	
					if (os.packetout(op) != 1) 
					{
						// no page? must not be vorbis
						throw new Exception("Error reading initial header packet.");
					}
	
					if (vi.synthesis_headerin(vc, op) < 0) 
					{
						// error case; not a vorbis header
						throw new Exception("This Ogg bitstream does not contain Vorbis audio data.");
					}
	
					int i = 0;
	
					while (i < 2) 
					{
						while (i < 2) 
						{
							if ( checkState() ) return;
							
							int result = oy.pageout(og);
							
							// Need more data
							if (result == 0) break;
							
							if (result == 1) 
							{
								os.pagein(og);
								while (i < 2) 
								{
									result = os.packetout(op);
									if (result == 0) break;
									if (result == -1) 
									{
										throw new Exception("Corrupt secondary header.  Exiting.");
									}
									vi.synthesis_headerin(vc, op);
									i++;
								}
							}
						}
	
						index = oy.buffer(BUFSIZE);
						buffer = oy.data;
						bytes = bitStream.read(buffer, index, BUFSIZE);
						
						if (bytes == 0 && i < 2) 
						{
							throw new Exception("End of file before finding all Vorbis headers!");
						}
						oy.wrote(bytes);
					}
	
					convsize = BUFSIZE / vi.channels;
	
					vd.synthesis_init(vi);
					vb.init(vd);
	
					float[][][] _pcmf = new float[1][][];
					int[] _index = new int[vi.channels];
	
					getOutputLine(vi.channels, vi.rate);
	
					while (eos == 0) 
					{
						while (eos == 0) 
						{
							if (player != me) return;
							
	
							int result = oy.pageout(og);
							if (result == 0) break; // need more data
							if (result == -1) 
							{ 	// missing or corrupt data at this page
								// position
								// System.err.println("Corrupt or missing data in
								// bitstream;
								// continuing...");
							} 
							else 
							{
								os.pagein(og);
	
								if (og.granulepos() == 0) 
								{
									chained = true;
									eos = 1;
									break;
								}
	
								while (true) 
								{
									if (checkState()) return;
									
									result = os.packetout(op);
									if (result == 0) break; // need more data
									if (result == -1) 
									{ 
										// missing or corrupt data at
										// this page position
										// no reason to complain; already complained
										// above
	
										// System.err.println("no reason to complain;
										// already complained above");
									} 
									else 
									{
										// we have a packet. Decode it
										int samples;
										
										// test for success!
										if (vb.synthesis(op) == 0) 
										{    
											vd.synthesis_blockin(vb);
										}
										while ((samples = vd.synthesis_pcmout(_pcmf,_index)) > 0) 
										{
											if (checkState()) return;
											
											float[][] pcmf = _pcmf[0];
											int bout = (samples < convsize ? samples : convsize);
	
											// convert doubles to 16 bit signed ints
											// (host order) and
											// interleave
											for (i = 0; i < vi.channels; i++) 
											{
												int ptr = i * 2;
												int mono = _index[i];
												for (int j = 0; j < bout; j++) 
												{
													int val = (int) (pcmf[i][mono + j] * 32767.);
													
													//Clip the values to a valid level
													val = Math.max(-32768, Math.min(val, 32767));
													
													if (val < 0) val = val | 0x8000;
													convbuffer[ptr] = (byte) (val);
													convbuffer[ptr + 1] = (byte) (val >>> 8);
													ptr += 2 * (vi.channels);
												}
											}
											outputLine.write(convbuffer, 0, 2 * vi.channels * bout);
											vd.synthesis_read(bout);
										}
									}
								}
								if ( og.eos() != 0 ) eos = 1;
							}
						}
	
						if (eos == 0) 
						{
							index = oy.buffer(BUFSIZE);
							buffer = oy.data;
							bytes = bitStream.read(buffer, index, BUFSIZE);
	
							if (bytes == -1) break;
							
							oy.wrote(bytes);
							if (bytes == 0) eos = 1;
						}
					}
	
					//Finish up and clear memory
					os.clear();
					vb.clear();
					vd.clear();
					vi.clear();
				}
	
				oy.clear();
			}
		}
	}
}
