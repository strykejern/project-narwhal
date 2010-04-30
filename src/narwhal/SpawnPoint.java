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
	
	final Type type;
	String name;
	Vector pos;
	aiType ai;
	String team;
	int size;
	
	public SpawnPoint(Type type) {
		this.type = type;
	}
}
