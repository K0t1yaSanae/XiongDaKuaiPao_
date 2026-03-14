import java.awt.*;

public enum PropType {
    INVINCIBLE("无敌", Color.YELLOW, 5000),
    SPEED_UP("加速", Color.GREEN, 5000),
    CLEAR_OBSTACLE("清障", Color.BLUE, 0);

    private final String name;
    private final Color color;
    private final long duration;

    PropType(String name, Color color, long duration) {
        this.name = name;
        this.color = color;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public long getDuration() {
        return duration;
    }
}