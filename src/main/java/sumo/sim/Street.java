package sumo.sim;

import it.polito.appeal.traci.SumoTraciConnection;

public class Street {
    private double maxSpeed; // same attributes as in .net
    private final SumoTraciConnection con;
    private final String id;
    // List of <Lane> objects

    public Street(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
    }

    public String getId() {
        return "";
    }
}
