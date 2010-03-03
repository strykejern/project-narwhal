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
}
