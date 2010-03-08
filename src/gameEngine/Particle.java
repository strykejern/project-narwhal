package gameEngine;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Particle {	
	static ArrayList<Particle> particleList = new ArrayList<Particle>();
	static final int MAX_PARTICLES = 500;	
	private static HashMap<Integer, Image> particleMap;
	
	//Static functions, move these to somewhere else?
	static public void spawnParticle( Vector pos, String hash, int lifeTime ) {
		if( particleList.size() >= MAX_PARTICLES ) return;
		particleList.add( new Particle(pos, hash, lifeTime) );
	}
	
	//Static functions, move these to somewhere else?
	static public void drawAllParticles(Graphics g) {
		for( int p = 0; p < particleList.size(); p++ )
		{
			particleList.get(p).draw(g);
			if(particleList.get(p).time > 0) particleList.get(p).time--;
			else
			{
				particleList.remove(p);
				p--;
			}
		}
	}
	
	/**
	 * JJ> Loads all particle images into a hash map for later use
	 */
	static public void loadParticles() {
		File[] fileList = new File("data/particles").listFiles();
		
		//Make sure it is removed from memory
		if( particleMap != null ) particleMap.clear();
		
		//Load all particles into the hash map
		particleMap = new HashMap<Integer, Image>();
		for( File f : fileList )
		{
			if( !f.isFile() ) continue;
			Image2D loadGraphic = new Image2D( f.toString() );
			
			//Trim away the file extension (.jpg, .png, etc.)
			String fileName = f.toString();
			fileName = fileName.substring(0, fileName.length()-4 );
			
			//Put the image into a hash map
			particleMap.put( fileName.hashCode() , loadGraphic.toImage() );
		}
	}
	
	//Object functions
	static int hashCode;
	int time;
	Vector pos;
	boolean remove;
	Transparency type;
	
	enum Transparency {
		SOLID,			//0% alpha
		TRANS,			//50%
		LIGHT			//90%
	}
	
	public Particle( Vector spawnPos, String hash, int lifeTime ) {
		pos = spawnPos;
		time = lifeTime;
		hashCode = hash.hashCode();
	}
	
	private void draw(Graphics g) {
		Image image = particleMap.get(hashCode);
		g.drawImage(image, pos.getX() -image.getWidth(null)/2, pos.getY()-image.getHeight(null)/2, null);	
	}
}
