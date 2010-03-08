package gameEngine;

import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;

public class Particle {	
	static ArrayList<Particle> particleList = new ArrayList<Particle>();
	static final int MAX_PARTICLES = 500;
	int time;
	Vector pos;
	boolean remove;
	Image image;
	Transparency type;
	static Image2D fire = new Image2D("data/fire.png");
	
	enum Transparency {
		SOLID,			//0% alpha
		TRANS,			//50%
		LIGHT			//90%
	}
	
	public Particle( Vector spawnPos, Image whichImage, int lifeTime ) {
		image = whichImage;
		pos = spawnPos;
		time = lifeTime;
	}
	
	private void draw(Graphics g) {
		g.drawImage(image, pos.getX() -image.getWidth(null)/2, pos.getY()-image.getHeight(null)/2, null);	
	}
	
	static public void spawnParticle( Vector pos, Image img ) {
		if( particleList.size() >= MAX_PARTICLES ) return;
		particleList.add( new Particle(pos, fire.toImage(), 100) );
	}
	
	static public void drawAllParticles(Graphics g) {
		fire.setAlpha(0.1f);
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
}
