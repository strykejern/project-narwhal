package gameEngine;
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
	
    public Vector clone() {
    	return new Vector(x,y);
    }
    
    public void negate() {
        x = -x;
        y = -y;
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
	
	public float length(){
		if (x != 0.0f || y != 0.0f)
			return (float)Math.sqrt((x*x)+(y*y));
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

}
