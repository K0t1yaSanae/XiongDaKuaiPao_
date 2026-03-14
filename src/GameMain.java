import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameMain extends JFrame {
    // 游戏常量（全局统一，供所有文件使用）
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int PLAYER_SIZE = 28;
    public static final int CHASER_SIZE = 28;
    public static final int OBSTACLE_SIZE = 40;

    public static final int PLAYER_AUTO_SPEED = 2;
    public static final int PLAYER_CONTROL_SPEED = 3;
    public static final int CHASER_FOLLOW_SPEED = 2;
    public static final int OBSTACLE_MIN_GAP = 80;

    // 游戏状态
    private int playerX = 150;
    private int playerY = HEIGHT / 2;
    private int chaserX = 50;
    private int chaserY = HEIGHT / 2;

    private long mapOffset = 0;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private final String gameOverText = "You were caught!";

    // 图片资源（关联ImageLoader工具类）
    private Image playerImage;
    private Image chaserImage;
    private Image backgroundImage;
    private Image obstacleImage;

    // 障碍物列表
    private final List<Rectangle> obstacles = new ArrayList<>();
    private final Random rand = new Random();

    // 按键状态（移除left，禁止A键后退）
    private boolean up, down, right;

    // UI组件（关联UI相关子模块）
    private JPanel mainMenuPanel;
    private JPanel gameOverPane;
    private GamePanel gamePanel;
    private JButton btnCustomPlayer, btnCustomBackground, btnCustomChaser, btnCustomObstacle, btnStartGame;
    private JButton btnRestart, btnResurrect;
    private JLabel gameOverLabel;

    public GameMain() {
        // 初始化窗口
        setTitle("熊大快跑");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        // 初始化图片（使用工具类加载默认图）
        initImages();

        // 初始化UI组件（按功能拆分初始化）
        initGamePanel();
        initMainMenu();
        initGameOverPanel();

        // 默认显示主界面
        add(mainMenuPanel);

        // 启动游戏循环
        new Thread(this::gameLoop).start();
    }

    private void initImages() {
        playerImage = ImageLoader.createDefaultImage(Color.CYAN, PLAYER_SIZE);
        chaserImage = ImageLoader.createDefaultImage(Color.ORANGE, CHASER_SIZE);
        backgroundImage = ImageLoader.createDefaultImage(Color.BLACK, WIDTH, HEIGHT);
        obstacleImage = ImageLoader.createDefaultImage(Color.RED, OBSTACLE_SIZE);
    }

    /**
     * 初始化主界面（自定义按钮+开始游戏按钮）
     */
    private void initMainMenu() {
        mainMenuPanel = new JPanel();
        mainMenuPanel.setBounds(0, 0, WIDTH, HEIGHT);
        mainMenuPanel.setBackground(Color.DARK_GRAY);
        mainMenuPanel.setLayout(new GridLayout(6, 1, 20, 20));
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(60, 200, 60, 200));

        // 标题
        JLabel titleLabel = new JLabel("熊大快跑", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        mainMenuPanel.add(titleLabel);

        // 自定义玩家按钮
        btnCustomPlayer = new JButton("Custom Player Image");
        btnCustomPlayer.setFont(new Font("Arial", Font.PLAIN, 18));
        btnCustomPlayer.addActionListener(e -> ImageLoader.customImage(this, "player", img -> playerImage = img));
        mainMenuPanel.add(btnCustomPlayer);

        // 自定义背景按钮
        btnCustomBackground = new JButton("Custom Background");
        btnCustomBackground.setFont(new Font("Arial", Font.PLAIN, 18));
        btnCustomBackground.addActionListener(e -> ImageLoader.customImage(this, "background", img -> backgroundImage = img));
        mainMenuPanel.add(btnCustomBackground);

        // 自定义追逐者按钮
        btnCustomChaser = new JButton("Custom Chaser Image");
        btnCustomChaser.setFont(new Font("Arial", Font.PLAIN, 18));
        btnCustomChaser.addActionListener(e -> ImageLoader.customImage(this, "chaser", img -> chaserImage = img));
        mainMenuPanel.add(btnCustomChaser);

        // 自定义障碍物按钮
        btnCustomObstacle = new JButton("Custom Obstacle Image");
        btnCustomObstacle.setFont(new Font("Arial", Font.PLAIN, 18));
        btnCustomObstacle.addActionListener(e -> ImageLoader.customImage(this, "obstacle", img -> obstacleImage = img));
        mainMenuPanel.add(btnCustomObstacle);

        // 开始游戏按钮
        btnStartGame = new JButton("Start Game");
        btnStartGame.setFont(new Font("Arial", Font.BOLD, 20));
        btnStartGame.setBackground(Color.GREEN);
        btnStartGame.setForeground(Color.WHITE);
        btnStartGame.addActionListener(e -> startGame());
        mainMenuPanel.add(btnStartGame);
    }

    /**
     * 初始化游戏结束界面
     */
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
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 24));

        btnRestart = new JButton("Play Again");
        btnResurrect = new JButton("Resurrect");

        btnRestart.addActionListener(e -> restartGame());
        btnResurrect.addActionListener(e -> resurrect());

        gameOverPane.add(gameOverLabel);
        gameOverPane.add(btnRestart);
        gameOverPane.add(btnResurrect);
        add(gameOverPane);
    }

    /**
     * 初始化游戏面板（绘制游戏元素，单独拆分类）
     */
    private void initGamePanel() {
        gamePanel = new GamePanel(this);
        gamePanel.setBounds(0, 0, WIDTH, HEIGHT);
        gamePanel.setVisible(false);
        add(gamePanel);

        // 键盘监听
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

    /**
     * 开始游戏
     */
    private void startGame() {
        mainMenuPanel.setVisible(false);
        gamePanel.setVisible(true);
        gameOverPane.setVisible(false);
        gameStarted = true;
        gameOver = false;
        initObstacles();
        gamePanel.requestFocus();
    }

    /**
     * 初始化障碍物
     */
    private void initObstacles() {
        obstacles.clear();
        long lastX = 200;
        for (int i = 0; i < 12; i++) {
            lastX += OBSTACLE_MIN_GAP + rand.nextInt(50);
            int y = rand.nextInt(HEIGHT - OBSTACLE_SIZE);
            obstacles.add(new Rectangle((int) lastX, y, OBSTACLE_SIZE, OBSTACLE_SIZE));
        }
    }

    /**
     * 生成新障碍物（无限地图）
     */
    private void spawnObstacles() {
        // 移除超出视野左侧的障碍物（原有逻辑不变）
        Iterator<Rectangle> it = obstacles.iterator();
        while (it.hasNext()) {
            Rectangle r = it.next();
            if (r.x < mapOffset - 200)
                it.remove();
        }

        // 修复：将double类型的x坐标显式转换为long
        long maxX = obstacles.stream()
                .mapToLong(r -> (long) r.getX()) // 关键修改：手动转换为long
                .max()
                .orElse(GameConstants.INIT_OBSTACLE_START_X); // 或原200

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

    /**
     * 游戏主循环
     */
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

    /**
     * 玩家自动向前移动
     */
    private void autoMovePlayer() {
        mapOffset += PLAYER_AUTO_SPEED;
        chaserX += PLAYER_AUTO_SPEED;
    }

    /**
     * 玩家手动控制
     */
    private void controlPlayer() {
        if (up && playerY > 0)
            playerY -= PLAYER_CONTROL_SPEED;
        if (down && playerY < HEIGHT - PLAYER_SIZE)
            playerY += PLAYER_CONTROL_SPEED;
        if (right && playerX < WIDTH / 2 + 50)
            playerX += PLAYER_CONTROL_SPEED;
    }

    /**
     * 追逐者跟随玩家
     */
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

    /**
     * 障碍物碰撞检测
     */
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

    /**
     * 重来一局
     */
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

    /**
     * 复活
     */
    private void resurrect() {
        long px = mapOffset + playerX;
        obstacles.removeIf(r -> r.x > px && r.x < px + 500);
        gameOver = false;
        gameOverPane.setVisible(false);
        gamePanel.requestFocus();
    }

    // 提供getter方法，供子模块（GamePanel）访问所需资源
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

    // 程序入口
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameMain().setVisible(true));
    }
}