package pacman;

//for image
import java.awt.Image;
//for image path
import java.net.URL;

import javax.swing.ImageIcon;

//Class for all the entity in the game
public class Entity extends GameObject{
	//every entity has a direction
	static enum Direction{
		LEFT,
		RIGHT,
		UP,
		DOWN,
		NEUTRAL
	}
	public Direction isFacing;
	protected int dx;
	protected int dy;
	
	//every entity has its SPEED
	protected int speed;
	
	//every entity has image for 4 directions: 0 : left, 1:right, 2:up, 3:down
	protected Image[] imgs = new Image[4];

	public Image getCurrentImage(){
		switch (isFacing){
			case LEFT:
				return imgs[0];
			case RIGHT:
				return imgs[1];
			case UP:
				return imgs[2];
			default:
				return imgs[3];
		}
	}

	
	//constructor
	public Entity(int x, int y, int dx, int dy, int speed) {
		//set / initialize the position direction, speed, and images
		super(x, y);
		this.dx = dx;
		this.dy = dy;
		this.speed = speed;
	}
	
	//every entity has an update movement
	public void updateMovement() {
		x = x + dx * speed;
		y = y + dy * speed;
	}
}
