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
	}
	
	String name;
	Vector pos;
	aiType ai;
	String team;
	Type type;
	
	public SpawnPoint( Type type, String name, Vector pos, aiType ai, String team) {
		this.name = name;
		this.pos = pos;
		this.ai = ai;
		this.team = team;
		this.type = type;
	}
}
