import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {
    private final GameMain game;

    public GamePanel(GameMain game) {
        this.game = game;
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
            g2.drawImage(game.getObstacleImage(), sx, r.y, GameMain.OBSTACLE_SIZE, GameMain.OBSTACLE_SIZE, null);
        }

        g2.drawImage(game.getPlayerImage(), game.getPlayerX(), game.getPlayerY(), GameMain.PLAYER_SIZE, GameMain.PLAYER_SIZE, null);

        int chaserScreenX = (int) (game.getChaserX() - game.getMapOffset());
        g2.drawImage(game.getChaserImage(), chaserScreenX, game.getChaserY(), GameMain.CHASER_SIZE, GameMain.CHASER_SIZE, null);

        if (!game.isGameOver() && game.isGameStarted()) {
            g2.setColor(Color.WHITE);
            g2.setFont(FontLoader.getCustomFont(Font.PLAIN, 16));
            g2.drawString("熊大快跑", 10, 20);
        }
    }
}