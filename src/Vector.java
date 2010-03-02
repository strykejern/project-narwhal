
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
