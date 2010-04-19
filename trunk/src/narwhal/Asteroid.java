package narwhal;

import java.util.Random;
import gameEngine.*;

public class Asteroid extends GameObject {
	static int meteorCount = 0;
	private float size;
	
	public Asteroid( Vector pos, Game world, float size ) {
		super(world);
		Random rand = new Random();
		
		//What world do we spawn inside?
		this.world = world;
		
		//Randomize image
		image = new Image2D("data/asteroid" + rand.nextInt(4) + ".png");
				
		this.size = size;
		if( size == 0 ) this.size = 1 + rand.nextFloat();
		image.scale( this.size );
				
		if(rand.nextBoolean()) image.verticalFlip();
		if(rand.nextBoolean()) image.horizontalFlip();
		
		//Initiate this object
		setMaxLife( 20 );
		super.pos = pos;
		super.speed = new Vector(rand.nextInt(4)-2, rand.nextInt(4)-2);
		
		//Physics
		super.shape = Shape.CIRCLE;
		super.setRadius(image.getWidth()/2);
		super.canCollide = true;
		super.anchored = false;

		meteorCount++;
		
		mass = (float)Math.PI * radius * radius;
	}
	
	public void update(){
		image.rotate( getSpeed().length()/100 );
		super.update();
	}
	
	public void destroy() {
		if( !active() ) return;
		
		//Spawn 3 new asteroids
		if( size > 0.75 )
		{
			world.getEntityList().add( new Asteroid( pos.clone(), world, size / 2 ) );
			world.getEntityList().add( new Asteroid( pos.clone(), world, size / 2 ) );
			world.getEntityList().add( new Asteroid( pos.clone(), world, size / 2 ) );
		}
		
		super.destroy();
	}
}
