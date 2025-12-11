package sumo.sim;

import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.config.Constants;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.subscription.*;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.scene.paint.Color;

import java.awt.geom.Point2D;

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

    // values tracked via subscription
    private double speed; // m/s
    private Point2D.Double position;
    private double angle;

    // values which need to be calculated / updated per tick
    private double maxSpeed;
    private double accel; // m/s²
    private double avgSpeed;
    private int nStops;
    private double waitingTime;
    private int activeTime;
    private int totalLifetime; // = waitingTime + activeTime;
    private boolean activeLastFrame; // using oldSpeed could render activeLastFrame useless
    private boolean exists; // check for despawning in gui?

    // could be used for selecting in the GUI later on
    private boolean selected;

    public VehicleWrap(String id , SumoTraciConnection con, String type, String route, Color color) {
        this.id = id;
        this.type = type;
        this.con = con;
        this.color = color;
        this.routeID = route;
        this.speed = 0.0;
        // this.position = new Point2D.Double(0.0,0.0);
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

        // currently trying to figure out how to access subscription values
        // VariableSubscription has a start and stop time (runs 100,000 ticks now)
        VariableSubscription sub = new VariableSubscription (
                SubscribtionVariable.vehicle,
                0,
                100000,
                this.id
        );

        // Add commands to our subscription of data we want to track
        sub.addCommand(Constants.VAR_SPEED);
        sub.addCommand(Constants.VAR_POSITION);
        sub.addCommand(Constants.VAR_ANGLE);

        // Register the subscription with the connection
        try {
            con.do_subscription(sub);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUsingSubscription() {
    }

    public void updateVehicle() { // updates attributes each step
        try {
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
                this.waitingTime++;
                if(this.activeLastFrame) this.nStops++;
            } else {
                this.activeTime++;
            }

            this.totalLifetime++;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public double getSpeed() {return speed;}
    public Point2D.Double getPosition() {return position;}
    public double getAngle() {return angle;}
    public double getAccel() {return accel;}
    public double getAvgSpeed() {return avgSpeed;}
    public String getID() {return id;}
    public String getType() {return type;}
    public boolean isSelected() {return selected;}
    public int getnStops() {return nStops;}
    public double getWaitingTime() {return waitingTime;}
    public double getMaxSpeed() {return maxSpeed;}
    public int getActiveTime() {return activeTime;}
    public int getTotalLifetime() {return totalLifetime;}
    public boolean exists() {return exists;}
    public void setExists(boolean exists) {this.exists = exists;}
    public Color  getColor() {return color;}
    public String getRouteID() {return routeID;}

    public void setSpeed(double speed) {
        try {
            con.do_job_set(Vehicle.setSpeed(id, speed));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}