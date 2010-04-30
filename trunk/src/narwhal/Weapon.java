package narwhal;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import gameEngine.Log;
import gameEngine.ResourceMananger;

public class Weapon {
	public float energyDamage;
	public float damage;
	public int cost;
	public int cooldown;
	public int techCost;
	public String particle;
	public String name;
	public float slow;
	
	//Multipliers
	public float shieldMul;			//How effective against shields?
	public float lifeMul;			//How effective against life?
	
	public Weapon(String fileName) {
		
		try
		{
			BufferedReader parse = new BufferedReader(
					new InputStreamReader(
					ResourceMananger.getInputStream("data/weapons/" + fileName)));
			
			//Parse the ship file
			while(true)
			{
				String line = parse.readLine();
				
				//Reached end of file
				if(line == null) break;
				
				//Ignore comments
				if( line.startsWith("//") || line.equals("") ) continue;
				
				//Translate line into data
				if(line.startsWith("[NAME]:"))  			name = parse(line);
				else if(line.startsWith("[DAMAGE]:"))  		damage = Float.parseFloat(parse(line));
				else if(line.startsWith("[COST]:")) 	    cost = Integer.parseInt(parse(line));
				else if(line.startsWith("[PARTICLE]:"))  	particle = parse(line);
				else if(line.startsWith("[TECH]:"))  	    techCost = Integer.parseInt(parse(line));
				else if(line.startsWith("[COOLDOWN]:"))	    cooldown = Integer.parseInt(parse(line));
				else if(line.startsWith("[ENERGY_DAMAGE]:"))energyDamage = Float.parseFloat(parse(line));
				else if(line.startsWith("[SLOW]:"))  		slow = Float.parseFloat(parse(line));
				else if(line.startsWith("[SHIELD_MUL]:"))
				{
					//Translate percent to float
					String percent = parse(line);
					percent = percent.substring(0, percent.lastIndexOf('%'));
					shieldMul = Float.parseFloat(percent) / 100;
				}
				else if(line.startsWith("[LIFE_MUL]:"))
				{
					//Translate percent to float
					String percent = parse(line);
					percent = percent.substring(0, percent.lastIndexOf('%'));
					lifeMul = Float.parseFloat(percent) / 100;
				}
				else Log.warning("Loading particle file ( "+ fileName +") unrecognized line - " + line);
			}
			if( !ResourceMananger.fileExists("/data/particles/" + particle) )
			{
				throw new Exception("Invalid particle type: " + particle);
			}
		}
		catch( Exception e )
		{
			//Something went wrong
			Log.warning("Loading weapon (" + fileName + ") - " + e);
		}
	}
	
	/**
	 * JJ> This is simply to make parsing easier. Gets whatever is behind the colon and trims all
	 *     whitespace before and after the text.
	 * @param line The String to parse
	 * @return The parsed String
	 */
	private String parse(String line) {
		return line.substring(line.indexOf(':')+1).trim();
	}

}
