public enum PropType {
    INVINCIBLE("无敌", 5000),
    SPEED_UP("加速", 3000),
    CLEAR_OBSTACLE("清障", 1000);

    private final String name;
    private final int duration;

    PropType(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() { return name; }
    public int getDuration() { return duration; }
}