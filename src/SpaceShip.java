
public class SpaceShip {
	float acceleration = 0;
	float xPos = 0, yPos = 0, direction = 0;
	Image2D sprite;
	
	public SpaceShip()	{
		sprite = new Image2D("data/spaceship.png");
		sprite.resize(64, 64);
	}
	
	void Update() {
		xPos += acceleration/10;
		yPos += acceleration/10;
	}


}
