package pacman;

import java.net.URL;

public class Ghost extends Entity{

    int[] dy_,dx_;

    public Ghost(int x, int y, int dx, int dy, int speed, URL urlLeft, URL urlRight, URL urlUp, URL urlDown){
        super(x,y,dx,dy,speed,urlLeft,urlRight,urlUp,urlDown);
        dx_ = new int[4];
        dy_ = new int[4];
        isFacing = Direction.LEFT;
    }

    public boolean detectPlayerCollision(Player pacman){
        if(pacman != null){
            return pacman.x > (x - 24) && pacman.x < (x + 24)
                    && pacman.y > (y - 24) && pacman.y < (y + 24);
        }
        else{
            System.out.println("Player object null!");
        }
        return false;
    }

    public int getScreenPos(int gridSize, int gridN){
        return x/gridSize + gridN * (y/gridSize);
    }

    public void moveGhost(short screenData){
        int count;

        count = 0;

        if ((screenData & 1) == 0 && dx != 1) {
            dx_[count] = -1;
            dy_[count] = 0;
            count++;
        }

        if ((screenData & 2) == 0 && dy != 1) {
            dx_[count] = 0;
            dy_[count] = -1;
            count++;
        }

        if ((screenData & 4) == 0 && dx != -1) {
            dx_[count] = 1;
            dy_[count] = 0;
            count++;
        }

        if ((screenData & 8) == 0 && dy != -1) {
            dx_[count] = 0;
            dy_[count] = 1;
            count++;
        }

        if (count == 0) {

            if ((screenData & 15) == 15) {
                dx = 0;
                dx = 0;
            } else {
                dx = -dx;
                dy = -dy;
            }

        } else {

            count = (int) (Math.random() * count);

            if (count > 3) {
                count = 3;
            }

            dx = dx_[count];
            dy = dy_[count];
        }

        //update ghost facing direction
        if(dx < 0) isFacing = Direction.LEFT;
        if(dx > 0) isFacing = Direction.RIGHT;
        if(dy < 0) isFacing = Direction.UP;
        if(dy > 0) isFacing = Direction.DOWN;
    }
}
