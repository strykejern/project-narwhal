package narwhal;
import gameEngine.Image2D;
import gameEngine.Vector;

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
 * JJ> Standardized object class. An object is anything in space. Such as a ship, planet, missile
 * or even an asteroid
 * @author Johan Jansen and Anders Eie
 */
public class Object {
	private boolean allowCollision;
	Vector velocity;
	Vector pos;
	Image2D sprite;
		
	public Object( Image2D copySprite, int x, int y ) {
		this.initialize(copySprite, x, y);
	}
	public Object( String fileName, int x, int y ) {
		this.initialize(new Image2D(fileName), x, y);
	}
	
	/*
	 * JJ> This allows multiple constructor overloads
	 */
	private void initialize(Image2D copySprite, int x, int y)
	{
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
		if( !this.allowCollision || !target.isCollidable() ) return false;

		//Rectangle this
		int dtx = this.pos.getX() - this.sprite.getWidth()/2;
		int dty = this.pos.getY() - this.sprite.getHeight()/2;
		int dbx = this.pos.getX() + this.sprite.getWidth()/2;
		int dby = this.pos.getY() + this.sprite.getHeight()/2;
		
		//Rectangle they
		int ttx = target.pos.getX() - target.sprite.getWidth()/2;
		int tty = target.pos.getY() - target.sprite.getHeight()/2;
		int tbx = target.pos.getX() + target.sprite.getWidth()/2;
		int tby = target.pos.getY() + target.sprite.getHeight()/2;
		
		//check it out
		if( dtx <= ttx && dty <= tty  ) 
			if( dbx >= tbx && dby >= tby ) return true;
		
		return false;
	}
}
