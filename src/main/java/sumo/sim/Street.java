package sumo.sim;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.util.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;
import java.util.ArrayList;

/**
 * A wrapper of {@link Edge} allowing for instancing of individual Edges (Streets)
 * <p>Includes stats tracked by {@link SumoTraciConnection} but also client-side calculated stats like {@link Street#density}
 */
public class Street {
    private double maxSpeed; // same attributes as in .net
    private final SumoTraciConnection con;
    private final String id;
    // List of <Lane> objects
    private final ArrayList<LaneWrap> lanes = new ArrayList<>();
    private String fromJunction;
    private String toJunction;
    private double density;
    private double noise;
    private XML xml;

    /**
     * @param id Edge ID
     * @param from Junction ID
     * @param to Junction ID
     * @param con an instance of {@link SumoTraciConnection}
     */
    public Street(String id, String from, String to, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
        this.fromJunction = from;
        this.toJunction = to;
        initializeStreet();
    }

    /**
     * Gets the number of lanes within the Edge and fills the {@link ArrayList} of {@link LaneWrap} with new objects
     */
    public void initializeStreet() {
        try {
            int laneCount = (Integer) this.con.do_job_get(Edge.getLaneNumber(id));
            for (int i = 0; i < laneCount; i++) {
                lanes.add(new LaneWrap(this.id + "_" + i, this.con, this.id));
            }
            updateStreet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Street " + id, e);
        }

    }

    /**
     * @param id Edge ID
     * @param con an instance of {@link SumoTraciConnection}
     */
    public Street(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
    }

    /**
     * Calculates the Street's density based on its length and the amount of vehicles currently on the Street
     */
    public void calcDensity(){
        try{
            Number num = (Number) con.do_job_get(Edge.getLastStepVehicleNumber(id));
            Number length = (Number) con.do_job_get(Lane.getLength(id+"_0"));

            double num_val = num.doubleValue();
            double length_val = length.doubleValue();

            this.density = num_val/length_val/1000;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the Street's density each tick via {@link Street#calcDensity()} and sets the noise emission via {@link SumoTraciConnection#do_job_get(SumoCommand)}
     */
    public void updateStreet() {
        try {
            calcDensity();
            this.noise = (double) this.con.do_job_get(Edge.getNoiseEmission(id));
        } catch (Exception e) {
            this.density = 0;
            this.noise = 0;
        }
    }

    /**
     * @return The {@link ArrayList} of {@link LaneWrap} objects contained in this street.
     */
    public ArrayList<LaneWrap> getLanes() { return lanes; }
    /**
     * @return This street's ID.
     */
    public String getId() { return id; }
    /**
     * @return ID of the junction where this street begins.
     */
    public String getFromJunction() { return fromJunction; }
    /**
     * @return ID of the junction where this street ends.
     */
    public String getToJunction() { return toJunction; }
    /**
     * @return {@link Street} itself.
     */
    public Street getStreet() { return this; }
    /**
     * Sets the current traffic density on this street.
     * @param den The new density value
     */
    public void setDensity(double den) { this.density = den; }
    /**
     * @return Current traffic density on this street.
     */
    public double getDensity() { return density; }
}
