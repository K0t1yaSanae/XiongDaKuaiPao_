import java.awt.*;
import java.io.InputStream;
import java.net.URL;

public class FontLoader {
    private static Font customFont;

    public static Font loadCustomFont() {
        if (customFont != null) {
            return customFont; // 已加载，直接返回
        }

        try {
            URL fontUrl = FontLoader.class.getClassLoader().getResource("fonts/harmony.ttf");
            if (fontUrl == null) {
                fontUrl = new URL("file:fonts/harmony.ttf");
            }

            InputStream fontStream = fontUrl.openStream();
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

            return customFont;
        } catch (Exception e) {
            System.err.println("加载自定义字体失败，使用系统默认字体：" + e.getMessage());
            return new Font("Arial", Font.PLAIN, 12);
        }
    }

    public static Font getCustomFont(int style, int size) {
        Font baseFont = loadCustomFont();
        return baseFont.deriveFont(style, size);
    }
}