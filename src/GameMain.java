import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameMain extends JFrame {

    //这边是张金辰aicode的狗屎主类。

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int PLAYER_SIZE = 28;
    public static final int CHASER_SIZE = 28;
    public static final int OBSTACLE_SIZE = 40;

    public static final int PLAYER_AUTO_SPEED = 2;
    public static final int PLAYER_CONTROL_SPEED = 3;
    public static final int CHASER_FOLLOW_SPEED = 2;
    public static final int OBSTACLE_MIN_GAP = 80;

    private int playerX = 150;
    private int playerY = HEIGHT / 2;
    private int chaserX = 50;
    private int chaserY = HEIGHT / 2;

    private long mapOffset = 0;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private final String gameOverText = "You were caught!";

    private Image playerImage;
    private Image chaserImage;
    private Image backgroundImage;
    private Image obstacleImage;

    private final List<Rectangle> obstacles = new ArrayList<>();
    private final Random rand = new Random();

    private boolean up, down, right;

    private JPanel mainMenuPanel;
    private JPanel gameOverPane;
    private GamePanel gamePanel;
    private JButton btnCustomPlayer, btnCustomBackground, btnCustomChaser, btnCustomObstacle, btnStartGame;
    private JButton btnRestart, btnResurrect;
    private JLabel gameOverLabel;

    public GameMain() {
        setTitle("熊大快跑");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        initImages();

        initGamePanel();
        initMainMenu();
        initGameOverPanel();

        add(mainMenuPanel);

        new Thread(this::gameLoop).start();
    }

    private void initImages() {
        playerImage = ImageLoader.createDefaultImage(Color.CYAN, PLAYER_SIZE);
        chaserImage = ImageLoader.createDefaultImage(Color.ORANGE, CHASER_SIZE);
        backgroundImage = ImageLoader.createDefaultImage(Color.BLACK, WIDTH, HEIGHT);
        obstacleImage = ImageLoader.createDefaultImage(Color.RED, OBSTACLE_SIZE);
    }

    private void initMainMenu() {
        mainMenuPanel = new JPanel();
        mainMenuPanel.setBounds(0, 0, WIDTH, HEIGHT);
        mainMenuPanel.setBackground(Color.DARK_GRAY);
        mainMenuPanel.setLayout(new GridLayout(6, 1, 20, 20));
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(60, 200, 60, 200));

        JLabel titleLabel = new JLabel("熊大快跑", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FontLoader.getCustomFont(Font.BOLD, 32));
        mainMenuPanel.add(titleLabel);

        btnCustomPlayer = new JButton("自定义玩家");
        btnCustomPlayer.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));
        btnCustomPlayer.addActionListener(e -> ImageLoader.customImage(this, "player", img -> playerImage = img));
        mainMenuPanel.add(btnCustomPlayer);

        btnCustomBackground = new JButton("自定义背景");
        btnCustomBackground.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));
        btnCustomBackground.addActionListener(e -> ImageLoader.customImage(this, "background", img -> backgroundImage = img));
        mainMenuPanel.add(btnCustomBackground);

        btnCustomChaser = new JButton("自定义追逐者");
        btnCustomChaser.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));
        btnCustomChaser.addActionListener(e -> ImageLoader.customImage(this, "chaser", img -> chaserImage = img));
        mainMenuPanel.add(btnCustomChaser);

        btnCustomObstacle = new JButton("自定义障碍物");
        btnCustomObstacle.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));
        btnCustomObstacle.addActionListener(e -> ImageLoader.customImage(this, "obstacle", img -> obstacleImage = img));
        mainMenuPanel.add(btnCustomObstacle);

        btnStartGame = new JButton("再来");
        btnStartGame.setFont(FontLoader.getCustomFont(Font.BOLD, 20));
        btnStartGame.setBackground(Color.GREEN);
        btnStartGame.setForeground(Color.WHITE);
        btnStartGame.addActionListener(e -> startGame());
        mainMenuPanel.add(btnStartGame);
    }

    private void initGameOverPanel() {
        gameOverPane = new JPanel();
        gameOverPane.setBounds(WIDTH / 4, HEIGHT / 3, WIDTH / 2, 220);
        gameOverPane.setBackground(new Color(0, 0, 0, 220));
        gameOverPane.setLayout(new GridLayout(3, 1, 10, 10));
        gameOverPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gameOverPane.setVisible(false);
        gameOverPane.setOpaque(true);

        gameOverLabel = new JLabel(gameOverText, SwingConstants.CENTER);
        gameOverLabel.setForeground(Color.WHITE);
        gameOverLabel.setFont(FontLoader.getCustomFont(Font.BOLD, 24));

        btnRestart = new JButton("Play Again");
        btnRestart.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));

        btnResurrect = new JButton("Resurrect");
        btnResurrect.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));

        btnRestart.addActionListener(e -> restartGame());
        btnResurrect.addActionListener(e -> resurrect());

        gameOverPane.add(gameOverLabel);
        gameOverPane.add(btnRestart);
        gameOverPane.add(btnResurrect);
        add(gameOverPane);
    }

    private void initGamePanel() {
        gamePanel = new GamePanel(this);
        gamePanel.setBounds(0, 0, WIDTH, HEIGHT);
        gamePanel.setVisible(false);
        add(gamePanel);

        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> up = true;
                    case KeyEvent.VK_S -> down = true;
                    case KeyEvent.VK_D -> right = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> up = false;
                    case KeyEvent.VK_S -> down = false;
                    case KeyEvent.VK_D -> right = false;
                }
            }
        });
    }

    private void startGame() {
        mainMenuPanel.setVisible(false);
        gamePanel.setVisible(true);
        gameOverPane.setVisible(false);
        gameStarted = true;
        gameOver = false;
        initObstacles();
        gamePanel.requestFocus();
    }

    private void initObstacles() {
        obstacles.clear();
        long lastX = 200;
        for (int i = 0; i < 12; i++) {
            lastX += OBSTACLE_MIN_GAP + rand.nextInt(50);
            int y = rand.nextInt(HEIGHT - OBSTACLE_SIZE);
            obstacles.add(new Rectangle((int) lastX, y, OBSTACLE_SIZE, OBSTACLE_SIZE));
        }
    }

    private void spawnObstacles() {
        Iterator<Rectangle> it = obstacles.iterator();
        while (it.hasNext()) {
            Rectangle r = it.next();
            if (r.x < mapOffset - 200)
                it.remove();
        }

        long maxX = obstacles.stream()
                .mapToLong(r -> (long) r.getX())
                .max()
                .orElse(GameConstants.INIT_OBSTACLE_START_X);

        int farRight = (int) (mapOffset + WIDTH + 300);
        if (maxX < farRight) {
            for (int i = 0; i < 2; i++) {
                long nx = maxX + OBSTACLE_MIN_GAP + rand.nextInt(50);
                int ny = rand.nextInt(HEIGHT - OBSTACLE_SIZE);
                obstacles.add(new Rectangle((int) nx, ny, OBSTACLE_SIZE, OBSTACLE_SIZE));
                maxX = nx;
            }
        }
    }

    private void gameLoop() {
        while (true) {
            if (gameStarted && !gameOver) {
                autoMovePlayer();
                controlPlayer();
                chaserFollowPlayer();
                spawnObstacles();
                checkObstacleHit();
            }
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException ignored) {}
        }
    }

    private void autoMovePlayer() {
        mapOffset += PLAYER_AUTO_SPEED;
        chaserX += PLAYER_AUTO_SPEED;
    }

    private void controlPlayer() {
        if (up && playerY > 0)
            playerY -= PLAYER_CONTROL_SPEED;
        if (down && playerY < HEIGHT - PLAYER_SIZE)
            playerY += PLAYER_CONTROL_SPEED;
        if (right && playerX < WIDTH / 2 + 50)
            playerX += PLAYER_CONTROL_SPEED;
    }

    private void chaserFollowPlayer() {
        if (chaserY < playerY - 10)
            chaserY += CHASER_FOLLOW_SPEED;
        if (chaserY > playerY + 10)
            chaserY -= CHASER_FOLLOW_SPEED;

        int targetChaserX = (int) (mapOffset + playerX - 100);
        if (chaserX < targetChaserX)
            chaserX += CHASER_FOLLOW_SPEED;
        if (chaserX > targetChaserX)
            chaserX -= CHASER_FOLLOW_SPEED;
    }

    private void checkObstacleHit() {
        long pWorldX = mapOffset + playerX;
        Rectangle playerBox = new Rectangle((int) pWorldX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        for (Rectangle r : obstacles) {
            if (playerBox.intersects(r)) {
                gameOver = true;
                gameOverPane.setVisible(true);
                gamePanel.requestFocusInWindow();
                return;
            }
        }
    }

    private void restartGame() {
        playerX = 150;
        playerY = HEIGHT / 2;
        chaserX = 50;
        chaserY = HEIGHT / 2;
        mapOffset = 0;
        gameOver = false;
        gameOverPane.setVisible(false);
        initObstacles();
        gamePanel.requestFocus();
    }

    private void resurrect() {
        long px = mapOffset + playerX;
        obstacles.removeIf(r -> r.x > px && r.x < px + 500);
        gameOver = false;
        gameOverPane.setVisible(false);
        gamePanel.requestFocus();
    }

    public Image getPlayerImage() { return playerImage; }
    public Image getChaserImage() { return chaserImage; }
    public Image getBackgroundImage() { return backgroundImage; }
    public Image getObstacleImage() { return obstacleImage; }
    public List<Rectangle> getObstacles() { return obstacles; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getChaserX() { return chaserX; }
    public int getChaserY() { return chaserY; }
    public long getMapOffset() { return mapOffset; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameStarted() { return gameStarted; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameMain().setVisible(true));
    }
}