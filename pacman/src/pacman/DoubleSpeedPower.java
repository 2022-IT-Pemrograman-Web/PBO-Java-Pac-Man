package pacman;

import java.net.URL;

public class DoubleSpeedPower extends PowerUp{
    public DoubleSpeedPower(int x, int y, URL urlImg) {
        super(x, y, urlImg);
    }

    @Override
    public void activatePower(Player p) {
        p.speed+=4;
        //kalo double bahaya, 3 makanan gak ke ambil, 4 nembus tembok
    }
}
