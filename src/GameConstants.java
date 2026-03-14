import java.awt.*;

public class GameConstants {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    public static final int PLAYER_SIZE = 28;
    public static final int CHASER_SIZE = 28;
    public static final int OBSTACLE_SIZE = 40;

    public static final int PLAYER_AUTO_SPEED = 2;
    public static final int PLAYER_CONTROL_SPEED = 3;
    public static final int CHASER_FOLLOW_SPEED = 2;

    public static final int OBSTACLE_MIN_GAP = 80;
    public static final int INIT_OBSTACLE_COUNT = 12; // 初始障碍物数量
    public static final int SPAWN_OBSTACLE_COUNT = 2;
    public static final long INIT_OBSTACLE_START_X = 200;

    public static final Color MAIN_MENU_BACKGROUND = Color.DARK_GRAY;
    public static final Color GAME_OVER_PANEL_BACKGROUND = new Color(0, 0, 0, 220);
    public static final Color DEFAULT_PLAYER_COLOR = Color.CYAN;
    public static final Color DEFAULT_CHASER_COLOR = Color.ORANGE;
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;
    public static final Color DEFAULT_OBSTACLE_COLOR = Color.RED;
    public static final Color PROMPT_TEXT_COLOR = Color.WHITE;
}
