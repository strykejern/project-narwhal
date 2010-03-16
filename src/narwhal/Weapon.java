package narwhal;

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
}
