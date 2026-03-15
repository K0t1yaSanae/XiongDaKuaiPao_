import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// 核心游戏类
public class GameMain extends JFrame {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private static final int CLEAR_RANGE = 150;

    private boolean gameOver = false;
    private boolean gameStarted = false;
    private boolean isInGame = false;
    private int reviveCount = 0;
    private boolean has5thSkin = false;

    private boolean upPressed = false;
    private boolean downPressed = false;

    private int playerX = 150;
    private int playerY = HEIGHT / 2;
    private int chaserX = 50;
    private int chaserY = HEIGHT / 2;
    private long mapOffset = 0;
    private Image playerImage;
    private Image chaserImage; // 追捕者皮肤

    private List<Rectangle> obstacles = new ArrayList<>();
    private List<Rectangle> coins = new ArrayList<>();
    private int coinCount = 0;
    private int totalCoinCount = 0;
    private Random rand = new Random();

    private int skinLevel = 1;
    private final int[] SKIN_COSTS = {0, 50, 100, 200, 100};

    private JPanel mainMenuPanel;
    private JPanel gameOverPane;
    private GamePanel gamePanel;
    private JButton btnStartGame;
    private JButton btnShop;
    private JButton btnRecharge;
    private JLabel totalCoinLabel;
    private JLabel gameOverLabel;
    private JLabel scoreLabel;
    private JLabel coinTipLabel;
    private JButton btnRestart;
    private JButton btnResume;

    private JPanel shopPanel;
    private JLabel shopTitle;
    private JButton[] skinButtons = new JButton[5];
    private JLabel[] skinTips = new JLabel[5];

    private Font defaultFont = new Font("微软雅黑", Font.PLAIN, 12);

    public GameMain() {
        loadCoinAndSkinData();
        initImages();
        setTitle("Java牛逼");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setLayout(null);

        initMainMenu();
        initGamePanel();
        initGameOverPanel();
        initShopPanel();

        add(mainMenuPanel);
        add(gamePanel);
        add(gameOverPane);
        add(shopPanel);

        mainMenuPanel.setVisible(true);
        gamePanel.setVisible(false);
        gameOverPane.setVisible(false);
        shopPanel.setVisible(false);

        setVisible(true);
        new Thread(this::gameLoop).start();
    }

    // ====================== 充值系统 ======================
    private void showRechargePanel() {
        JDialog dialog = new JDialog(this, "金币充值", true);
        dialog.setLayout(new FlowLayout());
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);

        JLabel label = new JLabel("选择充值金额：");
        dialog.add(label);

        String[] items = {"100金币 - 1元", "500金币 - 5元", "1000金币 - 10元", "5000金币 - 40元"};
        JComboBox<String> combo = new JComboBox<>(items);
        dialog.add(combo);

        JButton confirm = new JButton("确认充值");
        confirm.addActionListener(e -> {
            int index = combo.getSelectedIndex();
            int add = 0;
            switch (index) {
                case 0 -> add = 100;
                case 1 -> add = 500;
                case 2 -> add = 1000;
                case 3 -> add = 5000;
            }
            totalCoinCount += add;
            updateTotalCoinLabel();
            saveGameData();
            JOptionPane.showMessageDialog(dialog, "充值成功！获得 " + add + " 金币");
            dialog.dispose();
        });

        JButton cancel = new JButton("取消");
        cancel.addActionListener(e -> dialog.dispose());

