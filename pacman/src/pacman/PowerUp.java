package pacman;

//for image
import javax.swing.*;
import java.awt.Image;
//for image path
import java.net.URL;
public class PowerUp extends ImageObject{

    public PowerUp(int x, int y, URL urlImg) {
        super(x, y, urlImg);
    }

    public int activatePower(int x){
        return x;
    }
    public void activatePower(Player p){
        return;
    }

    public boolean isCollided(GameObject go){
        if((go.x > (this.x - 24) && go.x < (this.x + 24) && go.y > (this.y - 24) && go.y < (this.y + 24))){
            return true;
        }
        return false;
    }
}
