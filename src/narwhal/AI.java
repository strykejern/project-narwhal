package narwhal;

import gameEngine.Input;
import gameEngine.Vector;

/**
 * JJ> This implements the game artificial intelligence using "FSM" logic. 
 * "Finite State Machine" logic uses a simple finite state machine to make its decisions. 
 * By reacting to events that have occurred, it can take on a personality that is almost lifelike:
 * 1. Initially, the AI will try to chase the target.
 * 2. Then it retreats, possibly since it took some damage during the confrontation.
 * 3. When far enough away, it moves about in a pattern to regain its compose.
 * 4. Then, after circling the target a bit to see what's going on, it begins its chase again (state 1).
 * Essentially, the "FSM" system is an amalgamation of different AI states.
 * @author Anders Eie and Johan Jansen
 *
 */
public class AI extends Spaceship {
	private Spaceship target;
	private aiLevel   level;
	private aiState   state;
	
	//TODO: change this to AI type?
	//Brute - Brute force attacks straight on, low retreat
	//Ambush - Attacks from behind, retreats if shot on
	//Controller - Tries to adapt tactics and use special powers
	enum aiLevel{
		STUPID,				//Extra stupid AI, no pathfinding? Do not avoid planets or black holes?
		DEFAULT,			//The normal AI
		SMART				//Perfect calculations? Tries to attack player from behind? Dodge enemy fire?
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
		level = aiLevel.DEFAULT;
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
		
		//Consider retreating if we are not stupid
		if( level != aiLevel.STUPID )
		{
			//Retreat if shields are down
			//but only if the enemy has some shields left and some energy to spend
			if( ( shieldMax != 0 && shield == 0 ) && target.shield != 0 
					&& target.energy > target.energyMax/10 ) 			state = aiState.RETREAT;
	
			//Retreat if less than 10% energy left
			//but only if our enemy has more energy than us (cheat!)
			else if( energy < energyMax/10 && target.energy > energy ) state = aiState.RETREAT;
		}
		
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
			
			//Stop retreating if we are ready (33% energy and 20% shield)
			if( energy > energyMax/3 && shield > shieldMax/5) state = aiState.INTERCEPT;
			
			//Run away
			if(fDistance < 500)
			{
				keys.up = true;
				keys.down = false;
			}
			else
			{
				keys.up = false;
				keys.down = true;
			}
			keys.mosButton1 = false;
			
			//Opposite direction of target
			//not exactly, but it will do for now
			keys.mousePos = target.getPosCentre().clone();
			keys.mousePos.rotateTo(2-keys.mousePos.getAngle());
		}
		
		super.update();
	}

}
