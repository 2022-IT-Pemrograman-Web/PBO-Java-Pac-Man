package pacman;

import java.net.URL;

public class Player extends Entity{
    private int lives;

    public Player(int x, int y, int dx, int dy, int speed, URL urlLeft, URL urlRight, URL urlUp, URL urlDown, int lives) {
        super(x, y, dx, dy, speed, urlLeft, urlRight, urlUp, urlDown);
        this.lives = lives;
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
