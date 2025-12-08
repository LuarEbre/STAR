package sumo.sim;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Lane;
import it.polito.appeal.traci.SumoTraciConnection;

public class Street {
    private double maxSpeed; // same attributes as in .net
    private final SumoTraciConnection con;
    private final String id;
    // List of <Lane> objects
    private String fromJunction;
    private String toJunction;
    XML xml = null;
    double density;

    public Street(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
        try {
            xml = new XML(WrapperController.get_current_net());
            this.fromJunction = xml.get_from_junction(id);
            this.toJunction = xml.get_to_junction(id);
            calc_density(con);
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

    public Street getStreet() {
        return this;
    }

    public String getToJunction() {
        return toJunction;
    }

    public void calc_density(SumoTraciConnection con) {
        try {
            double num = (double) con.do_job_get(Edge.getLastStepVehicleNumber(this.id));
            double length = (double) con.do_job_get(Lane.getLength(this.id+ "_0"));
            this.density = num/ length / 1000;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

}