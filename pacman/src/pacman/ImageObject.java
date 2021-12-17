package pacman;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

public abstract class ImageObject extends GameObject{
    protected Image img;

    public ImageObject(int x, int y, URL urlImg) {
        super(x, y);
        this.img = new ImageIcon(urlImg).getImage();
    }
}
