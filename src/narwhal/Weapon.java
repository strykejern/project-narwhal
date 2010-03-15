package narwhal;

public class Weapon {
	public float damage;
	public int cost;
	public int cooldown;
	public String particle;
	
	public Weapon(float damage, int cost, int cooldown, String particle) {
		this.damage = damage;
		this.cost = cost;
		this.cooldown = cooldown;
		this.particle = particle;		
	}
}
