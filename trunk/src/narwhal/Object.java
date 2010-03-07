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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import gameEngine.Collidable;
import gameEngine.Image2D;
import gameEngine.Vector;

/**
 * JJ> Standardized object class. An object is anything in space. Such as a ship, planet, missile
 * or even an asteroid
 * @author Johan Jansen and Anders Eie
 */
public class Object extends Collidable {
	private boolean allowCollision;
	Vector speed;
	Vector pos;
    private Image2D sprite;
		
	public Object( Image2D copySprite, int x, int y ) {
		super(0);
		this.initialize(copySprite, x, y);
	}
	public Object( String fileName, int x, int y ) {
		super(0);
		this.initialize(new Image2D(fileName), x, y);
	}
	
	/*
	 * JJ> This allows multiple constructor overloads
	 */
	private void initialize(Image2D copySprite, int x, int y)
	{
		sprite = copySprite;
		super.speed = speed = new Vector();
		super.pos = pos = new Vector(x, y);
		allowCollision = false;
		super.setRadius( sprite.getWidth()/2 );
	}
	
	public int getWidth() {
		return sprite.getWidth();
	}
	public int getHeight() {
		return sprite.getHeight();
	}

	public void update() {
		pos.add(speed);
		sprite.setDirection( (float)Math.toDegrees(super.direction) );
	}
	
    public float getAngle() {
	    return (float)Math.toDegrees(super.direction);
    }

    public Image getImage() {
	    return sprite.toImage();
    }

    public Image2D getSprite() {
	    return sprite;
    }

    /**
     * JJ> Resizes both the collision box and sprite for this object in space
     * @param width New width int
     * @param height New height int
     */
	public void resizeObject( int width, int height ) {
		super.setRadius( width/2 );
		sprite.resize( width, height );
	}
	
	/**
	 * JJ> Rotates this object, handles both collision box and sprite
	 * @param degrees How many degrees to rotate it by
	 */
	public void rotate( float degrees )
	{
		super.setDirection( (float)Math.toRadians(degrees) + super.direction );
	}
	
	/**
	 * JJ> Draws a collision circle for this object
	 */
	public void drawCollision(Graphics g)
	{
		int w = (int)super.getRadius()*2;
		int h = (int)super.getRadius()*2;
		g.setColor(Color.RED);
		g.drawOval(pos.getX()-w/2, pos.getY()-h/2, w, h);
		
		w = sprite.getWidth();
		h = sprite.getHeight();		
		g.setColor(Color.BLUE);
		g.drawRect(pos.getX()-w/2, pos.getY()-h/2, w, h);
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
}
