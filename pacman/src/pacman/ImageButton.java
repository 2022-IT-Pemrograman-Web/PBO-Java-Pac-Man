package pacman;
import java.net.URL;

public class ImageButton extends ImageObject{
    private int returnValue;
    private int width, height;

    public ImageButton(int x, int y, int width, int height, URL urlImg, int returnValue){
        super(x, y, urlImg);
        this.width = width;
        this.height = height;
        this.returnValue = returnValue;
    }

    public boolean isClicked(int mx, int my){
        return (x <= mx && mx <= (x + width)) && (y <= my && my <= (y + height));
    }

    public int getReturnValue(){
        return returnValue;
    }
}
