package narwhal;

import java.util.ArrayList;
import java.util.Random;

import gameEngine.GameObject;
import gameEngine.Input;
import gameEngine.Log;
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
	private aiType    type;
	private aiState   state;
	private long 	  aiTimer;
	private ArrayList<GameObject>	entities;		// Contains all gameObjects in the universe...
	
	enum aiType{
		PLAYER,		 //This one is player controlled
		BRUTE,		 //Brute force attacks straight on, low retreat
		AMBUSH,		 //Attacks from behind, retreats if shot on, sneaky sniper
		CONTROLLER,	 //Tries to adapt tactics and use special powers. Perfect calculations? Tries to attack player from behind? Dodge enemy fire?
		FOOL		 //The stupid clumsy AI, Extra stupid AI, no pathfinding? Do not avoid planets or black holes?
	}
	
	enum aiState {
		DISABLED,			//No AI, the player is controlling this one
		RETREAT,			//Run away to fill energy and shields or to retreat from attack
		INTERCEPT,			//Move in to target, maybe from behind or maybe the fastest route
		COMBAT				//Shoot at target, maybe move in circles around it?
	}
	
	public AI(SpaceshipTemplate name, String team) {
		super(name, team);
	}
	
	public void instantiate(Vector pos, Game world, aiType AI) {
		this.pos 	    = pos;
		
		//Are we player or AI?
		type 		   	= AI;
		if( AI == aiType.PLAYER )
		{
			state 		= aiState.DISABLED;
			keys 		= world.getPlayerController();
		}
		else
		{
			state 		= aiState.INTERCEPT;
			keys 		= new Input();
		}
		
		//Set references
		universeSize   	= world.universeSize;
		particleEngine 	= world.getParticleEngine();		
		entities 		= world.getEntityList();
	}
	
	private boolean invalidTarget(){
		if( target == null ) 				return true;
		if( !target.active() ) 				return true;
		if( target.equals(this) ) 			return true;
		if( target.team.equals(this.team) ) return true;
		return false;
	}
		
	public void update() {		

		//Don't do AI
		if( state == aiState.DISABLED || aiTimer > System.currentTimeMillis() )
		{
			super.update();
			return;
		}

		//Figure out what AI to use
		switch( type )
		{			
			//Brute force attacks straight on, low retreat
			case BRUTE:  	 doBruteAI();    	break;
			
			//Attacks from behind, retreats if shot on, sneaky sniper
			case AMBUSH: 	 doAmbusherAI(); 	break;	
			
			//Tries to adapt tactics and use special powers. Perfect calculations? Tries to attack player from behind? Dodge enemy fire?
			case CONTROLLER: doControllerAI();	break;  
			
			//The stupid clumsy AI, Extra stupid AI, no pathfinding? Do not avoid planets or black holes?
			case FOOL:		 doFoolAI();		break;
			
			//Player controlled, don't run any AI
			default: case PLAYER:				break;
		}
				
		super.update();
	}
	
	private void doFoolAI() {
		// TODO Auto-generated method stub
		
	}

	private void doAmbusherAI() {
		// TODO Auto-generated method stub
		
	}

	private void doBruteAI() {
		//Randomizer
		Random rand = new Random();

		//Try to stick to a single target
		if( invalidTarget() )
		{
			target = getClosestTarget();
		}		
		
		//Reset any controllers first
		resetInput();
		
		//Calculate distance from target
		float fDistance = target.getPosCentre().minus(getPosCentre()).length();

		//AI State - Intercept
		if( state == aiState.INTERCEPT )
		{
			//Start combat mode if close enough
			if(fDistance < 600) state = aiState.COMBAT;
			
			//Move towards target, but slow down once we are close enough
			if( fDistance < 1200 && speed.length() > maxSpeed*0.66f )
			{
				keys.down = true;
			}
			else
			{
				keys.up = true;
			}
			
			//Focus on target
			keys.mousePos = target.getPosCentre();
			
			//Slow and steady follow
			aiTimer = System.currentTimeMillis() + 200 + rand.nextInt(250);
		}
		
		//AI State - Combat
		else if( state == aiState.COMBAT )
		{
			//Intercept if target ran away
			if(fDistance > 600) state = aiState.INTERCEPT;

			//Stand still and shoot
			keys.down = true;
			
			//Pick either weapon, 50%
			if( rand.nextBoolean() ) keys.mosButton1 = true;
			else 					 keys.mosButton2 = true;
			
			//Focus on target
			keys.mousePos = target.getPosCentre();
			
			//Combat intensive
			aiTimer = System.currentTimeMillis() + rand.nextInt(20);
		}
		
		
	}

	private void doControllerAI() {
		//Randomizer
		Random rand = new Random();

		//We change targets very often, depending on the situation
		target = getClosestTarget();

		//Reset any controllers first
		resetInput();

		//Calculate distance from target
		Vector vDistance = target.getPosCentre().minus(getPosCentre());
		float fDistance = vDistance.length();

		//Consider retreating
		//Retreat if shields are down
		//but only if the enemy has some shields left and some energy to spend
		if( ( shieldMax != 0 && shield == 0 ) && target.shield != 0 
				&& target.energy > target.energyMax/10 ) 			state = aiState.RETREAT;

		//Retreat if less than 10% energy left
		//but only if our enemy has more energy than us (cheat!)
		//else if( energy < energyMax/10 && target.energy > energy ) state = aiState.RETREAT;
		//TODO: disabled this because the AI got very chicken, needs more work
		
		//AI State - Intercept
		if( state == aiState.INTERCEPT )
		{
			//Start combat mode if close enough
			if(fDistance < 600) state = aiState.COMBAT;
			
			//Move towards target, but slow down once we are close enough
			if( fDistance < 1200 && speed.length() > maxSpeed/2 )
			{
				keys.down = true;
			}
			else
			{
				keys.up = true;
			}
			
			//Focus on target
			keys.mousePos = target.getPosCentre();
			
			//Slow and steady follow
			aiTimer = System.currentTimeMillis() + 200 + rand.nextInt(250);
		}
		
		//AI State - Combat
		else if( state == aiState.COMBAT )
		{
			//Intercept if target ran away
			if(fDistance > 600) state = aiState.INTERCEPT;

			//Stand still and shoot
			keys.down = true;

			//Pick either weapon, 50%
			if( rand.nextBoolean() ) keys.mosButton1 = true;
			else 					 keys.mosButton2 = true;
			
			//Focus on target
			keys.mousePos = target.getPosCentre();
			
			//Combat intensive
			aiTimer = System.currentTimeMillis() + rand.nextInt(20);
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
			if( energy > energyMax/3 && shield > shieldMax/5) 
			{
				state = aiState.INTERCEPT;
			}
			
			//Run away
			if(fDistance < 500)
			{
				keys.up = true;
			}
			else
			{
				keys.down = true;
			}
			
			//Opposite direction of target
			//not exactly, but it will do for now
			keys.mousePos = target.getPosCentre().clone();
			keys.mousePos.rotateTo(pos.minus(target.getPosCentre()).getAngle());
			
			//Slow reaction retreat
			aiTimer = System.currentTimeMillis() + 500 + rand.nextInt(350);
		}		
	}
	
	private void resetInput() {
		keys.down = false;
		keys.left = false;
		keys.left = false;
		keys.right = false;
		keys.up = false;
		keys.mosButton1 = false;
		keys.mosButton2 = false;
		keys.mosButton3 = false;
	}
	
	/**
	 * JJ> This loops through every active Spaceship in the game to find a valid
	 *     enemy target that is active and not invisible for us.
	 * @return The closest alive enemy target to this Spaceship
	 */
	private Spaceship getClosestTarget() {
		Spaceship bestTarget = this;
		float bestDistance = Float.MAX_VALUE;
		
		//Go through every entity in the game
		for(int i = 0; i < entities.size(); i++)
		{
			//Skip non-spaceships
			if( !(entities.get(i) instanceof Spaceship) ) continue;						
			Spaceship target = (Spaceship)entities.get(i);
			
			//Don't target ourself, that would be silly
			if( target == this || !target.active() ) continue;
			
			//Don't target friendlies
			if( target.team.equals(this.team) ) continue;
			
			//Calculate distance. If it is less than the last target, keep this one instead
			float dist = target.getPosCentre().minus(getPosCentre()).length();
			if( dist < bestDistance ) 
			{	
				bestTarget = target;
				bestDistance = dist;
			}
		}
		
		//TODO: Debug?
		if(target == this )
		{
			Log.message("Could not find a target! Disabling AI");
			state = aiState.DISABLED;
		}
		
		return bestTarget;
	}
}
