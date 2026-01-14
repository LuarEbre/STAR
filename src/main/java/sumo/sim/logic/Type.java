package sumo.sim.logic;

import java.util.logging.Logger;

public class Type {

    private final String id;
    private final String color;
    private double speed;

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(Type.class.getName());

    /**
     * The Constructor of the Type object.
     * ID, color and speed are required.
     *
     * @param id
     * @param color
     * @param speed
     */
    public Type(String id, String color, double speed) {
        this.id = id;
        this.color = color;
        this.speed = speed;
    }

    /**
     * Returns the ID of the Type object.
     *
     * @return
     */
    public String getId() {
        return id;
    }
}
