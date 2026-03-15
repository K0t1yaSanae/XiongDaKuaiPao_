import java.awt.*;

public class GameConstants {
    // 尺寸常量
    public static final int PLAYER_SIZE = 50;
    public static final int CHASER_SIZE = 60;
    public static final int OBSTACLE_SIZE = 40;
    public static final int PROP_SIZE = 30;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    // 速度常量
    public static final int PLAYER_AUTO_SPEED = 5;
    public static final int PLAYER_AUTO_SPEED_SPEED_UP = 8;
    public static final int PLAYER_CONTROL_SPEED = 8;
    public static final int CHASER_FOLLOW_SPEED = 3;

    // 障碍物生成常量
    public static final int INIT_OBSTACLE_COUNT = 10;
    public static final int INIT_OBSTACLE_START_X = 800;
    public static final int OBSTACLE_MIN_GAP = 100;
    public static final int SPAWN_OBSTACLE_COUNT = 2;

    // 道具生成常量
    public static final int INIT_PROP_COUNT = 3;
    public static final int PROP_SPAWN_CHANCE = 15;

    // 颜色常量
    public static final Color MAIN_MENU_BACKGROUND = new Color(200, 200, 200);
    public static final Color PROMPT_TEXT_COLOR = Color.WHITE;
    public static final Color PROP_TIP_COLOR = new Color(255, 215, 0);
}