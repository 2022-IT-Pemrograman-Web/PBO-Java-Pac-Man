package pacman;

import java.net.URL;

public class EatGhostPower extends PowerUp{
    public EatGhostPower(int x, int y, URL urlImg) {
        super(x, y, urlImg);
    }

    @Override
    public void activatePower(Player p) {
        p.activateSuperPellet();
    }
}
