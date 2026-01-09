package sumo.sim;

import java.util.logging.Logger;

public class Type {

    private final String id;
    private final String color;
    private double speed;

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(Type.class.getName());

    public Type(String id, String color, double speed) {
        this.id = id;
        this.color = color;
        this.speed = speed;
    }

    public String getId() {
        return id;
    }
}
