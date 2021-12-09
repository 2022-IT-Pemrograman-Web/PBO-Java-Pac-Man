package pacman;

//for image
import java.awt.Image;
//for image path
import java.net.URL;

import javax.swing.ImageIcon;

//Class for all the entity in the game
public class Entity {
	//every entity has a coordinate
	protected int x;
	protected int y;
	
	//every entity has a direction
	protected int dx;
	protected int dy;
	
	//every entity has its SPEED
	protected int speed;
	
	//every entity has image for 4 directions: 0 : left, 1:right, 2:up, 3:down
	protected Image[] imgs = new Image[4];
	
	//constructor
	public Entity(int x, int y, int dx, int dy, int speed, URL urlLeft, URL urlRight, URL urlUp, URL urlDown) {
		//set / initialize the position direction, speed, and images
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.speed = speed;
		this.imgs[0] = new ImageIcon(urlLeft).getImage();
		this.imgs[1] =new ImageIcon(urlRight).getImage();
		this.imgs[2] = new ImageIcon(urlUp).getImage();
		this.imgs[3] = new ImageIcon(urlDown).getImage();
	}
	
	//every entity has an update movement
	public void updateMovement() {
		x = x + dx * speed;
		y = y + dy * speed;
	}
}
