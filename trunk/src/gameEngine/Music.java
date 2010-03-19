package gameEngine;

public class Music {
	private static float musicVolume = 0.05f;
	private static Sound music;
	public static boolean enabled = true;

	/**
	 * JJ> Starts looping a music track. only one music track can 
	 *     be played at the same time
	 * @param song The Sound object to be looped.
	 */
	public static void play(Sound song){
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
	
	/**
	 * JJ> Stops playing the music
	 */
	public static void stopMusic() {
		if(music != null) music.stop();
	}
}
