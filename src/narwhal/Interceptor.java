package narwhal;

import java.util.ArrayList;

import gameEngine.GameObject;
import gameEngine.Input;
import gameEngine.Log;
import gameEngine.Sound;
import gameEngine.Vector;
import gameEngine.GameEngine;

public class Interceptor extends Spaceship {
	
	private static final int FUEL_TIME = 1500;
	
	//Sound effects
	private static Sound launch = new Sound("shiplaunch.wav");
	private static Sound dock   = new Sound("shipdock.wav");	
	
	private ArrayList<GameObject>	entities;	//Contains all gameObjects in the universe...
    private long timer;							//AI timer
    private Spaceship target;					//What's our target?
    private State state;						//What are we currently doing?
    private Spaceship master;					//Who's our boss?
    private int fuel;							//How many frames before we need to return to master?
    
    private enum State {
    	FOLLOW,
    	ATTACK,
    	REFUEL
    }
    
	public Interceptor(Vector pos, SpaceshipTemplate blueprint, Spaceship master) {
		super(blueprint, master.team, master.world);
		this.pos 	    = pos;
		keys 		    = new Input();
		speed           = master.getSpeed().clone();
		fuel 			= FUEL_TIME;
		
		//Folow master
		this.master 	= master;
		state           = State.FOLLOW;
		
		//Set references
		this.world 		= master.world;
		entities 		= master.world.getEntityList();
		
		//Play sound
		launch.play3D(pos, GameEngine.getCameraPos());
	}

	public void update() {
		
		//We need to return to our master if fuel runs out
		fuel--;
		if( outOfFuel() )
		{
			state = State.REFUEL;
			
			//Die if we go way behind refuel schedule
			if(fuel == -FUEL_TIME)
			{
				this.destroy();
				return;
			}
		}
		
		//We need our master to survive
		if( !master.active() ) this.destroy();
		
		//Only be active every so often
		if( timer > System.currentTimeMillis() )
		{
			super.update();
			return;
		}

		//Remove any previous actions first
		resetInput();

		//STATE REFUEL - Return to master and dock
		if( state == State.REFUEL )
		{
			//Move at full speed towards master
			keys.mousePos = master.getPosCentre();
			keys.up = true;
			if( getDistanceTo(master) < 400 && getSpeed().length() > maxSpeed/2 ) getSpeed().setLength(maxSpeed/2);
			timer = System.currentTimeMillis() + 50;
		}
		
		//STATE FOLLOW - Follow master and look for enemies
		else if( state == State.FOLLOW )
		{
			target = master;
			
			//Find enemy target, but only if master is close
			if( getDistanceTo(master) < 1000 )
			{
				for( int i = 0; i < entities.size(); i++ )
				{
					//Skip non-spaceships
					if( !(entities.get(i) instanceof Spaceship) ) continue;						
					Spaceship tryTarget = (Spaceship)entities.get(i);
					
					//Don't target ourself, that would be silly
					if( tryTarget == this || !tryTarget.active() ) continue;
					
					//Don't target friendlies
					if( tryTarget.team.equals(this.team) ) continue;
								
					//Calculate distance. If it is less than the last target, keep this one instead
					if( getDistanceTo(tryTarget) < 800 )
					{	
						target = tryTarget;
						break;
					}
				}
			}
			
			//Enter attack mode if we found a target
			if( !invalidTarget() ) 	state = State.ATTACK;
			
			//Nope, just follow the master
			else
			{
				keys.mousePos = master.getPosCentre();
				keys.mousePos.addDirection( 150, master.getDirection() );
				
				//TODO: circle around master
				if( getDistanceTo(master) > 450 ) keys.up = true;
				else							  
				{
					if( getSpeed().length() > maxSpeed*0.66f ) getSpeed().setLength(maxSpeed*0.66f);
					keys.down = true;
				}
			}
			timer = System.currentTimeMillis() + 100;
		}
		
		//STATE ATTACK - Follow and fire at enemy target!
		else if( state == State.ATTACK )
		{
			//Stop attacking if enemy ran away
			if( invalidTarget() || getDistanceTo(target) > 1000 ) state = State.FOLLOW;
			
			//Stop attacking if master ran away
			else if ( getDistanceTo(master) > 1200 ) state = State.FOLLOW;

			//Follow the enemy target
			keys.mousePos = target.getPosCentre();
			keys.up = true;

			//Shoot?
			if( facingTarget(target, 700) )
			{
				keys.mosButton1 = true;
			}
			timer = System.currentTimeMillis() + 50;
		}
		
	}
	
	private boolean invalidTarget(){
		if( target == null ) 				return true;
		if( !target.active() ) 				return true;
		if( target.equals(this) ) 			return true;
		if( target.team.equals(this.team) ) return true;
		if( target.disguised != null )      return true;
		return false;
	}

	public GameObject getMaster() {
		return master;
	}

	public void dock() {
		
		if( !master.active() || !this.active() ) return;
		
		//Give master the life back
		master.setLife( Math.min(master.getMaxLife(), master.getLife() + this.getLife()) );
		
		//Play sound
		dock.play3D(this.pos, GameEngine.getCameraPos());
		
		super.remove();
	}
	
	public boolean outOfFuel() {
		return fuel <= 0;
	}

}
