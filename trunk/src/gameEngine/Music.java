package gameEngine;

public class Music extends Sound {
	
	public static boolean enabled = true;
	private static float musicVolume = 0.5f;
	private static Sound music;

	/**
	 * JJ> Starts looping a music track. only one music track can 
	 *     be played at the same time
	 * @param song The Sound object to be looped.
	 */
	public static void play( String song ){
		if(!enabled) return;
		
		//Load music into memory if that hasn't been done yet
		if( soundList == null ) loadAllSounds();
		
		//Dont play non-existing music
		if( !soundList.containsKey(song) )
		{
			Log.warning("Tried to play non-existing song track - " + song);
			return;
		}
		
		//Stop any existing music
		stopMusic();
		
		//Set the new song
		music = soundList.get(song);
		
		//Play the next song
		music.playLooped(musicVolume, 0);
	}
	
	/**
	 * JJ> Stops playing the music
	 */
	public static void stopMusic() {
		if( music != null ) music.silence();
	}
	
	
	/**
	 * JJ> Duplicate of the Sound constructor. Java requires it.
	 * @param fileName The name of the song to be loaded
	 */
	private Music(String fileName) {
		super(fileName);
	}

}
