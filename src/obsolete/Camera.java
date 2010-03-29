import gameEngine.GameObject;
import gameEngine.Vector;
import gameEngine.Video;


public class Camera {

	void drawBackground()
	{
		float uniX = Video.getScreenWidth()*universeSize;
		float uniY = Video.getScreenHeight()*universeSize;
		Vector pos = position.clone();
		
		boolean u = false;
		boolean d = false;
		boolean l = false;
		boolean r = false;
		
		if 		(pos.x < 0) 		  	l = true;
		else if (pos.x > uniX - Video.getScreenWidth()) 	r = true;
		
		if		(pos.y < 0)				u = true;
		else if (pos.y > uniY - Video.getScreenHeight())	d = true;
	
		pos.negate();
		
		drawSingleBackground(g, pos);
		
		if 		(l) drawSingleBackground(g, pos.plus(new Vector(-uniX,0)));
		else if (r) drawSingleBackground(g, pos.plus(new Vector( uniX,0)));
	
		if 		(u) drawSingleBackground(g, pos.plus(new Vector(0,-uniY)));
		else if (d) drawSingleBackground(g, pos.plus(new Vector(0, uniY)));
		
		if 		(u && l) drawSingleBackground(g, pos.plus(new Vector(-uniX,-uniY)));
		else if (u && r) drawSingleBackground(g, pos.plus(new Vector( uniX,-uniY)));
		else if (d && l) drawSingleBackground(g, pos.plus(new Vector(-uniX, uniY)));
		else if (d && r) drawSingleBackground(g, pos.plus(new Vector( uniX, uniY)));
	}

	private Vector isInFrame(GameObject entity){
		Vector topLeft  = entity.pos;
		Vector botRight = topLeft.plus(new Vector(entity.image.getWidth(), entity.image.getHeight())).returnOverflowWithin(universeBotRight);
		Vector botLeft  = topLeft.plus(new Vector(0, botRight.y)).returnOverflowWithin(universeBotRight);
		Vector topRight = topLeft.plus(new Vector(botRight.x, 0)).returnOverflowWithin(universeBotRight);
		
		if (topLeft.isInsideRect( cameraPos, cameraPos.plus(Video.getResolutionVector()))) return cameraPos;
		if (botRight.isInsideRect(cameraPos, cameraPos.plus(Video.getResolutionVector())))
		{
			if (topLeft.isTopLeftOf(cameraBotRight))
				return cameraPos;
			else
				return background.getUniverseSize().plus(cameraPos);
		}
		
		// Where is the bottom right part of the camera in relation to the top left
		Vector cameraRelation = cameraBotRight.minus(cameraPos);
		
		// Camera overlaps to the right
		if (cameraRelation.x < 0 && cameraRelation.y > 0)
		{
			// topLeft is inside right side
			if (topLeft.isBotLeftOf(cameraTopRight) && topLeft.isTopLeftOf(cameraBotRight)) 
				return (new Vector(universeBotRight.x, 0)).negated().plus(cameraPos);
			
			// botLeft is inside left side
			if (botLeft.isTopRightOf(cameraBotLeft) && botLeft.isBotRightOf(cameraPos)) 
			{
				if (topLeft.isTopRightOf(cameraBotLeft)) 
					return cameraPos;
				else 
					return (new Vector(0, universeBotRight.y)).plus(cameraPos);
			}
			
			// botLeft is inside right side TODO: test
			if (botLeft.isTopLeftOf(cameraBotRight) && botLeft.isBotLeftOf(cameraTopRight))
			{
				if (topLeft.isTopLeftOf(botRight))
					return (new Vector(universeBotRight.x, 0)).negated().plus(cameraPos);
				else
					return (new Vector(universeBotRight.x, -universeBotRight.y)).negated().plus(cameraPos);
			}
		}
		// Camera overlaps to the bottom
		if (cameraRelation.x > 0 && cameraRelation.y < 0)
		{
			// topLeft is inside bottom part
			if (topLeft.isTopLeftOf(cameraBotRight) && topLeft.isTopRightOf(cameraBotLeft)) 
				return (new Vector(0, universeBotRight.y)).negated().plus(cameraPos);
			
			// topRight is inside top part TODO test
			if (topRight.isBotLeftOf(cameraTopRight) && topRight.isBotRightOf(cameraPos))
			{
				if (topLeft.isBotLeftOf(cameraPos))
					return cameraPos;
				else
					return (new Vector(universeBotRight.x, 0)).plus(cameraPos);
			}
			
			// topRight is inside bottom part TODO test
			if (topRight.isTopLeftOf(cameraBotRight) && topRight.isTopRightOf(cameraBotLeft))
			{
				if (topLeft.isTopLeftOf(cameraBotLeft))
					return (new Vector(0, universeBotRight.y)).negated().plus(cameraPos);
				else
					return (new Vector(-universeBotRight.x, universeBotRight.y)).negated().plus(cameraPos);
			}
		}
		// Camera overlaps to the right and the bottom
		if (cameraRelation.x < 0 && cameraRelation.y < 0)
		{
			if (topLeft.isInsideRect(cameraBotRight)) 
				return background.getUniverseSize().negated().plus(cameraPos);
			
			if (topLeft.isTopRightOf(cameraBotLeft))  
				return (new Vector(0, background.getUniverseSize().y)).negated().plus(cameraPos);
			
			if (topLeft.isBotLeftOf(cameraTopRight))  
				return (new Vector(background.getUniverseSize().x, 0)).negated().plus(cameraPos);
			
			if (botRight.isTopLeftOf(cameraBotRight)) 
				return cameraPos;
			
			if (botLeft.isTopRightOf(cameraBotLeft))
				return cameraPos;
			
			if (topRight.isBotLeftOf(cameraTopRight))
				return cameraPos;
		}
		return null;
	}
	
	private void updateCameraVectors(){
		cameraPos.x = follow.pos.x - ((float)Video.getScreenWidth() / 2f) + follow.image.getWidth()/2;
		cameraPos.y = follow.pos.y - ((float)Video.getScreenHeight() / 2f) + follow.image.getHeight()/2;
		cameraPos.overflowWithin(background.getUniverseSize());
		calculateCameraOverflow();
		
		universeBotRight	= background.getUniverseSize();
		universeBotLeft		= new Vector(0, universeBotRight.y);
		universeTopRight	= new Vector(universeBotRight.x, 0);
		
		cameraBotRight		= cameraPos.plus(Video.getResolutionVector()).returnOverflowWithin(universeBotRight);
		cameraBotLeft		= cameraPos.plus(new Vector(0, Video.getResolutionVector().y)).returnOverflowWithin(universeBotRight);
		cameraTopRight		= cameraPos.plus(new Vector(Video.getResolutionVector().x, 0)).returnOverflowWithin(universeBotRight);
	}

	private void calculateCameraOverflow(){
		cameraOverflow = new Vector();
		
		if (cameraPos.x < 0) cameraOverflow.x -= 1;
		else if (cameraPos.x + Video.getScreenWidth() > background.getUniverseSize().x) cameraOverflow.x += 1;
		
		if (cameraPos.y < 0) cameraOverflow.y -= 1;
		else if (cameraPos.y + Video.getScreenHeight() > background.getUniverseSize().y) cameraOverflow.y += 1; 
	}
}
