import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class WelcomePanel extends JPanel {
    private final GameMain game;
    private Font welcomeFont;

    public WelcomePanel(GameMain game) {
        this.game = game;
        setBounds(0, 0, GameConstants.WIDTH, GameConstants.HEIGHT);
        setBackground(Color.BLACK);
        setLayout(null);

        loadWelcomeFont();

        new Timer(3000, e -> {
            setVisible(false);
            game.showMainMenu();
        }).start();
    }

    private void loadWelcomeFont() {
        try {
            java.net.URL fontUrl = WelcomePanel.class.getClassLoader().getResource("fonts/hyw.ttf");
            if (fontUrl == null) {
                fontUrl = new java.net.URL("file:fonts/hyw.ttf");
            }

            InputStream fontStream = fontUrl.openStream();
            welcomeFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(welcomeFont);
        } catch (Exception e) {
            System.err.println("加载欢迎页字体hyw.ttf失败，使用默认字体：" + e.getMessage());
            welcomeFont = new Font("Arial", Font.BOLD, 80);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        Font displayFont = welcomeFont.deriveFont(Font.BOLD, 80);
        g2.setFont(displayFont);

        FontMetrics fm = g2.getFontMetrics();
        String text = "hello";
        int x = (GameConstants.WIDTH - fm.stringWidth(text)) / 2;
        int y = (GameConstants.HEIGHT + fm.getAscent()) / 2 - fm.getDescent();

        g2.drawString(text, x, y);
    }
}