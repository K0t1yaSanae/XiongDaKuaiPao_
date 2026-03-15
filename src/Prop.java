import java.awt.*;

public class Prop {
    private int x, y;
    private PropType type;
    private boolean isActive = true;

    public Prop(int x, int y, PropType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public boolean isPicked(int playerX, int playerY, long mapOffset) {
        long playerWorldX = mapOffset + playerX;
        Rectangle playerRect = new Rectangle((int) playerWorldX, playerY, GameConstants.PLAYER_SIZE, GameConstants.PLAYER_SIZE);
        Rectangle propRect = new Rectangle(x, y, GameConstants.PROP_SIZE, GameConstants.PROP_SIZE);
        return playerRect.intersects(propRect);
    }

    public void draw(Graphics2D g2, long mapOffset) {
        if (!isActive) return;
        int screenX = (int) (x - mapOffset);

        Color color = switch (type) {
            case INVINCIBLE -> Color.YELLOW;
            case SPEED_UP -> Color.GREEN;
            case CLEAR_OBSTACLE -> Color.RED;
        };

        g2.setColor(color);
        g2.fillOval(screenX, y, GameConstants.PROP_SIZE, GameConstants.PROP_SIZE);
        g2.setColor(Color.BLACK);
        g2.drawOval(screenX, y, GameConstants.PROP_SIZE, GameConstants.PROP_SIZE);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        String text = switch (type) {
            case INVINCIBLE -> "无";
            case SPEED_UP -> "速";
            case CLEAR_OBSTACLE -> "清";
        };
        g2.drawString(text, screenX + GameConstants.PROP_SIZE/2 - 6, y + GameConstants.PROP_SIZE/2 + 4);
    }

    // Getter/Setter
    public int getX() { return x; }
    public int getY() { return y; }
    public PropType getType() { return type; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}