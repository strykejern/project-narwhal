import  sun.audio.*;    //import the sun.audio package
import  java.io.*;

public class Sound 
{

	static public void testPlaySound(String filename)
	{
		InputStream in;
		AudioStream as;
		
		//** add this into your application code as appropriate
		// Open an input stream  to the audio file.
		try 
		{
			in = new FileInputStream(filename);
		
			// Create an AudioStream object from the input stream.
			as = new AudioStream(in);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
	
		// Use the static class member "player" from class AudioPlayer to play
		// clip.
		AudioPlayer.player.start(as);            
		
		// Similarly, to stop the audio.
		//AudioPlayer.player.stop(as); 		
	}
}
