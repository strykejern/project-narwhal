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

/**
 * JJ> Standarized object class. An object is anything in space. Such as a ship, planet, missile
 * or even an asteroid
 * @author Johan Jansen og Anders Eie
 */
public class Object {
	private boolean allowCollision;
	Vector velocity;
	Vector pos;
	Image2D sprite;
	
	public Object( Image2D copySprite, int x, int y ) {
		sprite = copySprite;
		velocity = new Vector();
		pos = new Vector(x, y);
		allowCollision = false;
	}
	
	public void Move() {
		pos.add(velocity);
	}
	
	//JJ> Helper functions to set and disable collisions for this object
	public boolean isCollidable() {
		return allowCollision;
	}
	public void disableCollision() {
		allowCollision = false;
	}
	public void enableCollision() {
		allowCollision = true;
	}

	//JJ> TODO: implement this function
	public boolean collidesWith( Object target ) {
		
		//Are both collidable?
		if( !this.allowCollision || target.isCollidable() ) return false;
		
		return this.pos == target.pos;
	}
}
