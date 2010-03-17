package gameEngine;

import gameEngine.Sound.OggClip;

import java.io.BufferedInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jcraft.jogg.*;
import com.jcraft.jorbis.*;

/****************************************************************************************
 * JJ> Everything below is taken from the JOrbis Player
 ***************************************************************************************/
public class JOrbisPlayer {
	
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
	
	//Pointers
	public SourceDataLine outputLine;
	private BufferedInputStream bitStream;

	//JJ> Constructor
	public JOrbisPlayer(BufferedInputStream stream){
		resetData();
		outputLine = null;
		bitStream = stream;
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
		} 
		catch (Exception e) 
		{
			Log.warning("Initialize Java Sound - " + e);
		}
	}

	private SourceDataLine getOutputLine(int channels, int rate) 
	{
		if (outputLine == null || this.rate != rate
				|| this.channels != channels) 
		{
			//Stop outputline
			if (outputLine != null) 
			{
				outputLine.drain();
				outputLine.stop();
				outputLine.close();
			}
			
			//Start new outputline
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

	public void playStream(OggClip ogg, Thread me) throws Exception {
		boolean chained = false;
		resetData();

		while (true) 
		{
			if ( ogg.stopped() ) return;
			
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
					if ( ogg.stopped() ) return;
					
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
					if (ogg.player != me) return;
					

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
							if (ogg.stopped()) return;
							
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
									if ( ogg.stopped() ) return;
									
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
