package sumo.sim;

import it.polito.appeal.traci.SumoTraciConnection;

public class Street {
    private double maxSpeed; // same attributes as in .net
    private final SumoTraciConnection con;
    private final String id;
    // List of <Lane> objects
    private String fromJunction;
    private String toJunction;
    XML xml = null;

    public Street(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
        try {
            xml = new XML(WrapperController.get_current_net());
            this.fromJunction = xml.get_from_junction(id);
            this.toJunction = xml.get_to_junction(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getId() {
        return id;
    }

    public String getFromJunction() {
        return fromJunction;
    }

    public Street getStreet() { return this; }

    public String getToJunction() {
        return toJunction;
    }
}