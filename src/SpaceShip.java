//********************************************************************************************
//*
//*    This file is part of Project Narwhal.
//*
//*    Project Narwhal is free software: you can redistribute it and/or modify it
//*    under the terms of the GNU General Public License as published by
//*    the Free Software Foundation, either version 3 of the License, or
//*    (at your option) any later version.
//*
//*    Project Narwhal is distributed in the hope that it will be useful, but
//*    WITHOUT ANY WARRANTY; without even the implied warranty of
//*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//*    General Public License for more details.
//*
//*    You should have received a copy of the GNU General Public License
//*    along with Project Narwhal.  If not, see <http://www.gnu.org/licenses/>.
//*
//********************************************************************************************

public class SpaceShip {
	Vector speed;
	Vector pos;
	Image2D sprite;
	
	public SpaceShip()	{
		sprite = new Image2D("data/spaceship.png");
		sprite.resize(64, 64);
		speed = new Vector();
		pos = new Vector();
	}
	
	void Update() {
		pos.add(speed);
	}


}
