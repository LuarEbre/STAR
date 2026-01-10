package sumo.sim.logic;

public class Type {

    private final String id;
    private final String color;
    private double speed;

    public Type(String id, String color, double speed) {
        this.id = id;
        this.color = color;
        this.speed = speed;
    }

    public String getId() {
        return id;
    }
}
