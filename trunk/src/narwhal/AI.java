package narwhal;

import gameEngine.Input;
import gameEngine.Vector;

public class AI extends Spaceship {
	private Spaceship target;
	private aiLevel   level;
	private aiState   state;
	
	//TODO: change this to AI type?
	//Brute - Brute force attacks straight on, low retreat
	//Ambush - Attacks from behind, retreats if shot on
	//Controller - Tries to adapt tactics and use special powers
	enum aiLevel{
		STUPID,				//Extra stupid AI, no pathfinding?
		DEFAULT,			//The normal AI
		SMART				//Perfect calculations? Tries to attack player from behind?
	}
	
	enum aiState {
		RETREAT,			//Run away to fill energy and shields or to retreat from attack
		INTERCEPT,			//Move in to target, maybe from behind or maybe the fastest route
		COMBAT				//Shoot at target, maybe move in circles around it?
	}
	
	public AI(String name) {
		super(name);
		
		super.keys = new Input();
		keys.setCameraPos(new Vector());
		target = this;
		state = aiState.INTERCEPT;
		level = aiLevel.STUPID;
	}
	
	public void setTarget(Spaceship target) {
		this.target = target;
	}
	
	public void update() {		
		//Some strange bug is causing this not to happen in the constructor above?
		if(keys == null)
		{
			super.keys = new Input();
			keys.setCameraPos(new Vector());
		}
		
		//Calculate distance from target
		Vector vDistance = target.getPosCentre().minus(getPosCentre());
		float fDistance = Math.abs(vDistance.x + vDistance.y);

		//AI State - Intercept
		if( state == aiState.INTERCEPT )
		{
			//Start combat mode if close enough
			if(fDistance < 600) state = aiState.COMBAT;
			
			//Move towards target
			keys.up = true;
			keys.down = false;
			keys.mosButton1 = false;
			
			//Focus on target
			keys.mousePos = target.getPosCentre();
		}
		
		//AI State - Combat
		else if( state == aiState.COMBAT )
		{
			//Intercept if target ran away
			if(fDistance > 600) state = aiState.INTERCEPT;

			//Stand still and shoot
			keys.up = false;
			keys.down = true;
			keys.mosButton1 = true;
			
			//Focus on target
			keys.mousePos = target.getPosCentre();
		}
		
		//AI State - Retreat
		else if( state == aiState.RETREAT )
		{
			//TODO: scenarios for retreat:
			//- if low shields
			//- if under attack from behind
			//- need to regenerate energy
			//- to prepare an ambush
		}
		
		super.update();
	}

}
