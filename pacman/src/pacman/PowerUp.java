package pacman;

//for image
import javax.swing.*;
import java.awt.Image;
//for image path
import java.net.URL;
public class PowerUp extends GameObject{
    protected Image img;

    public PowerUp(int x, int y, URL urlImg) {
        super(x, y);
        this.img = new ImageIcon(urlImg).getImage();
    }

    public void activatePower(Entity en){
        return;
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
