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

public class Vector {
	public float x, y;
	
	public Vector(){
		x = 0;
		y = 0;
	}
	public Vector(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public Vector(float length, float angle, @SuppressWarnings("unused") boolean dummy){
        x = (float)Math.cos(angle) * length;
        y = (float)Math.sin(angle) * length;
	}

    public Vector clone() {
    	return new Vector(x,y);
    }
    
    public boolean equals(Vector vec){
    	return x == vec.x && y == vec.y;
    }
    
    public void negate() {
        x = -x;
        y = -y;
    }
    
    public Vector negated(){
    	return new Vector(-x, -y);
    }

	public void addDirection(float length, float angle) {
        x += (float)Math.cos(angle) * length;
        y += (float)Math.sin(angle) * length;
	}

	public void add(Vector v){
		x += v.x;
		y += v.y;
	}

	public Vector plus(Vector v){
		return new Vector(this.x + v.x, this.y + v.y);
	}
	
	public void sub(Vector v){
		x -= v.x;
		y -= v.y;
	}
	
	public Vector minus(Vector v){
		return new Vector(this.x - v.x, this.y - v.y);
	}
	
	public void multiply(float val){
		this.x *= val;
		this.y *= val;
	}
	
	public Vector times(float val){
		return new Vector(this.x * val, this.y * val);
	}
	
	public void divide(float val){
		this.x /= val;
		this.y /= val;
	}
	
	public Vector dividedBy(float val){
		return new Vector(this.x / val, this.y / val);
	}
	
	public float length() {
		if (x != 0.0f || y != 0.0f)
			return (float)Math.sqrt( (x*x)+(y*y) );
		else
			return 0.01f;
	}
	
	public void rotateTo(float radian){
		float length = length();
		x = (float)Math.cos(radian) * length;
		y = (float)Math.sin(radian) * length;
	}
	
	public void rotateToDegree(float degree){
		this.rotateTo((float)Math.toRadians(degree));
	}
	
	public void rotateBy(float radian){
		rotateTo(getAngle()+radian);
	}
	
	public void setLength(float length){
		float hyp = length();
		float fx, fy;
		if (hyp != 0)
		{
			fx = x / hyp;
			fy = y / hyp;
		}
		else
		{
			fx = 1;
			fy = 1;
		}
		this.x = fx * length;
		this.y = fy * length;
	}
	
	public void grow(float length){
		setLength(length()+length);
	}
	
	public void shrink(float length){
		setLength(length()-length);
	}
	
	/**
	 * JJ> Gets the X value in this vector as a integer
	 * @return this.x as an integer
	 */
	public int getX() {
		return (int)x;
	}
	
	/**
	 * JJ> Gets the Y value in this vector as a integer
	 * @return this.y as an integer
	 */
	public int getY() {
		return (int)y;
	}
	
	public float getAngle(){
		return (float)Math.atan2(y, x);
	}
	
	public boolean isInsideRect(Vector pos, Vector size){
		if (x > pos.x && x < pos.x + size.x && y > pos.y && y < pos.y + size.y) return true;
		return false;
	}
	
	public boolean isInsideRect(Vector size){
		if (x > 0 && x < size.x && y > 0 && y < size.y) return true;
		return false;
	}
	
	public void overflowWithin(Vector vec){
		if (isInsideRect(new Vector(), vec)) return;
		while (x < 0) 	  x += vec.x;
		while (x > vec.x) x -= vec.x;
		while (y < 0) 	  y += vec.y;
		while (y > vec.y) y -= vec.y;
	}
	
	public Vector returnOverflowWithin(Vector vec){
		Vector clone = clone();
		if (clone.isInsideRect(new Vector(), vec)) return clone;
		while (clone.x < 0) 	clone.x += vec.x;
		while (clone.x > vec.x) clone.x -= vec.x;
		while (clone.y < 0) 	clone.y += vec.y;
		while (clone.y > vec.y) clone.y -= vec.y;
		return clone;
	}
	
	public boolean lessThan(Vector vec){
		return x < vec.x && y < vec.y;
	}
	
	public boolean lessThanEither(Vector vec){
		return x < vec.x || y < vec.y;
	}
	
	public void flipX(){
		x = -x;
	}
	
	public void flipY(){
		y = -y;
	}
	
	public Vector flippedX(){
		return new Vector(-x, y);
	}
	
	public Vector flippedY(){
		return new Vector(x, -y);
	}
	
	// Methods for game graphics with origin at top left of screen
	public boolean isTopLeftOf(Vector vec){
		return lessThan(vec);
	}
	
	public boolean isBotRightOf(Vector vec){
		return !lessThanEither(vec);
	}
	
	public boolean isBotLeftOf(Vector vec){
		if (x < vec.x && y > vec.y) return true;
		return false;
	}
	
	public boolean isTopRightOf(Vector vec){
		if (x > vec.x && y < vec.y) return true;
		return false;
	}
}
