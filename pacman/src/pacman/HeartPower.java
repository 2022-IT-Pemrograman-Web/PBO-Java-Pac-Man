package pacman;

import java.net.URL;

public class HeartPower extends PowerUp{
    public HeartPower(int x, int y, URL urlImg) {
        super(x, y, urlImg);
    }

    @Override
    public void activatePower(Player p) {
        p.setLives(p.getLives()+1);
    }
}
