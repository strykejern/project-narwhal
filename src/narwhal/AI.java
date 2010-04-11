package narwhal;

import gameEngine.Input;
import gameEngine.Vector;

public class AI {
	public Input controller;
	private Spaceship target;
	public Spaceship us;
	
	public AI(Spaceship target) {
		controller = new Input();
		controller.setCameraPos(new Vector());
		this.target = target;
	}
	
	public void think() {
		Vector vDistance = target.getPosCentre().minus(us.getPosCentre());
		float fDistance = Math.abs(vDistance.x + vDistance.y);
		
		if(fDistance > 600)
		{
			controller.up = true;
			controller.down = false;
			controller.mosButton1 = true;
		}
		else
		{
			controller.up = false;
			controller.down = true;
		}
		
		controller.mousePos = target.getPosCentre();
		
	}

}
