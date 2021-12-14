package pacman;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.net.URL;

public class Player extends Entity{
    private URL loadImage(String fileName){
        return getClass().getResource("/images/" + fileName);
    }
    private URL urlDown = loadImage("down.gif");
    private URL urlUp = loadImage("up.gif");
    private URL urlLeft = loadImage("left.gif");
    private URL urlRight = loadImage("right.gif");
    private int lives;
    public Direction bufferedDirection;
    public boolean canEatGhosts;

    public Player(int x, int y, int dx, int dy, int speed, int lives) {
        super(x, y, dx, dy, speed);
        this.lives = lives;
        bufferedDirection = isFacing = Direction.NEUTRAL;
        this.imgs[0] = new ImageIcon(urlLeft).getImage();
        this.imgs[1] = new ImageIcon(urlRight).getImage();
        this.imgs[2] = new ImageIcon(urlUp).getImage();
        this.imgs[3] = new ImageIcon(urlDown).getImage();
        canEatGhosts = true;
    }

    public boolean getInput(int key){
        boolean inputStatus = true;
        switch (key){
            case KeyEvent.VK_LEFT:
                bufferedDirection = Direction.LEFT;
                break;
            case KeyEvent.VK_RIGHT:
                bufferedDirection = Direction.RIGHT;
                break;
            case KeyEvent.VK_UP:
                bufferedDirection = Direction.UP;
                break;
            case KeyEvent.VK_DOWN:
                bufferedDirection = Direction.DOWN;
                break;
            default:
                inputStatus = false;
        }
        return inputStatus;
    }

    public void movePlayer(int ch){
        if (bufferedDirection != Direction.NEUTRAL) {
            if (!((bufferedDirection == Direction.LEFT && (ch & 1) != 0)
                    || (bufferedDirection == Direction.RIGHT && (ch & 4) != 0)
                    || (bufferedDirection == Direction.UP && (ch & 2) != 0)
                    || (bufferedDirection == Direction.DOWN && (ch & 8) != 0))) {
                switch (bufferedDirection){
                    case LEFT:
                        dx = -1;
                        dy = 0;
                        isFacing = Direction.LEFT;
                        break;
                    case RIGHT:
                        dx = 1;
                        dy = 0;
                        isFacing = Direction.RIGHT;
                        break;
                    case UP:
                        dx = 0;
                        dy = -1;
                        isFacing = Direction.UP;
                        break;
                    case DOWN:
                        dx = 0;
                        dy = 1;
                        isFacing = Direction.DOWN;
                        break;
                }
            }
        }
        if ((isFacing == Direction.LEFT && (ch & 1) != 0)
                || (isFacing == Direction.RIGHT && (ch & 4) != 0)
                || (isFacing == Direction.UP && (ch & 2) != 0)
                || (isFacing == Direction.DOWN && (ch & 8) != 0)) {
            dx = 0;
            dy = 0;
        }
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void decreaseLives(){
        this.lives-=1;
    }

    public void increaseLives(){
        this.lives+=1;
    }
}
