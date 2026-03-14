import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {
    private final GameMain game;

    public GamePanel(GameMain game) {
        this.game = game;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(game.getBackgroundImage(), 0, 0, GameMain.WIDTH, GameMain.HEIGHT, null);

        List<Rectangle> obstacles = game.getObstacles();
        for (Rectangle r : obstacles) {
            int sx = (int) (r.x - game.getMapOffset());
            g2.drawImage(game.getObstacleImage(), sx, r.y, GameConstants.OBSTACLE_SIZE, GameConstants.OBSTACLE_SIZE, null);
        }

        List<Prop> props = game.getProps();
        for (Prop prop : props) {
            prop.draw(g2, game.getMapOffset());
        }

        if (game.isInvincible()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime % 500 < 250) {
                g2.setColor(new Color(255, 255, 0, 100));
                g2.fillOval(game.getPlayerX(), game.getPlayerY(), GameConstants.PLAYER_SIZE + 5, GameConstants.PLAYER_SIZE + 5);
            }
        }
        g2.drawImage(game.getPlayerImage(), game.getPlayerX(), game.getPlayerY(), GameConstants.PLAYER_SIZE, GameConstants.PLAYER_SIZE, null);

        int chaserScreenX = (int) (game.getChaserX() - game.getMapOffset());
        g2.drawImage(game.getChaserImage(), chaserScreenX, game.getChaserY(), GameConstants.CHASER_SIZE, GameConstants.CHASER_SIZE, null);

        drawPropTip(g2);
        drawPropState(g2);
        if (!game.isGameOver() && game.isGameStarted()) {
            g2.setColor(GameConstants.PROMPT_TEXT_COLOR);
            g2.setFont(FontLoader.getCustomFont(Font.PLAIN, 16));
            g2.drawString("W/S/D 移动 | 躲避障碍物", 10, 20);
        }
    }

    private void drawPropTip(Graphics2D g2) {
        String tip = game.getPropTip();
        long currentTime = System.currentTimeMillis();
        if (!tip.isEmpty() && currentTime < game.getPropTipEndTime()) {
            g2.setColor(GameConstants.PROP_TIP_COLOR);
            g2.setFont(FontLoader.getCustomFont(Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            int x = (GameMain.WIDTH - fm.stringWidth(tip)) / 2;
            int y = GameMain.HEIGHT / 2 - 50;
            g2.drawString(tip, x, y);
        }
    }

    private void drawPropState(Graphics2D g2) {
        g2.setColor(GameConstants.PROMPT_TEXT_COLOR);
        g2.setFont(FontLoader.getCustomFont(Font.PLAIN, 14));
        int y = 40;
        if (game.isInvincible()) {
            g2.drawString("无敌状态中...", 10, y);
            y += 20;
        }
        if (game.isSpeedUp()) {
            g2.drawString("加速状态中...", 10, y);
        }
    }
}