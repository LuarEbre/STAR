package sumo.sim.objects;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.util.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper of {@link Edge} allowing for instancing of individual Edges (Streets)
 * <p>Includes stats tracked by {@link SumoTraciConnection} but also client-side calculated stats like {@link Street#density}
 */
public class Street extends SelectableObject {
    // connection
    private final SumoTraciConnection con;
    private final String id;

    // List of <Lane> objects
    private final ArrayList<LaneWrap> lanes = new ArrayList<>();
    private String fromJunction;
    private String toJunction;

    // attributes
    private double density;
    private double minX,minY,maxX,maxY; // for rendering optimization
    private double meanPositionX;
    private double meanPositionY;

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(Street.class.getName());

    /**
     * @param id Edge ID
     * @param con an instance of {@link SumoTraciConnection}
     */
    public Street(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
        calcMeanPosition();
    }

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
        calcMeanPosition();
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
            logger.log(Level.FINE, "Failed to initialize street Data", e);
            throw new RuntimeException("Failed to initialize Street " + id, e);
        }

    }

    /**
     * Calculates the Street's density based on its length and the amount of vehicles currently on the Street
     */
    public void calcDensity(){
        try{
            int num = (int) con.do_job_get(Edge.getLastStepVehicleNumber(id));
            double length = lanes.getFirst().getLength();

            this.density = num / (length / 1000.0);
        }
        catch (Exception e){
            logger.log(Level.FINE, "Failed to calculate Street Density", e);
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    private void calcMeanPosition() {
        if (lanes.isEmpty()) return;
        LaneWrap middleLane = lanes.get(lanes.size() / 2); // middle index

        double[] rawX = middleLane.getShapeX();
        double[] rawY = middleLane.getShapeY();

        if (rawX == null || rawX.length < 2) return;
        // Normal Line
        if (rawX.length == 2) {
            this.meanPositionX = (rawX[0] + rawX[1]) / 2.0;
            this.meanPositionY = (rawY[0] + rawY[1]) / 2.0;
        }

        // Polyline
        else {
            this.meanPositionX = rawX[rawX.length / 2];
            this.meanPositionY = rawY[rawX.length / 2];
        }
    }

    /**
     * Calculates the Street's density each tick via {@link Street#calcDensity()} and sets the noise emission via {@link SumoTraciConnection#do_job_get(SumoCommand)}
     */
    public void updateStreet() {
        try {
            calcDensity();
            //this.noise = (double) this.con.do_job_get(Edge.getNoiseEmission(id));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update Street Data", e);
            this.density = 0;
            //this.noise = 0;
        }
    }

    public void calculateBounds() {
        minX = Double.MAX_VALUE;
        maxX = -Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (LaneWrap lane : lanes) {
            double[] xCoords = lane.getShapeX();
            double[] yCoords = lane.getShapeY();

            for (double x : xCoords) {
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
            }
            for (double y : yCoords) {
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
        }

    }

    // setter
    public void setDensity(double den) { this.density = den; }
    // getter

    public LaneWrap getLaneBasedOnID(String laneID){
        for (LaneWrap lane : lanes) {
            if (lane.getLaneID().equals(laneID)) {
                return lane;
            }
        }
        return null;
    }

    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }
    public double getMeanPositionX() { return meanPositionX; }
    public double getMeanPositionY() { return meanPositionY; }
    public ArrayList<LaneWrap> getLanes() { return lanes; }
    public String getId() { return id; }
    public String getFromJunction() { return fromJunction; }
    public String getToJunction() { return toJunction; }
    public double getDensity() { return density; }
}