        dialog.add(confirm);
        dialog.add(cancel);
        dialog.setVisible(true);
    }

    // ====================== 复活清理障碍物 ======================
    private void clearObstaclesAroundPlayer() {
        long playerAbsX = mapOffset + playerX;
        int playerAbsY = playerY;

        Iterator<Rectangle> obstacleIt = obstacles.iterator();
        while (obstacleIt.hasNext()) {
            Rectangle obs = obstacleIt.next();
            double dx = Math.abs(obs.getX() - playerAbsX);
            double dy = Math.abs(obs.getY() - playerAbsY);
            if (dx <= CLEAR_RANGE && dy <= CLEAR_RANGE) {
                obstacleIt.remove();
            }
        }
    }

    // ====================== 皮肤加载（修复点1） ======================
    private void initImages() {
        playerImage = createDefaultBallSkin(Color.RED, 50);
        updateChaserSkin(); // 一开始就加载追捕者皮肤
    }

    private Image createDefaultBallSkin(Color color, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(color);
        g2.fillOval(0, 0, size, size);
        g2.setColor(Color.BLACK);
        g2.drawOval(0, 0, size, size);
        g2.dispose();
        return img;
    }

    // 追捕者皮肤更新（确保每次买皮肤都刷新）
    private void updateChaserSkin() {
        switch (skinLevel) {
            case 1 -> chaserImage = createDefaultBallSkin(Color.ORANGE, 60);
            case 2 -> chaserImage = loadImage("pictures/2.png", 60);
            case 3 -> chaserImage = loadImage("pictures/1.png", 60);
            case 4 -> chaserImage = loadImage("pictures/11.png", 60);
            case 5 -> chaserImage = loadImage("pictures/12.png", 60);
            default -> chaserImage = createDefaultBallSkin(Color.ORANGE, 60);
        }
    }

    private Image loadImage(String path, int size) {
        try {
            return Toolkit.getDefaultToolkit().getImage(path).getScaledInstance(size, size, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            return createDefaultBallSkin(Color.ORANGE, size);
        }
    }

    private void initGamePanel() {
        gamePanel = new GamePanel(this);
        gamePanel.setBounds(0, 0, WIDTH, HEIGHT);
        gamePanel.setVisible(false);
        gamePanel.setFocusable(true);

        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> upPressed = true;
                    case KeyEvent.VK_S -> downPressed = true;
                    case KeyEvent.VK_D -> playerX = Math.min(WIDTH / 2 + 50, playerX + 8);
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> upPressed = false;
                    case KeyEvent.VK_S -> downPressed = false;
                }
            }
        });
    }

    private void updatePlayerMove() {
        if (gameOver) return;
        if (upPressed) playerY = Math.max(0, playerY - 6);
        if (downPressed) playerY = Math.min(HEIGHT - 50, playerY + 6);
    }

    private void initMainMenu() {
        mainMenuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(30, 30, 60));
                g.fillRect(0, 0, WIDTH, HEIGHT);
            }
        };
        mainMenuPanel.setBounds(0, 0, WIDTH, HEIGHT);
        mainMenuPanel.setLayout(null);

        JPanel centerPanel = new JPanel();
        centerPanel.setBounds(0, 50, WIDTH, HEIGHT - 100);
        centerPanel.setLayout(new GridLayout(4, 1, 20, 20));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(80, 250, 80, 250));
        centerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Auto Run Chase", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(getCustomFont(Font.BOLD, 32));
        centerPanel.add(titleLabel);

        btnStartGame = new JButton("开始游戏");
        btnStartGame.setFont(getCustomFont(Font.BOLD, 20));
        btnStartGame.setBackground(Color.GREEN);
        btnStartGame.setForeground(Color.WHITE);
        btnStartGame.setFocusPainted(false);
        btnStartGame.addActionListener(e -> startGame());
        centerPanel.add(btnStartGame);

        btnShop = new JButton("皮肤商店");
        btnShop.setFont(getCustomFont(Font.BOLD, 20));
        btnShop.setBackground(new Color(255, 215, 0));
        btnShop.setForeground(Color.BLACK);
        btnShop.setFocusPainted(false);
        btnShop.addActionListener(e -> showShopPanel());
        centerPanel.add(btnShop);

        btnRecharge = new JButton("金币充值");
        btnRecharge.setFont(getCustomFont(Font.BOLD, 20));
        btnRecharge.setBackground(Color.PINK);
        btnRecharge.setForeground(Color.BLACK);
        btnRecharge.setFocusPainted(false);
        btnRecharge.addActionListener(e -> showRechargePanel());
        centerPanel.add(btnRecharge);

        totalCoinLabel = new JLabel("总金币: " + totalCoinCount, SwingConstants.CENTER);
        totalCoinLabel.setForeground(Color.YELLOW);
        totalCoinLabel.setFont(getCustomFont(Font.BOLD, 20));
        totalCoinLabel.setBounds(WIDTH - 200, 20, 150, 30);

        mainMenuPanel.add(centerPanel);
        mainMenuPanel.add(totalCoinLabel);
    }

    private void initGameOverPanel() {
        gameOverPane = new JPanel();
        gameOverPane.setBounds(WIDTH / 4, HEIGHT / 3, WIDTH / 2, 300);
        gameOverPane.setBackground(new Color(0, 0, 0, 230));
        gameOverPane.setLayout(new GridLayout(6, 1, 10, 10));
        gameOverPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gameOverPane.setVisible(false);

        gameOverLabel = new JLabel("", SwingConstants.CENTER);
        gameOverLabel.setForeground(Color.WHITE);
        gameOverLabel.setFont(getCustomFont(Font.BOLD, 28));

        scoreLabel = new JLabel("", SwingConstants.CENTER);
        scoreLabel.setForeground(Color.YELLOW);
        scoreLabel.setFont(getCustomFont(Font.PLAIN, 20));

        coinTipLabel = new JLabel("", SwingConstants.CENTER);
        coinTipLabel.setForeground(Color.ORANGE);
        coinTipLabel.setFont(getCustomFont(Font.PLAIN, 16));

        btnRestart = new JButton("重新开始");
        btnRestart.setFont(getCustomFont(Font.PLAIN, 18));
        btnRestart.setBackground(new Color(76, 175, 80));
        btnRestart.setForeground(Color.WHITE);
        btnRestart.setFocusPainted(false);
        btnRestart.addActionListener(e -> {
            resetGameState();
            gameOverPane.setVisible(false);
            gamePanel.setVisible(false);
            mainMenuPanel.setVisible(true);
            shopPanel.setVisible(false);
            updateTotalCoinLabel();
        });

        btnResume = new JButton("复活继续 (50金币)");
        btnResume.setFont(getCustomFont(Font.PLAIN, 18));
        btnResume.setBackground(new Color(255, 152, 0));
        btnResume.setForeground(Color.WHITE);
        btnResume.setFocusPainted(false);
        btnResume.addActionListener(e -> {
            if (has5thSkin && reviveCount == 0) {
                reviveCount++;
                gameOver = false;
                clearObstaclesAroundPlayer();
                gameOverPane.setVisible(false);
                gamePanel.requestFocus();
                JOptionPane.showMessageDialog(this, "5级皮肤特权！免费复活1次", "恭喜", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (totalCoinCount >= 50) {
                    totalCoinCount -= 50;
                    gameOver = false;
                    clearObstaclesAroundPlayer();
                    gameOverPane.setVisible(false);
                    gamePanel.requestFocus();
                    updateTotalCoinLabel();
                    saveGameData();
                    JOptionPane.showMessageDialog(this, "复活成功！消耗50金币", "提示", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    coinTipLabel.setText("金币不足！需要50金币");
                }
            }
        });

        gameOverPane.add(gameOverLabel);
        gameOverPane.add(scoreLabel);
        gameOverPane.add(coinTipLabel);
        gameOverPane.add(btnRestart);
        gameOverPane.add(btnResume);
        gameOverPane.add(new JLabel());
    }

    private void initShopPanel() {
        shopPanel = new JPanel();
        shopPanel.setBounds(0, 0, WIDTH, HEIGHT);
        shopPanel.setBackground(new Color(30, 30, 60));
        shopPanel.setLayout(null);
        shopPanel.setVisible(false);

        shopTitle = new JLabel("皮肤商店", SwingConstants.CENTER);
        shopTitle.setForeground(Color.WHITE);
        shopTitle.setFont(getCustomFont(Font.BOLD, 32));
        shopTitle.setBounds(0, 50, WIDTH, 50);
        shopPanel.add(shopTitle);

        String[] skinNames = {
                "1级：默认小球（免费）",
                "2级：进阶皮肤（50金币）",
                "3级：高级皮肤（100金币）",
                "4级：史诗皮肤（200金币）",
                "5级：传说皮肤（100金币）- 解锁复活特权+称号"
        };
        int yStart = 150;
        for (int i = 0; i < 5; i++) {
            skinButtons[i] = new JButton(skinNames[i]);
            skinButtons[i].setFont(getCustomFont(Font.PLAIN, 16));
            skinButtons[i].setBounds(200, yStart + i * 80, 400, 40);
            skinButtons[i].setFocusPainted(false);
            int finalI = i;
            skinButtons[i].addActionListener(e -> buySkin(finalI + 1));

            skinTips[i] = new JLabel();
            skinTips[i].setForeground(Color.WHITE);
            skinTips[i].setFont(getCustomFont(Font.PLAIN, 14));
            skinTips[i].setBounds(200, yStart + i * 80 + 45, 400, 20);
            updateSkinTip(finalI + 1);

            shopPanel.add(skinButtons[i]);
            shopPanel.add(skinTips[i]);
        }

        JButton backBtn = new JButton("返回主界面");
        backBtn.setFont(getCustomFont(Font.PLAIN, 18));
        backBtn.setBackground(new Color(153, 102, 255));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBounds(350, yStart + 5 * 80, 150, 40);
        backBtn.addActionListener(e -> {
            shopPanel.setVisible(false);
            mainMenuPanel.setVisible(true);
        });
        shopPanel.add(backBtn);
    }

    private void showShopPanel() {
        mainMenuPanel.setVisible(false);
        shopPanel.setVisible(true);
        for (int i = 1; i <= 5; i++) updateSkinTip(i);
    }

    private void updateSkinTip(int level) {
        int index = level - 1;
        if (skinLevel >= level) {
            skinTips[index].setText("状态：已拥有");
            skinTips[index].setForeground(Color.GREEN);
            skinButtons[index].setEnabled(false);
        } else {
            if (totalCoinCount >= SKIN_COSTS[index]) {
                skinTips[index].setText("状态：可购买（需" + SKIN_COSTS[index] + "金币）");
                skinTips[index].setForeground(Color.YELLOW);
                skinButtons[index].setEnabled(true);
            } else {
                skinTips[index].setText("状态：金币不足（需" + SKIN_COSTS[index] + "金币）");
                skinTips[index].setForeground(Color.RED);
                skinButtons[index].setEnabled(false);
            }
        }
    }

    // 购买皮肤后强制刷新追捕者皮肤（修复点2）
    private void buySkin(int targetLevel) {
        if (targetLevel <= skinLevel) return;
        int cost = SKIN_COSTS[targetLevel - 1];
        if (totalCoinCount >= cost) {
            totalCoinCount -= cost;
            skinLevel = targetLevel;
            has5thSkin = (skinLevel >= 5);

            // ========== 关键：买完立刻刷新皮肤 ==========
            updateChaserSkin();

            if (skinLevel == 5) {
                JOptionPane.showMessageDialog(this, "恭喜解锁5级皮肤！获得称号「捡金币大蛇」", "解锁成功", JOptionPane.INFORMATION_MESSAGE);
            }
            updateTotalCoinLabel();
            saveGameData();
            updateSkinTip(targetLevel);
        }
    }

    private void startGame() {
        mainMenuPanel.setVisible(false);
        shopPanel.setVisible(false);
        gamePanel.setVisible(true);
        gameOverPane.setVisible(false);
        gameStarted = true;
        gameOver = false;
        isInGame = true;
        reviveCount = 0;
        gameTime = System.currentTimeMillis();
        initObstaclesAndCoins();
        gamePanel.requestFocus();
    }

    private void initObstaclesAndCoins() {
        obstacles.clear();
        coins.clear();
        coinCount = 0;
        long lastX = 800;
        for (int i = 0; i < 10; i++) {
            lastX += 100 + rand.nextInt(50);
            int y = rand.nextInt(HEIGHT - 40);
            obstacles.add(new Rectangle((int) lastX, y, 40, 40));
            if (rand.nextBoolean()) {
                int cy = rand.nextInt(HEIGHT - 20);
                coins.add(new Rectangle((int) lastX + rand.nextInt(-30, 30), cy, 20, 20));
            }
        }
    }

    private long gameTime = 0;

    private void gameLoop() {
        while (true) {
            if (gameStarted && !gameOver && isInGame) {
                updatePlayerMove();
                autoMovePlayer();
                chaserFollowPlayer();
                spawnObstaclesAndCoins();
                checkCoinPickup();
                checkObstacleHit();
            }
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException ignored) {}
        }
    }

    private void autoMovePlayer() {
        mapOffset += 5;
        chaserX += 5;
    }

    private void chaserFollowPlayer() {
        if (chaserY < playerY - 10) chaserY += 3;
        if (chaserY > playerY + 10) chaserY -= 3;
        int targetChaserX = (int) (mapOffset + playerX - 100);
        if (chaserX < targetChaserX) chaserX += 3;
        if (chaserX > targetChaserX) chaserX -= 3;
    }

    private void spawnObstaclesAndCoins() {
        Iterator<Rectangle> it = obstacles.iterator();
        while (it.hasNext()) {
            Rectangle r = it.next();
            if (r.x < mapOffset - 200) it.remove();
        }
        Iterator<Rectangle> coinIt = coins.iterator();
        while (coinIt.hasNext()) {
            Rectangle c = coinIt.next();
            if (c.x < mapOffset - 50) coinIt.remove();
        }

        long maxX = 800;
        if (!obstacles.isEmpty()) {
            for (Rectangle r : obstacles) {
                long x = (long) r.getX();
                if (x > maxX) maxX = x;
            }
        }

        int farRight = (int) (mapOffset + WIDTH + 300);
        if (maxX < farRight) {
            for (int i = 0; i < 2; i++) {
                long nx = maxX + 100 + rand.nextInt(50);
                int ny = rand.nextInt(HEIGHT - 40);
                obstacles.add(new Rectangle((int) nx, ny, 40, 40));
                if (rand.nextBoolean()) {
                    int cy = rand.nextInt(HEIGHT - 20);
                    coins.add(new Rectangle((int) nx + rand.nextInt(-30, 30), cy, 20, 20));
                }
                maxX = nx;
            }
        }
    }

    private void checkCoinPickup() {
        long px = mapOffset + playerX;
        Rectangle playerBox = new Rectangle((int) px, playerY, 50, 50);
        Iterator<Rectangle> it = coins.iterator();
        while (it.hasNext()) {
            Rectangle c = it.next();
            if (playerBox.intersects(c)) {
                it.remove();
                coinCount++;
            }
        }
    }

    private void checkObstacleHit() {
        long px = mapOffset + playerX;
        Rectangle playerBox = new Rectangle((int) px, playerY, 50, 50);
        for (Rectangle r : obstacles) {
            if (playerBox.intersects(r)) {
                gameOver = true;
                totalCoinCount += coinCount;
                saveGameData();
                gameOverLabel.setText("你撞到障碍物了！");
                scoreLabel.setText("本次金币：" + coinCount + "  时长：" + (System.currentTimeMillis() - gameTime) / 1000 + "s");
                coinTipLabel.setText("总金币：" + totalCoinCount);
                SwingUtilities.invokeLater(() -> gameOverPane.setVisible(true));
                return;
            }
        }
    }

    private void resetGameState() {
        playerX = 150;
        playerY = HEIGHT / 2;
        chaserX = 50;
        chaserY = HEIGHT / 2;
        mapOffset = 0;
        gameOver = false;
        gameStarted = false;
        isInGame = false;
        gameTime = 0;
        obstacles.clear();
        coins.clear();
        coinCount = 0;
        reviveCount = 0;
        upPressed = false;
        downPressed = false;
        updateChaserSkin(); // 重置时也刷新皮肤
    }

    private void updateTotalCoinLabel() {
        SwingUtilities.invokeLater(() -> totalCoinLabel.setText("总金币: " + totalCoinCount));
    }

    private Font getCustomFont(int style, float size) {
        return defaultFont.deriveFont(style, size);
    }

    // ====================== 绘制追捕者皮肤（修复点3：真正显示图片） ======================
    class GamePanel extends JPanel {
        private GameMain game;
        public GamePanel(GameMain game) {
            this.game = game;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, WIDTH, HEIGHT);

            // 障碍物
            g2.setColor(Color.RED);
            for (Rectangle r : game.obstacles) {
                int sx = (int) (r.x - game.mapOffset);
                g2.fillRect(sx, r.y, r.width, r.height);
            }

            // 金币
            g2.setColor(Color.YELLOW);
            for (Rectangle c : game.coins) {
                int sx = (int) (c.x - game.mapOffset);
                g2.fillOval(sx, c.y, c.width, c.height);
            }

            // 玩家
            g2.drawImage(playerImage, playerX, playerY, 50, 50, null);

            // ========== 真正绘制追捕者皮肤 ==========
            int chaserScreenX = (int) (chaserX - mapOffset);
            g2.drawImage(chaserImage, chaserScreenX, chaserY, 60, 60, null);

            // UI
            g2.setColor(Color.WHITE);
            g2.setFont(getCustomFont(Font.BOLD, 20));
            g2.drawString("本局金币: " + game.coinCount, 20, 30);
            g2.drawString("总金币: " + game.totalCoinCount, 20, 60);
            g2.setFont(getCustomFont(Font.PLAIN, 16));
            g2.drawString("长按 W/S 上下移动 | D 前进", 20, 90);

            if (game.has5thSkin) {
                g2.setColor(new Color(255, 215, 0));
                g2.setFont(getCustomFont(Font.BOLD, 18));
                g2.drawString("称号：捡金币大蛇", WIDTH - 220, 30);
            }
        }
    }

    // 读取存档时也要刷新皮肤（修复点4）
    private void loadCoinAndSkinData() {
        File file = new File("game_data.txt");
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String coinLine = br.readLine();
                String skinLine = br.readLine();
                if (coinLine != null) totalCoinCount = Integer.parseInt(coinLine);
                if (skinLine != null) skinLevel = Integer.parseInt(skinLine);
                has5thSkin = (skinLevel >= 5);
                updateChaserSkin(); // 读档后刷新皮肤
            } catch (Exception e) {
                totalCoinCount = 0;
                skinLevel = 1;
            }
        }
    }

    private void saveGameData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("game_data.txt"))) {
            bw.write(String.valueOf(totalCoinCount));
            bw.newLine();
            bw.write(String.valueOf(skinLevel));
        } catch (Exception e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameMain().setVisible(true));
    }
}