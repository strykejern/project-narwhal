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
package gameEngine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import narwhal.Game;
import narwhal.Weapon;

public abstract class GameObject extends Physics{
	protected static final int INACTIVE_OBJECT = Integer.MIN_VALUE;
	protected static final int OBJECT_INVULNERABLE = Integer.MAX_VALUE;

	protected Input keys;
	protected Image2D image;
	public Game world;
	
	private float lifeMax;
	private float life;

	public GameObject( Game world ){
		super();
		
		//Assume that is inactive unless someone specifies otherwise with setMaxLife()
		life = INACTIVE_OBJECT;

		if(world == null)
		{
			Log.warning("INVALID GAME OBJECT SPAWN! (This is a bad error and might cause null pointer exception): " + this.getClass().getSimpleName());
		}
		this.world = world;
	}
	
	public void update() {
		
		if(world == null) 
		{
			Log.warning("INVALID OBJECT UPDATE, DELETING OBJECT: " + this.getClass().getSimpleName());
			this.destroy();
			return;
		}
		
		// Quick implement of universe bounds
		float uniX = world.universeSize * GameEngine.getScreenWidth();
		float uniY = world.universeSize * GameEngine.getScreenHeight();
		
		if 		(pos.x < 0) 	pos.x = uniX + pos.x;
		else if (pos.x > uniX)  pos.x %= uniX;
		
		if 		(pos.y < 0) 	pos.y = uniY + pos.y;
		else if (pos.y > uniY)  pos.y %= uniY;

		super.update();
	}
	
	public void draw(Graphics2D g, Vector offset) {
		image.draw(g, drawX(offset), drawY(offset));
	}
	
	public void drawCollision(Graphics g, Vector offset) {
		if( !GameEngine.getConfig().debugMode ) return;
		
		//Always draw the image bounds
		int w = image.getWidth();
		int h = image.getHeight();
		g.setColor(Color.BLUE);
		g.drawRect(drawX(offset), drawY(offset), w, h);

		//Draw it as a circle
		if( super.shape == Shape.CIRCLE )
		{
			w = size.getX();
			h = size.getY();
			Vector drawPos = getPosCentre().minus(new Vector(radius, radius)).minus(offset);
			g.setColor(Color.RED);
			g.drawOval(drawPos.getX(), drawPos.getY(), (int)radius*2, (int)radius*2);
		}
	}
	
	protected int drawX(Vector offset){
		return pos.getX() - offset.getX();
	}
	
	protected int drawY(Vector offset){
		return pos.getY() - offset.getY();
	}
	
	public Vector getPosCentre(){
		return pos.plus(new Vector(image.getWidth()/2, image.getHeight()/2));
	}
	
	public float getDirection() {
		return direction;
	}	
	
	public boolean active() {
		return life != INACTIVE_OBJECT;
	}
	
	public void destroy(){
		
		//Can't destroy what is already destroyed
		if( !active() ) return;

		//Mark as dead so that it gets removed in the next update
		life = INACTIVE_OBJECT;
		
		//Free any resources
		image.dispose();
		world = null;
	}

	public float getLife() {
		return life;
	}

	public float getMaxLife() {
		return lifeMax;
	}

	public void setMaxLife( float set ) {
		life = lifeMax = set;
	}

	public void setLife( float set ) {
		life = set;
		
		//Upper limit
		if( life >= lifeMax ) life = lifeMax;
		
		//If equal or below 0, destroy this one
		if(life <= 0)
		{
			this.destroy();
		}
	}
	
	public void damage( Weapon weapon ) {
		
		//Can't be killed
		if( lifeMax == OBJECT_INVULNERABLE ) return;
		
		setLife(life - weapon.damage*weapon.lifeMul);
	}
	
	/**
	 * JJ> Determines whether the we are looking towards the specified spaceship in a 120 degree cone
	 *     with no limit in distance
	 * @param target Who are we supposed to be looking at?
	 * @return Returns true if target is within the cone, false otherwise
	 */
	public boolean facingTarget( GameObject target ) {
		return facingTarget(target, Float.MAX_VALUE);
	}
	
	/**
	 * JJ> Determines whether the we are looking towards the specified spaceship in a 120 degree cone
	 * @param target Who are we supposed to be looking at?
	 * @param distance The maximum distance from us to the target
	 * @return Returns true if target is within the cone, false otherwise
	 */
	public boolean facingTarget( GameObject target, float distance ) {
		Vector diff = target.getPosCentre().minus(getPosCentre());
		if( this.direction - diff.getAngle() < Math.PI/3
				&& diff.length() < distance) return true;
		return false;
	}
	
	/**
	 * JJ> Gets the distance from this GameObject to the specified GameObject
	 * @param target Which GameObject to find the distance to
	 * @return A float describing the distance between the two objects
	 */
	public float getDistanceTo( GameObject target ) {
		return target.getPosCentre().minus(getPosCentre()).length();
	}
	
}
