package sumo.sim.objects;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.util.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;
import sumo.sim.data.XML;
import sumo.sim.util.ExportableData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper of {@link Edge} allowing for instancing of individual Edges (Streets)
 * <p>Includes stats tracked by {@link SumoTraciConnection} but also client-side calculated stats like {@link Street#density}
 */
public class Street implements ExportableData {
    private double maxSpeed; // same attributes as in .net
    private final SumoTraciConnection con;
    private final String id;
    // List of <Lane> objects
    private final ArrayList<LaneWrap> lanes = new ArrayList<>();
    private String fromJunction;
    private String toJunction;
    private double density;
    //private double noise;
    private double minX,minY,maxX,maxY; // for rendering optimization
    private XML xml;
    // Data Export
    private double sumDensity = 0.0;
    private long densityTicks = 0;
    private double maxDensity = 0;



    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(Street.class.getName());

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
                "Avg Density (Veh/km)",
                "Peak Denisty",
                "Duration (Ticks)",
                "from",
                "to"
        };
    }

    @Override
    public String[] getRowData() {
        // data for columns
        return new String[]{
                this.id,
                String.format(java.util.Locale.US, "%.2f", getAverageDensity()),
                String.format(java.util.Locale.US, "%.2f", this.maxDensity),
                String.valueOf(this.densityTicks),
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
            int num = (int) con.do_job_get(Edge.getLastStepVehicleNumber(id));
            double length = lanes.getFirst().getLength();

            this.density = num / (length / 1000.0);
        }
        catch (Exception e){
            logger.log(Level.FINE, "Failed to calculate Street Density", e);
            throw new RuntimeException(e);
        }
    }

    public void resetDataTracking() {
        this.sumDensity = 0.0;
        this.densityTicks = 0;
        this.maxDensity = 0.0;
    }
    /**
     * Calculates the Street's density each tick via {@link Street#calcDensity()} and sets the noise emission via {@link SumoTraciConnection#do_job_get(SumoCommand)}
     */
    public void updateStreet() {
        try {
            calcDensity();
            //this.noise = (double) this.con.do_job_get(Edge.getNoiseEmission(id));
            this.sumDensity = this.sumDensity + this.density;
            this.densityTicks++;
                if(this.density > this.maxDensity) {
                    this.maxDensity = this.density;
                }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update Street Data", e);
            this.density = 0;
            //this.noise = 0;
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

    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }
    public double getSumDensity() {return sumDensity; }
    public long getDensityTicks() {return densityTicks; }
    public double getMaxDensity() {return maxDensity;}
    public double getAverageDensity() {return (densityTicks > 0) ? (sumDensity / densityTicks) : 0;}
}
