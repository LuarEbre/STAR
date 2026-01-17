package sumo.sim.objects;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.util.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;
import sumo.sim.data.XML;
import sumo.sim.logic.WrapperController;
import sumo.sim.util.ExportableData;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper of {@link Edge} allowing for instancing of individual Edges (Streets)
 * <p>Includes stats tracked by {@link SumoTraciConnection} but also client-side calculated stats like {@link Street#density}
 */
public class Street extends SelectableObject implements ExportableData {
    // connection
    private final SumoTraciConnection con;
    private final String id;

    // List of <Lane> objects
    private final ArrayList<LaneWrap> lanes = new ArrayList<>();
    private String fromJunction;
    private String toJunction;

    // attributes
    private double maxSpeed; // same attributes as in .net
    private double density;
    private double minX,minY,maxX,maxY; // for rendering optimization
    private XML xml;
    // Data Export
    private double sumDensity = 0.0;
    private long stepStartOrReset = 0;
    private double maxDensity = 0;
    private WrapperController controller;


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
    public Street(String id, String from, String to, SumoTraciConnection con, WrapperController controller) {
        this.controller = controller;
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

    @Override
    public String getExportCategory() {
        //header
        return "Traffic Density";
    }
    @Override
    public String[] getColumnHeaders() {
        // header for rows
        return new String[] {
                "Street ID",
                "Avg Density",
                "Peak Density",
                "measurement",
                "from",
                "to"
        };
    }
    @Override
    public String[] getRowData() {
        long measurementSteps = getMeasurementDuration();
        // data for columns
        return new String[]{
                this.id,
                String.format(java.util.Locale.US, "%.2f", getAverageDensity()),
                String.format(java.util.Locale.US, "%.2f", this.maxDensity),
                String.valueOf(measurementSteps),
                this.fromJunction != null ? this.fromJunction : "unknown",
                this.toJunction != null ? this.toJunction : "unknown"
        };
    }
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
            this.sumDensity = this.sumDensity + this.density;
                if(this.density > this.maxDensity) {
                    this.maxDensity = this.density;
                }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update Street Data", e);
            this.density = 0;
            //this.noise = 0;
        }
    }
    //helper method
    public long getMeasurementDuration() {
        long duration = this.controller.getStepCounter() - this.stepStartOrReset;
            if (duration < 0) {
                duration = 0;
            }
        return duration;
    }
    /**
     * Resets traffic data tracking for this street.
     * <p>
     * Clears cumulative density values and synchronizes the start step
     * with the current simulation step to begin a new measurement interval.
     */
    public void resetDataTracking() {
        //reset steps/measurement after traffic light changes
        this.sumDensity = 0.0;
        this.maxDensity = 0.0;
        this.stepStartOrReset = this.controller.getStepCounter();
    }
    /**
     * Calculates the average traffic density since the last reset.
     * * @return The average density as a double, or 0.0 if no steps
     * have passed since the last reset.
     */
    public double getAverageDensity() {
        long duration = getMeasurementDuration();

        if (duration <= 0) return 0.0;
        return this.sumDensity / duration;
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

    // getter
    public Street getStreetBasedOnLane(String laneID){
       for (LaneWrap lane : lanes) {
           if (lane.getLaneID().equals(laneID)) {
               return this;
           }
       }
        return null;
    }

    public void setDensity(double den) { this.density = den; }
    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }
    public double getSumDensity() {return sumDensity; }
    public double getMaxDensity() {return maxDensity;}
    public double getMeanPositionX() { return meanPositionX; }
    public double getMeanPositionY() { return meanPositionY; }
    public ArrayList<LaneWrap> getLanes() { return lanes; }
    public String getId() { return id; }
    public String getFromJunction() { return fromJunction; }
    public String getToJunction() { return toJunction; }
    public Street getStreet() { return this; }
    public double getDensity() { return density; }
}

