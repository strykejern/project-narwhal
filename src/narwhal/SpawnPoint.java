package narwhal;

import gameEngine.Vector;
import narwhal.AI.aiType;

/**
 * JJ> Simple data structure to hold some spawning information
 * @author Anders Eie and Johan Jansen
 *
 */
public class SpawnPoint {
	String name;
	Vector pos;
	aiType ai;
	String team;
	
	public SpawnPoint(String name, Vector pos, aiType ai, String team) {
		this.name = name;
		this.pos = pos;
		this.ai = ai;
		this.team = team;
	}
}
