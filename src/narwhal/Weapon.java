package narwhal;

import gameEngine.Particle;
import gameEngine.Vector;

import java.util.ArrayList;

public class Weapon {
	public float energyDamage;
	public float damage;
	public int cost;
	public int cooldown;
	public String particle;
	
	//Multipliers
	public float shieldMul;			//How effective against shields?
	public float lifeMul;			//How effective against life?
	
	public Weapon(float damage, int cost, int cooldown, String particle) {
		this.damage = damage;
		this.cost = cost;
		this.cooldown = cooldown;
		this.particle = particle;
		shieldMul = 1.00f;
		lifeMul = 1.00f;
	}

	public void spawnParticle(ArrayList<Particle> particleList, Vector pos, float direction, Vector speed) {
		direction %= 2 * Math.PI;
		Vector baseSpeed = speed.clone();
		speed = new Vector(20.0f, direction, true);
		speed.add(baseSpeed);
		particleList.add( new Particle(pos.clone(), particle, 200, 0.75f, -0.01f, direction, 0, speed ) );
	}
}
