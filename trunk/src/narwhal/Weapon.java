package narwhal;

import gameEngine.Sound;

public class Weapon {
	public float energyDamage;
	public float damage;
	public int cost;
	public int cooldown;
	public String particle;
	public String name;
	
	//Multipliers
	public float shieldMul;			//How effective against shields?
	public float lifeMul;			//How effective against life?
	
	private Sound snd;
	
	public Weapon(float damage, int cost, int cooldown, String particle, String name) {
		snd = new Sound("data/blaster.au");
		snd.play();
		this.damage = damage;
		this.cost = cost;
		this.cooldown = cooldown;
		this.particle = particle;
		this.name = name;
		shieldMul = 1.00f;
		lifeMul = 1.00f;
	}
}
