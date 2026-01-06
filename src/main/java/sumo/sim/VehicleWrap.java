package sumo.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.util.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.scene.paint.Color;
import java.awt.geom.Point2D;

/**
 * A wrapper of {@link Vehicle} allowing for instancing of individual vehicles
 * <p>Includes stats tracked by {@link SumoTraciConnection} but also client-side calculated stats like {@link VehicleWrap#avgSpeed},{@link VehicleWrap#accel},
 * {@link VehicleWrap#totalLifetime} and properties critical for rendering such as {@link VehicleWrap#color}
 */
public class VehicleWrap {

    // currently, to set the currentStreet of a vehicle as a Street, each car would have to have a reference to Street_List
    // therefore it could be beneficial to just use a String of EdgeID
    private Street currentStreet;

    // final values, set once and never updated after
    private final String id;
    private String name; // name = id; -> but can be customized for searching
    private final String type;
    private final SumoTraciConnection con;
    private Color color;
    private String routeID; // which route the car is assigned to (could be of RouteWrap if implemented)

    private double speed; // m/s
    private Point2D.Double position;
    private double angle;

    // values which need to be calculated / updated per tick
    private double maxSpeed;
    private double accel; // m/s²
    private double avgSpeed;
    private int nStops;
    private int waitingTime;
    private int activeTime;
    private int totalLifetime; // = waitingTime + activeTime;
    private boolean activeLastFrame; // using oldSpeed could render activeLastFrame useless
    private boolean currentlyStopped;
    private boolean exists; // check for despawning in gui?
    private boolean departed;

    // could be used for selecting in the GUI later on
    private boolean selected;

    /**
     * Constructor initializes most values to 0 before they can be set by {@link VehicleWrap#updateVehicle()}
     * @param id Vehicle ID
     * @param con an instance of {@link SumoTraciConnection}
     * @param type Vehicle Type
     * @param route Vehicle Route
     * @param color Vehicle Color
     */
    public VehicleWrap(String id , SumoTraciConnection con, String type, String route, Color color) {
        this.id = id;
        this.type = type;
        this.con = con;
        this.color = color;
        this.routeID = route;
        this.speed = 0.0;
        this.position = new Point2D.Double(0.0,0.0);
        // this.angle = 0.0;
        this.maxSpeed = 0.0;
        // this.accel = 0.0;
        this.avgSpeed = 0.0;
        this.nStops = 0;
        // activeTime = 1, waitingTime = -1 -> first frame of existence = active
        this.waitingTime = -1;
        this.activeTime = 1;
        this.totalLifetime = 0;
        this.activeLastFrame = false;
        this.currentlyStopped = false;
        this.departed = false;
    }

    /**
     * Gets called each step by the Simulation, updates all SUMO internal values using {@link SumoTraciConnection#do_job_get(SumoCommand)}
     * , as well as calculating our live-tracked values.
     */
    public void updateVehicle() { // updates attributes each step, causes exception (if many cars are updated and delay is changed) needs fixing
        try {
            this.currentlyStopped = false;
            // retrieve previous frame's speed before updating the vehicle's speed
            double oldSpeed = this.speed;
            // determine whether vehicle has been active last frame via oldSpeed
            this.activeLastFrame = oldSpeed > 0;
            this.speed = (double)con.do_job_get(Vehicle.getSpeed(id)); // returns SumoCommand, which is then performed by do_job_get
            SumoPosition2D pos2D = (SumoPosition2D)con.do_job_get(Vehicle.getPosition(id)); // casted on SumoPosition2d
            this.position = new Point2D.Double(pos2D.x, pos2D.y); // SumoPosition values stored in Point2d object
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
                if(this.activeLastFrame) this.nStops++;
            } else {
                this.activeTime++;
            }
            this.totalLifetime++;
        } catch (Exception e) {
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
     * @return true: if the vehicle is currently selected by the user in the GUI<br>false: else
     */
    public boolean isSelected() { return selected; }
    /**
     * @return The number of times the vehicle has stopped.
     */
    public int getnStops() { return nStops; }
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
    public int getActiveTime() { return activeTime; }
    /**
     * @return The total lifetime of the vehicle.
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
    public String getRouteID() { return routeID; }
    public boolean isCurrentlyStopped() { return this.currentlyStopped; }
}