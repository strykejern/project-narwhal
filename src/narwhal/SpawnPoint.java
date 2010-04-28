package narwhal;

import gameEngine.Vector;
import narwhal.AI.aiType;

/**
 * JJ> Simple data structure to hold some spawning information
 * @author Anders Eie and Johan Jansen
 *
 */
public class SpawnPoint {
	
	public enum Type {
		SPACESHIP,
		PLANET,
		PLAYER,
	}
	
	String name;
	Vector pos;
	aiType ai;
	String team;
	Type type;
	int size;
	
	public SpawnPoint(Type type) {
		this.type = type;
	}
}
