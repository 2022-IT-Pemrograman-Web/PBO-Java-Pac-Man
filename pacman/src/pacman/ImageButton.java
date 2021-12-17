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
        System.out.println(String.format("this boi [%d] is on %d %d", returnValue,x, y));
        System.out.println(String.format("x: %d --> %d <-- %d", x, mx, x+width));
        System.out.println(String.format("y: %d --> %d <-- %d", y, my, y+height));
        System.out.println((x <= mx && mx <= (x + width)) && (y <= my && my <= (y + height)));
        return (x <= mx && mx <= (x + width)) && (y <= my && my <= (y + height));
    }

    public int getReturnValue(){
        return returnValue;
    }
}
