import java.awt.*;

public class Prop {
    public static final int PROP_SIZE = 30;
    private final int x;
    private final int y;
    private final PropType type;
    private boolean isActive = true;

    public Prop(int x, int y, PropType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public boolean isPicked(int playerX, int playerY, long mapOffset) {
        if (!isActive) return false;
        long playerWorldX = mapOffset + playerX;
        Rectangle playerRect = new Rectangle((int) playerWorldX, playerY, GameConstants.PLAYER_SIZE, GameConstants.PLAYER_SIZE);
        Rectangle propRect = new Rectangle(x, y, PROP_SIZE, PROP_SIZE);
        return playerRect.intersects(propRect);
    }

    public int getScreenX(long mapOffset) {
        return x - (int) mapOffset;
    }

    public void draw(Graphics2D g2, long mapOffset) {
        if (!isActive) return;
        int screenX = getScreenX(mapOffset);
        if (screenX > -PROP_SIZE && screenX < GameConstants.WIDTH) {
            g2.setColor(type.getColor());
            g2.fillOval(screenX, y, PROP_SIZE, PROP_SIZE);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(screenX, y, PROP_SIZE, PROP_SIZE);
        }
    }

    public PropType getType() {
        return type;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}