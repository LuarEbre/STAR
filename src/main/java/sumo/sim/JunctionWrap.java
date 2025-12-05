package sumo.sim;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.geom.Point2D;

import static java.lang.Math.abs;

public class JunctionWrap {
    private final String id;
    private final Point2D.Double position;
    private final SumoTraciConnection con;

    private double distance = Integer.MAX_VALUE; //Used for Dijkstra Initialization
    private String predecessor = null; //Used for Dijkstra

    public JunctionWrap(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;

        try {
            SumoPosition2D pos2D = (SumoPosition2D) this.con.do_job_get(Junction.getPosition(id)); // position
            this.position = new Point2D.Double(pos2D.x, pos2D.y);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getID() {
        return id;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setPredecessor(String predecessor) {
        this.predecessor = predecessor;
    }

    public String getPredecessor() {
        return predecessor;
    }

    public double distance_to(JunctionWrap u) {
        double distance = abs((this.position.x + this.position.y) - (u.position.x + u.position.y));
        return distance;
    }

}
