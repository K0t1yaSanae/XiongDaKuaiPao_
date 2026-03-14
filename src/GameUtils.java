import java.util.Iterator;
import java.util.List;
import java.awt.*;
import java.util.Random;

public class GameUtils {

    public static void clearOutOfViewObstacles(List<Rectangle> obstacles, long mapOffset) {
        Iterator<Rectangle> it = obstacles.iterator();
        while (it.hasNext()) {
            Rectangle r = it.next();
            if (r.x < mapOffset - 200) {
                it.remove();
            }
        }
    }

    public static void spawnNewObstacles(List<Rectangle> obstacles, long mapOffset, Random rand) {
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
                maxX = nx;
            }
        }
    }

    public static void initObstacles(List<Rectangle> obstacles, Random rand) {
        obstacles.clear();
        long lastX = GameConstants.INIT_OBSTACLE_START_X;
        for (int i = 0; i < GameConstants.INIT_OBSTACLE_COUNT; i++) {
            lastX += GameConstants.OBSTACLE_MIN_GAP + rand.nextInt(50);
            int y = rand.nextInt(GameConstants.HEIGHT - GameConstants.OBSTACLE_SIZE);
            obstacles.add(new Rectangle((int) lastX, y, GameConstants.OBSTACLE_SIZE, GameConstants.OBSTACLE_SIZE));
        }
    }

    public static boolean checkObstacleHit(int playerX, int playerY, long mapOffset, List<Rectangle> obstacles) {
        long pWorldX = mapOffset + playerX;
        Rectangle playerBox = new Rectangle((int) pWorldX, playerY, GameConstants.PLAYER_SIZE, GameConstants.PLAYER_SIZE);

        for (Rectangle r : obstacles) {
            if (playerBox.intersects(r)) {
                return true;
            }
        }
        return false;
    }
}