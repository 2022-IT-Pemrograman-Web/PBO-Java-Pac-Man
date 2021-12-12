package pacman;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FontLoader {
    public static Font getFontFromFile(String fontname, float fontsize) {

        //case of error
        Font customFont = new Font("Arial",Font.PLAIN,0);

        try {
            //create the font to use. Specify the size!
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/" + fontname + ".ttf")).deriveFont(fontsize);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //register the font
            ge.registerFont(customFont);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(FontFormatException e) {
            e.printStackTrace();
        }
        return customFont;
    }
}
