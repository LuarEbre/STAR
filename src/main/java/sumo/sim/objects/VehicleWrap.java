package sumo.sim.objects;


import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.util.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.scene.paint.Color;
import sumo.sim.util.ExportableData;

import java.awt.geom.Point2D;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper of {@link Vehicle} allowing for instancing of individual vehicles
 * <p>Includes stats tracked by {@link SumoTraciConnection} but also client-side calculated stats like {@link VehicleWrap#avgSpeed},{@link VehicleWrap#accel},
 * {@link VehicleWrap#totalLifetime} and properties critical for rendering such as {@link VehicleWrap#color}
 */
public class VehicleWrap extends SelectableObject implements ExportableData {


    // final values, set once and never updated after
    private final String id;
    private final String type;
    private final SumoTraciConnection con;
    private final Color color;
    private final String routeID;

    // values which need to be calculated / updated per tick
    private double speed; // m/s
    private Point2D.Double position;
    private double angle;
    private double maxSpeed;
    private double accel; // m/s²
    private double avgSpeed;
    private int numberOfStops;
    private int waitingTime;
    private int activeTime;
    private int totalLifetime; // = waitingTime + activeTime;

    private boolean activeLastFrame;
    private boolean currentlyStopped;
    private boolean exists;
    private boolean queued;

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(VehicleWrap.class.getName());

    /**
     * Constructor initializes most values to 0 before they can be set by {@link VehicleWrap#updateVehicle()}
     * @param id Vehicle ID
     * @param con an instance of {@link SumoTraciConnection}
     * @param type Vehicle Type
     * @param route Vehicle Route
     * @param color Vehicle Color
     */
    public VehicleWrap(String id , SumoTraciConnection con, String type, String route, Color color) {
        super();
        this.id = id;
        this.type = type;
        this.con = con;
        this.color = color;
        this.routeID = route;
        this.speed = 0.0;
        this.position = new Point2D.Double(0.0,0.0);
        this.maxSpeed = 0.0;
        this.avgSpeed = 0.0;
        this.numberOfStops = 0;
        this.waitingTime = -1;
        this.activeTime = 1;
        this.totalLifetime = 0;
        this.activeLastFrame = false;
        this.currentlyStopped = false;
        // assume any newly created Vehicle is queued, only setting to false if Vehicle is confirmed to exist
        this.queued = true;
    }
    /**
     * Returns the export category for vehicle data.
     * @return A string header for the vehicle data section.
     */
    @Override
    public String getExportCategory() {return "Vehicles: "; }
    /**
     * Defines the table headers for the vehicle export.
     * @return An array of strings including the new Route column.
     */
    @Override
    public String[] getColumnHeaders() {
        return new String[] {
                "ID",
                "Route",
                "Avg Speed",
                "Stops",
                "Waiting Time",
                "Max Speed"
        };
    }
    /**
     * Formats the vehicle's performance metrics and route information for export.
     * <p>
     * Handles non-finite double values (NaN/Infinity) by defaulting to 0.0
     * to ensure export stability.
     * @return A string array containing formatted vehicle statistics.
     */
    @Override
    public String[] getRowData() {
        double avg = 0.0;
        if (Double.isFinite(avgSpeed)) {
            avg = avgSpeed;
        }
        double max = 0.0;
        if (Double.isFinite(maxSpeed)) {
            max = maxSpeed;
        }
        double wait = 0.0;
        if (Double.isFinite(waitingTime)) {
            wait = waitingTime;
        }
        return new String[] {
                id,
                this.routeID,
                String.format(Locale.US, "%.2f m/s", avg),
                String.valueOf(numberOfStops),
                String.format(Locale.US, "%.0f s", wait),
                String.format(Locale.US, "%.2f m/s", max)
        };
    }

    /**
     * Gets called each step by the Simulation, updates all SUMO internal values using {@link SumoTraciConnection#do_job_get(SumoCommand)}
     * , as well as calculating our live-tracked values.
     */
    public void updateVehicle() { // updates attributes each step, causes exception (if many cars are updated and delay is changed) needs fixing
        try {
            // assume Vehicle currentl'y isn't stopped
            this.currentlyStopped = false;
            // retrieve previous frame's speed before updating the vehicle's speed
            double oldSpeed = this.speed;
            // determine whether vehicle has been active last frame via oldSpeed
            this.activeLastFrame = oldSpeed > 0;
            this.speed = (double)con.do_job_get(Vehicle.getSpeed(id)); // returns SumoCommand, which is then performed by do_job_get
            SumoPosition2D pos2D = (SumoPosition2D)con.do_job_get(Vehicle.getPosition(id)); // cast to SumoPosition2D
            this.position = new Point2D.Double(pos2D.x, pos2D.y); // SumoPosition values stored in Point2D object
            this.angle = (double)con.do_job_get(Vehicle.getAngle(id));

            // since time between calculating acceleration is always 1 second
            // a = delta v / delta t simplifies to a = delta v
            // positive acceleration -> speeding up
            // negative acceleration -> slowing down
            this.accel = this.speed - oldSpeed;

            // calculate cumulative average
            this.avgSpeed = ((this.avgSpeed*this.totalLifetime)+this.speed)/(this.totalLifetime+1);

            if(speed > maxSpeed) {
                this.maxSpeed = speed;
            }

            // determine whether waiting or active
            if(this.speed == 0) {
                this.currentlyStopped = true;
                this.waitingTime++;
                // if Vehicle is currently stopped but was active last frame increment numberOfStops
                if(this.activeLastFrame) this.numberOfStops++;
            } else {
                this.activeTime++;
            }
            this.totalLifetime++;

        } catch (Exception e) {
            String errorMessage = "Failed to update vehicle (" + this.id + ")";
            logger.log(Level.FINE, errorMessage, e);
            this.exists = false;
        }
    }
    /**
     * Allows for setting individual vehicle's speed.
     * @param speed desired speed in m/s
     */
    public void setSpeed(double speed) {
        try {
            con.do_job_set(Vehicle.setSpeed(id, speed));
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to set speed of specific Vehicle", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * @return The current speed of the vehicle in m/s.
     */
    public double getSpeed() { return speed; }
    /**
     * @return The current X,Y coordinates of the vehicle.
     */
    public Point2D.Double getPosition() { return position; }
    /**
     * @return The angle of the vehicle in degrees (0-360).
     */
    public double getAngle() { return angle; }
    /**
     * @return The current acceleration in m/s².
     */
    public double getAccel() { return accel; }
    /**
     * @return The average speed over the vehicle's entire trip.
     */
    public double getAvgSpeed() { return avgSpeed; }
    /**
     * @return This vehicle's ID.
     */
    public String getID() { return id; }
    /**
     * @return The vehicle Type identifier (e.g. "STANDARD_VEH").
     */
    public String getType() { return type; }
    /**
     * @return The number of times the vehicle has stopped.
     */
    public int getNumberOfStops() { return numberOfStops; }
    /**
     * @return The total time (in seconds) the vehicle has spent waiting.
     */
    public int getWaitingTime() { return waitingTime; }
    /**
     * @return The maximum recorded speed of this vehicle.
     */
    public double getMaxSpeed() { return maxSpeed; }
    /**
     * @return The duration (in seconds) the vehicle has been active (not stopped).
     */
    public int getTotalLifetime() { return totalLifetime; }
    /**
     * @return true if the vehicle is currently active on the road network.
     */
    public boolean exists() { return exists; }
    /**
     * Updates the vehicle's existence status
     * @param exists true if the vehicle is on the road network, false otherwise.
     */
    public void setExists(boolean exists) { this.exists = exists; }
    /**
     * @return {@link Color} used to render the vehicle.
     */
    public Color getColor() { return color; }
    /**
     * @return ID of the route this vehicle is following.
     */
    /**
     * @return The ID of the route this vehicle is following.
     */
    public String getRouteID() { return routeID; }

    /**
     * @return True if the vehicle is currently stopped (speed is ~0).
     */
    public boolean isCurrentlyStopped() { return this.currentlyStopped; }

    /**
     * Updates the queued status (waiting to enter the road network).
     * @param queued True if waiting in the insertion queue.
     */
    protected void setQueued(boolean queued) { this.queued = queued; }

    /**
     * @return True if the vehicle is waiting in the insertion queue.
     */
    protected boolean isQueued()  { return this.queued; }
}