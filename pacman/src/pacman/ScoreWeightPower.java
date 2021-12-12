package pacman;

import java.net.URL;

public class ScoreWeightPower extends PowerUp{
    public ScoreWeightPower(int x, int y, URL urlImg) {
        super(x, y, urlImg);
    }
    
    public int activatePower(int scoreWeight) {
        return scoreWeight+1;
        //kalo double bahaya, 3 makanan gak ke ambil, 4 nembus tembok
    }
}
