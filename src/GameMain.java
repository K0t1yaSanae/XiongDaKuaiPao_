import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameMain extends JFrame {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private int playerX = 150;
    private int playerY = HEIGHT / 2;
    private int chaserX = 50;
    private int chaserY = HEIGHT / 2;
    private long mapOffset = 0;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private boolean isInGame = false;
    private final String gameOverText = "游戏结束！";

    private final List<Prop> props = new ArrayList<>();
    private boolean isInvincible = false;
    private boolean isSpeedUp = false;
    private long invincibleEndTime = 0;
    private long speedUpEndTime = 0;
    private String propTip = "";
    private long propTipEndTime = 0;

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
    private WelcomePanel welcomePanel;
    private JButton btnRestart;
    private JButton btnResume;
    private JButton btnCustomPlayer, btnCustomBackground, btnCustomChaser, btnCustomObstacle, btnStartGame;
    private JLabel gameOverLabel;

    private List<Rectangle> coins = new ArrayList<>();
    private int coinCount = 0;

    public GameMain() {
        setTitle("熊大快跑");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        setSize(WIDTH, HEIGHT);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setLocationRelativeTo(null);
        setLayout(null);

        initImages();
        initWelcomePanel();
        initGamePanel();
        initMainMenu();
        initGameOverPanel();

        add(welcomePanel);
        add(mainMenuPanel);
        add(gamePanel);
        add(gameOverPane);

        welcomePanel.setVisible(true);
        mainMenuPanel.setVisible(false);
        gamePanel.setVisible(false);
        gameOverPane.setVisible(false);
        isInGame = false;

        setVisible(true);

        new Thread(this::gameLoop).start();
    }

    private void initWelcomePanel() {
        welcomePanel = new WelcomePanel(this);
        welcomePanel.setBounds(0, 0, WIDTH, HEIGHT);
    }

    public void showMainMenu() {
        if (!isInGame) {
            mainMenuPanel.setVisible(true);
            welcomePanel.setVisible(false);
        }
    }

    private void startGame() {
        mainMenuPanel.setVisible(false);
        gamePanel.setVisible(true);
        gameOverPane.setVisible(false);
        gameStarted = true;
        gameOver = false;
        isInGame = true;
        resetPropState();
        initObstacles();
        initProps();
        gamePanel.requestFocus();
    }

    private void initGameOverPanel() {
        gameOverPane = new JPanel();
        gameOverPane.setBounds(WIDTH / 4, HEIGHT / 3, WIDTH / 2, 250);
        gameOverPane.setBackground(new Color(0, 0, 0, 230));
        gameOverPane.setLayout(new GridLayout(4, 1, 10, 10));
        gameOverPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gameOverPane.setVisible(false);
        gameOverPane.setOpaque(true);

        gameOverLabel = new JLabel(gameOverText, SwingConstants.CENTER);
        gameOverLabel.setForeground(Color.WHITE);
        gameOverLabel.setFont(FontLoader.getCustomFont(Font.BOLD, 28));

        btnRestart = new JButton("重新开始");
        btnRestart.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));
        btnRestart.setBackground(new Color(76, 175, 80));
        btnRestart.setForeground(Color.WHITE);
        btnRestart.setFocusPainted(false);
        btnRestart.addActionListener(e -> {
            playerX = 150;
            playerY = HEIGHT / 2;
            chaserX = 50;
            chaserY = HEIGHT / 2;
            mapOffset = 0;
            gameOver = false;
            gameStarted = false;
            isInGame = false;
            resetPropState();
            obstacles.clear();
            props.clear();
            coins.clear();
            coinCount = 0;

            gameOverPane.setVisible(false);
            gamePanel.setVisible(false);
            mainMenuPanel.setVisible(true);
        });

        btnResume = new JButton("复活继续");
        btnResume.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));
        btnResume.setBackground(new Color(255, 152, 0));
        btnResume.setForeground(Color.WHITE);
        btnResume.setFocusPainted(false);
        btnResume.addActionListener(e -> {
            long px = mapOffset + playerX;
            obstacles.removeIf(r -> r.x > px && r.x < px + 500);
            gameOver = false;
            gameOverPane.setVisible(false);
            gamePanel.requestFocus();
        });

        JLabel emptyLabel = new JLabel();

        gameOverPane.add(gameOverLabel);
        gameOverPane.add(btnRestart);
        gameOverPane.add(btnResume);
        gameOverPane.add(emptyLabel);

        add(gameOverPane);
    }

    private void gameLoop() {
        while (true) {
            if (gameStarted && !gameOver && isInGame) {
                autoMovePlayer();
                controlPlayer();
                chaserFollowPlayer();
                spawnObstacles();
                checkCoinPickup();
                checkPropPickup();
                if (!isInvincible) {
                    checkObstacleHit();
                }
            }
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException ignored) {}
        }
    }

    private void checkObstacleHit() {
        long pWorldX = mapOffset + playerX;
        Rectangle playerBox = new Rectangle((int) pWorldX, playerY, GameConstants.PLAYER_SIZE, GameConstants.PLAYER_SIZE);

        for (Rectangle r : obstacles) {
            if (playerBox.intersects(r)) {
                gameOver = true;
                gameOverPane.setVisible(true);
                gamePanel.requestFocusInWindow();
                return;
            }
        }
    }

    private void initMainMenu() {
        mainMenuPanel = new JPanel();
        mainMenuPanel.setBounds(0, 0, WIDTH, HEIGHT);
        mainMenuPanel.setBackground(GameConstants.MAIN_MENU_BACKGROUND);
        mainMenuPanel.setLayout(new GridLayout(6, 1, 20, 20));
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(60, 200, 60, 200));

        JLabel titleLabel = new JLabel("Auto Run Chase", SwingConstants.CENTER);
        titleLabel.setForeground(GameConstants.PROMPT_TEXT_COLOR);
        titleLabel.setFont(FontLoader.getCustomFont(Font.BOLD, 32));
        mainMenuPanel.add(titleLabel);

        btnCustomPlayer = new JButton("自定义玩家图标");
        btnCustomPlayer.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));
        btnCustomPlayer.addActionListener(e -> ImageLoader.customImage(this, "player", img -> playerImage = img));
        mainMenuPanel.add(btnCustomPlayer);

        btnCustomBackground = new JButton("自定义背景");
        btnCustomBackground.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));
        btnCustomBackground.addActionListener(e -> ImageLoader.customImage(this, "background", img -> backgroundImage = img));
        mainMenuPanel.add(btnCustomBackground);

        btnCustomChaser = new JButton("自定义追逐者图标");
        btnCustomChaser.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));
        btnCustomChaser.addActionListener(e -> ImageLoader.customImage(this, "chaser", img -> chaserImage = img));
        mainMenuPanel.add(btnCustomChaser);

        btnCustomObstacle = new JButton("自定义障碍物图标");
        btnCustomObstacle.setFont(FontLoader.getCustomFont(Font.PLAIN, 18));
        btnCustomObstacle.addActionListener(e -> ImageLoader.customImage(this, "obstacle", img -> obstacleImage = img));
        mainMenuPanel.add(btnCustomObstacle);

        btnStartGame = new JButton("开始游戏");
        btnStartGame.setFont(FontLoader.getCustomFont(Font.BOLD, 20));
        btnStartGame.setBackground(Color.GREEN);
        btnStartGame.setForeground(GameConstants.PROMPT_TEXT_COLOR);
        btnStartGame.setFocusPainted(false);
        btnStartGame.addActionListener(e -> startGame());
        mainMenuPanel.add(btnStartGame);
    }

    private void initImages() {
        playerImage = loadDefaultImage("pictures/1.png", GameConstants.PLAYER_SIZE);
        if (playerImage == null) {
            playerImage = ImageLoader.createDefaultImage(Color.CYAN, GameConstants.PLAYER_SIZE);
        }

        chaserImage = loadDefaultImage("pictures/2.png", GameConstants.CHASER_SIZE);
        if (chaserImage == null) {
            chaserImage = ImageLoader.createDefaultImage(Color.ORANGE, GameConstants.CHASER_SIZE);
        }

        backgroundImage = ImageLoader.createDefaultImage(Color.BLACK, GameConstants.WIDTH, GameConstants.HEIGHT);
        obstacleImage = ImageLoader.createDefaultImage(Color.RED, GameConstants.OBSTACLE_SIZE);
    }

    private Image loadDefaultImage(String path, int size) {
        try {
            java.net.URL imgUrl = getClass().getClassLoader().getResource(path);
            if (imgUrl == null) {
                imgUrl = new java.net.URL("file:" + path);
            }
            Image image = new ImageIcon(imgUrl).getImage();
            return image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            return null;
        }
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

    private void resetPropState() {
        isInvincible = false;
        isSpeedUp = false;
        invincibleEndTime = 0;
        speedUpEndTime = 0;
        propTip = "";
        propTipEndTime = 0;
        props.clear();
        coins.clear();
        coinCount = 0;
    }

    private void initObstacles() {
        obstacles.clear();
        coins.clear();
        long lastX = GameConstants.INIT_OBSTACLE_START_X;
        for (int i = 0; i < GameConstants.INIT_OBSTACLE_COUNT; i++) {
            lastX += GameConstants.OBSTACLE_MIN_GAP + rand.nextInt(50);
            int y = rand.nextInt(GameConstants.HEIGHT - GameConstants.OBSTACLE_SIZE);
            obstacles.add(new Rectangle((int) lastX, y, GameConstants.OBSTACLE_SIZE, GameConstants.OBSTACLE_SIZE));
            spawnPropIfChance((int) lastX);

            if (rand.nextBoolean()) {
                int cy = rand.nextInt(GameConstants.HEIGHT - 20);
                coins.add(new Rectangle((int) lastX + rand.nextInt(-30, 30), cy, 20, 20));
            }
        }
    }

    private void initProps() {
        props.clear();
        long lastX = GameConstants.INIT_OBSTACLE_START_X + 100;
        for (int i = 0; i < GameConstants.INIT_PROP_COUNT; i++) {
            lastX += GameConstants.OBSTACLE_MIN_GAP * 2 + rand.nextInt(100);
            int y = rand.nextInt(GameConstants.HEIGHT - GameConstants.PROP_SIZE);
            PropType type = PropType.values()[rand.nextInt(PropType.values().length)];
            props.add(new Prop((int) lastX, y, type));
        }
    }

    private void spawnPropIfChance(int x) {
        if (rand.nextInt(100) < GameConstants.PROP_SPAWN_CHANCE) {
            int y = rand.nextInt(GameConstants.HEIGHT - GameConstants.PROP_SIZE);
            PropType type = PropType.values()[rand.nextInt(PropType.values().length)];
            props.add(new Prop(x + rand.nextInt(50) + 20, y, type));
        }
    }

    private void spawnObstacles() {
        Iterator<Rectangle> it = obstacles.iterator();
        while (it.hasNext()) {
            Rectangle r = it.next();
            if (r.x < mapOffset - 200)
                it.remove();
        }

        Iterator<Prop> propIt = props.iterator();
        while (propIt.hasNext()) {
            Prop prop = propIt.next();
            if (prop.getX() < mapOffset - GameConstants.PROP_SIZE)
                propIt.remove();
        }

        Iterator<Rectangle> coinIt = coins.iterator();
        while (coinIt.hasNext()) {
            Rectangle c = coinIt.next();
            if (c.x < mapOffset - 50)
                coinIt.remove();
        }

        long maxX = obstacles.stream()
                .mapToLong(r -> (long) r.getX())
                .max()
                .orElse(GameConstants.INIT_OBSTACLE_START_X);
        int farRight = (int) (mapOffset + GameConstants.WIDTH + 300);
        if (maxX < farRight) {
            for (int i = 0; i < GameConstants.SPAWN_OBSTACLE_COUNT; i++) {
                long nx = maxX + GameConstants.OBSTACLE_MIN_GAP + rand.nextInt(50);
                int ny = rand.nextInt(GameConstants.HEIGHT - GameConstants.OBSTACLE_SIZE);
                obstacles.add(new Rectangle((int) nx, ny, GameConstants.OBSTACLE_SIZE, GameConstants.OBSTACLE_SIZE));
                spawnPropIfChance((int) nx);
                maxX = nx;

                if (rand.nextBoolean()) {
                    int cy = rand.nextInt(GameConstants.HEIGHT - 20);
                    coins.add(new Rectangle((int) nx + rand.nextInt(-30, 30), cy, 20, 20));
                }
            }
        }
    }

    private void checkPropPickup() {
        long currentTime = System.currentTimeMillis();
        for (Prop prop : props) {
            if (prop.isActive() && prop.isPicked(playerX, playerY, mapOffset)) {
                prop.setActive(false);
                PropType type = prop.getType();
                propTip = "拾取了" + type.getName() + "道具！";
                propTipEndTime = currentTime + 2000;

                switch (type) {
                    case INVINCIBLE:
                        isInvincible = true;
                        invincibleEndTime = currentTime + type.getDuration();
                        break;
                    case SPEED_UP:
                        isSpeedUp = true;
                        speedUpEndTime = currentTime + type.getDuration();
                        break;
                    case CLEAR_OBSTACLE:
                        clearAllObstacles();
                        break;
                }
            }
        }

        if (isInvincible && currentTime > invincibleEndTime) {
            isInvincible = false;
            propTip = "无敌效果结束！";
            propTipEndTime = currentTime + 2000;
        }
        if (isSpeedUp && currentTime > speedUpEndTime) {
            isSpeedUp = false;
            propTip = "加速效果结束！";
            propTipEndTime = currentTime + 2000;
        }
    }

    private void checkCoinPickup() {
        long px = mapOffset + playerX;
        Rectangle playerBox = new Rectangle((int) px, playerY, GameConstants.PLAYER_SIZE, GameConstants.PLAYER_SIZE);

        Iterator<Rectangle> it = coins.iterator();
        while (it.hasNext()) {
            Rectangle c = it.next();
            if (playerBox.intersects(c)) {
                it.remove();
                coinCount++;
            }
        }
    }

    private void clearAllObstacles() {
        obstacles.clear();
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            initObstacles();
        }).start();
    }

    private void autoMovePlayer() {
        int speed = isSpeedUp ? GameConstants.PLAYER_AUTO_SPEED_SPEED_UP : GameConstants.PLAYER_AUTO_SPEED;
        mapOffset += speed;
        chaserX += speed;
    }

    private void controlPlayer() {
        if (up && playerY > 0)
            playerY -= GameConstants.PLAYER_CONTROL_SPEED;
        if (down && playerY < GameConstants.HEIGHT - GameConstants.PLAYER_SIZE)
            playerY += GameConstants.PLAYER_CONTROL_SPEED;
        if (right && playerX < GameConstants.WIDTH / 2 + 50)
            playerX += GameConstants.PLAYER_CONTROL_SPEED;
    }

    private void chaserFollowPlayer() {
        if (chaserY < playerY - 10)
            chaserY += GameConstants.CHASER_FOLLOW_SPEED;
        if (chaserY > playerY + 10)
            chaserY -= GameConstants.CHASER_FOLLOW_SPEED;

        int targetChaserX = (int) (mapOffset + playerX - 100);
        if (chaserX < targetChaserX)
            chaserX += GameConstants.CHASER_FOLLOW_SPEED;
        if (chaserX > targetChaserX)
            chaserX -= GameConstants.CHASER_FOLLOW_SPEED;
    }

    public Image getPlayerImage() { return playerImage; }
    public Image getChaserImage() { return chaserImage; }
    public Image getBackgroundImage() { return backgroundImage; }
    public Image getObstacleImage() { return obstacleImage; }
    public List<Rectangle> getObstacles() { return obstacles; }
    public List<Prop> getProps() { return props; }
    public List<Rectangle> getCoins() { return coins; }
    public int getCoinCount() { return coinCount; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getChaserX() { return chaserX; }
    public int getChaserY() { return chaserY; }
    public long getMapOffset() { return mapOffset; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameStarted() { return gameStarted; }
    public boolean isInvincible() { return isInvincible; }
    public boolean isSpeedUp() { return isSpeedUp; }
    public String getPropTip() { return propTip; }
    public long getPropTipEndTime() { return propTipEndTime; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameMain().setVisible(true));
    }
}