package narwhal;

import java.util.ArrayList;
import java.util.Random;

import gameEngine.GameObject;
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
	private aiType    type;
	private aiState   state;
	private long 	  aiTimer;
	ArrayList<GameObject>	entities;		// Contains all gameObjects in the universe...
	
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
		COMBAT,				//Shoot at target, maybe move in circles around it?
		PATROL
	}
	
	public AI(SpaceshipTemplate name, String team, Game world) {
		super(name, team, world);
	}
	
	public void instantiate(Vector pos, aiType AI) {
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
		//TODO unessecary to do here, move to constructor or replace functions
		particleEngine 	= world.getParticleEngine();		
		entities 		= world.getEntityList();
	}
	
	private boolean invalidTarget(){
		if( target == null ) 				return true;
		if( !target.active() ) 				return true;
		if( target.equals(this) ) 			return true;
		if( target.team.equals(this.team) ) return true;
		if( target.disguised != null )		return true;
		return false;
	}
		
	public void update() {		
		
		//Avoid collisions with planets, this is done outside the normal AI loop so that it is
		//calculated realtime. If this is a resource hog, then we can implement a throttler
		findPath();
		
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
		//Randomizer
		Random rand = new Random();

		//Try to stick to a single target
		if( invalidTarget() )
		{
			target = getClosestTarget(1000);
		}
		
		//Reset any controllers first
		resetInput();
		
		//Calculate distance from target
		float distance = getDistanceTo(target);

		//AI State - Intercept
		if( state == aiState.INTERCEPT )
		{
			
			//Focus on target
			keys.mousePos = target.getPosCentre();
			keys.up = true;
			
			//Start combat mode if close enough
			if( distance < 400 )
			{
				state = aiState.COMBAT;
				if( getSpeed().length() > maxSpeed/2 ) getSpeed().setLength( maxSpeed/2 );
			}
			
			//Slow and steady follow
			aiTimer = System.currentTimeMillis() + 200 + rand.nextInt(250);
		}
		
		//AI State - Combat
		else if( state == aiState.COMBAT )
		{
			//Intercept if target ran away
			if( distance > 600 ) state = aiState.INTERCEPT;

			//Stand still and shoot
			keys.down = true;
			
			//Pick a weapon, 75% for primary weapon
			if( rand.nextBoolean() )
			{
				if( rand.nextInt(100) >= 25 ) keys.mosButton1 = true;
				else 					      keys.mosButton2 = true;
			}
			
			//Focus on target, but simulate a bad aim
			keys.mousePos = target.getPosCentre().plus( new Vector(rand.nextInt(20)-10, rand.nextInt(20)-10) );
			
			//Combat intensive
			aiTimer = System.currentTimeMillis() + rand.nextInt(50) + 100;
		}
		
		//AI State - Patrol
		else if( state == aiState.PATROL )
		{
			//Move randomly in a direction
			keys.mousePos = getPosCentre();
			keys.mousePos.addDirection(1200, rand.nextFloat() + direction - rand.nextFloat() );
			
			//We encountered a enemy, engage!
			if( !invalidTarget() ) state = aiState.INTERCEPT;
			
			//Don't move more than 50% of max speed
			if( getSpeed().length() > maxSpeed/2 ) keys.down = true;
			else 								   keys.up = true;

			//Slow reaction in patrol mode
			aiTimer = System.currentTimeMillis() + rand.nextInt(300) + 450;
		}	
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
			target = getClosestTarget((radarLevel+1)*800);
		}		
		
		//Reset any controllers first
		resetInput();
		
		//Spawn interceptors
		if( interceptor != null && getLife() >= getMaxLife()/2 )
		{
			keys.mosButton2 = keys.mosButton1 = true;
		}
		
		//Calculate distance from target
		float distance = getDistanceTo(target);

		//AI State - Intercept
		if( state == aiState.INTERCEPT )
		{
			//Start combat mode if close enough
			if( distance < 700 ) state = aiState.COMBAT;
			
			//Move towards target, but slow down once we are close enough
			if( distance < 1200 && getSpeed().length() > maxSpeed*0.66f )
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
			if( distance > 600 ) state = aiState.INTERCEPT;

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
		
		//AI State - Patrol
		else if( state == aiState.PATROL )
		{
			//Move randomly in a direction
			keys.mousePos = getPosCentre();
			keys.mousePos.addDirection(1200, rand.nextFloat() + direction - rand.nextFloat() );
			
			//We encountered a enemy, engage!
			if(!invalidTarget()) state = aiState.INTERCEPT;
			
			//Don't move more than 66% of max speed
			if( getSpeed().length() > maxSpeed*0.66f ) keys.down = true;
			else 									   keys.up = true;

			//Slow reaction in patrol mode
			aiTimer = System.currentTimeMillis() + rand.nextInt(300) + 450;
		}	
	}

	/**
	 * JJ> Tries to adapt tactics and use a lot of special powers. Does many good calculations. 
	 *     A dangerous combination between the other AI types. Dodges enemy fire.
	 */
	private void doControllerAI() {
		//Randomizer
		Random rand = new Random();

		//We change targets very often, depending on the situation
		target = getClosestTarget((radarLevel+1)*800);
		
		//Reset any controllers first
		resetInput();

		//Calculate distance from target
		float distance = getDistanceTo(target);

		//Consider retreating
		//Retreat if shields are down
		//but only if the enemy has some shields left and some energy to spend
		if( ( shieldMax != 0 && shield == 0 ) && target.shield != 0 
				&& target.energy > target.energyMax/10 ) 			state = aiState.RETREAT;

		//Retreat if less than 10% energy left
		//but only if our enemy has more energy than us
		//else if( energy < energyMax/10 && target.energy > energy ) state = aiState.RETREAT;
		//TODO: disabled this because the AI got very chicken, needs more work
		
		//Spawn interceptors
		if( interceptor != null && state != aiState.RETREAT && getLife() >= getMaxLife()/2 )
		{
			keys.mosButton2 = keys.mosButton1 = true;
		}

		//Disguise as asteroid
		if( canDisguise != null && disguised == null && state == aiState.INTERCEPT && energy >= energyMax )
		{
			keys.mosButton2 = keys.mosButton1 = true;
		}

		//AI State - Intercept
		if( state == aiState.INTERCEPT )
		{
			//Start combat mode if close enough
			if( disguised == null )
			{
				if( distance < 600) state = aiState.COMBAT;
			}
			else if( distance < 250) state = aiState.COMBAT;
			
			//Move towards target, but slow down once we are close enough
			if( distance < 1200 && getSpeed().length() > maxSpeed/2 )
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
			if( distance > 600 ) state = aiState.INTERCEPT;

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
			//- flanked or surrounded
						
			//Stop retreating if we are ready (33% energy and 20% shield)
			if( energy > energyMax/3 && (shieldMax == 0 || shield > shieldMax/5) ) 
			{
				state = aiState.INTERCEPT;
			}
			
			//Run away
			if( distance < 500)
			{
				keys.up = true;
			}
			else
			{
				keys.down = true;
			}
			
			//Opposite direction of target
			keys.mousePos = target.getPosCentre().clone();
			keys.mousePos.rotateTo(pos.minus(target.getPosCentre()).getAngle());
			
			//Slow reaction retreat
			aiTimer = System.currentTimeMillis() + 500 + rand.nextInt(350);
		}
		
		//AI State - Patrol
		else if( state == aiState.PATROL )
		{
			//Move randomly in a direction
			keys.mousePos = getPosCentre();
			keys.mousePos.addDirection(1200, rand.nextFloat() + direction - rand.nextFloat() );
			
			//We encountered a enemy, engage!
			if(!invalidTarget()) state = aiState.INTERCEPT;
			
			//Don't move more than 66% of max speed
			if( getSpeed().length() > maxSpeed*0.66f ) keys.down = true;
			else 									   keys.up = true;

			//Slow reaction in patrol mode
			aiTimer = System.currentTimeMillis() + rand.nextInt(300) + 450;
		}		
	}
		
	/**
	 * JJ> This loops through every active Spaceship in the game to find a valid
	 *     enemy target that is active and not invisible for us.
	 * @return The closest alive enemy target to this Spaceship
	 */
	private Spaceship getClosestTarget(float maxDistance) {
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
			
			//Don't target invisible or disguised enemies
			if( target.disguised != null ) continue;
			
			//Don't target friendlies
			if( target.team.equals(this.team) ) continue;
						
			//Calculate distance. If it is less than the last target, keep this one instead
			float dist = getDistanceTo(target);
			if( dist < bestDistance && dist < maxDistance ) 
			{	
				bestTarget = target;
				bestDistance = dist;
				
				//If we are looking at it and it's close, then it's good enough for us!
				if( dist < 800 && facingTarget(target) ) break;
			}
		}
		
		//Return to patrol AI if no enemy was found
		if(target == this ) state = aiState.PATROL;
		
		return bestTarget;
	}
	
	private void findPath() {
		
		//The stupid AI doesn't avoid planets
		if( type == aiType.FOOL || type == aiType.PLAYER ) return;
		
		//Combat intensive action doesn't need pathfinding
		if( state == aiState.DISABLED || state == aiState.COMBAT ) return;

		//Go through every entity in the game
		for(int i = 0; i < entities.size(); i++)
		{
			//Only check planets
			if( !(entities.get(i) instanceof Planet) ) continue;						
			Planet obstacle = (Planet)entities.get(i);
			
			//Skip inactive objects
			if( !obstacle.active() ) continue;
									
			//We are within 500 units of a planet and heading towards it
			if( facingTarget(obstacle, 600) )
			{				
				Vector diff = obstacle.getPosCentre().minus(getPosCentre());
				keys.mousePos = obstacle.getPosCentre();
				
				//Steer to the right
				if( this.direction > diff.getAngle() ) keys.mousePos.addDirection(900, direction+0.7f);

				//Steer to the left
				else								   keys.mousePos.addDirection(900, direction-0.7f);
								
				return;
			}
		}		
	}

	public Spaceship getHomingTarget( float distance ) {
		//Try the current target before finding a new one
		if( !invalidTarget() )
		{
			//Only if looking at the target and it is close enough
			if( facingTarget(target) && getDistanceTo(target) < distance ) return target;
		}
		
		//Current target isn't good enough, find another one instead
		Spaceship newTarget = getClosestTarget(distance);
		
		//No targets found!
		if( newTarget == this ) return null;
		
		//Gotcha!
		return newTarget;
	}

	public boolean isPlayer() {
		return type == aiType.PLAYER;
	}
}
