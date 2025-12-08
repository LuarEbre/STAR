package sumo.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.*;
import java.awt.geom.Point2D;

public class VehicleWrap {
    private final String id;
    private String name; // name = id; -> but can be customized for searching
    private final String type;
    private final SumoTraciConnection con;
    //private final SumoColor color;
    private boolean marked;

    private double speed;
    private double accel;
    private double decel;
    private Point2D.Double position;
    private double angle;
    private int routeId; // which route is assigned

    private int number_stops;
    private int timeActive; // time driving before despawning -> 300 seconds active and 34 seconds standing
    private double stop_time;
    private double maxspeed;
    private boolean active_last_frame;

    public VehicleWrap(String id , SumoTraciConnection con, String type) {
        this.id = id;
        this.con = con;
        this.type = type;

        this.maxspeed = 0.0;
        this.active_last_frame = false;
        updateVehicle();
    }

    public void updateVehicle(){ // updates attributes each step
        try {
            this.speed = (double)con.do_job_get(Vehicle.getSpeed(id)); // returns SumoCommand, which is then performed by do_job_get
            SumoPosition2D pos2D = (SumoPosition2D)con.do_job_get(Vehicle.getPosition(id)); // casted on SumoPosition2d
            this.position = new Point2D.Double(pos2D.x, pos2D.y); // SumoPosition values stored in Point2d object
            this.angle = (double)con.do_job_get(Vehicle.getAngle(id));

            if(this.speed > 0){
                this.maxspeed = speed;
            }

            if(this.speed == 0){
                stop_time++;
                if(active_last_frame){
                    number_stops++;
                }
            }

            if(this.speed > 0){
                active_last_frame = true;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public double getSpeed() {return speed;}
    public Point2D.Double getPosition() {return position;}
    public double getAngle() {return angle;}
    public String getID() {return id;}
    public String getType() {return type;}
    public boolean isMarked() {return marked;} // maybe return object? -> marked (by which filter) and which color
    public int getNumber_stops() {return number_stops;}
    public double getStop_time() {return stop_time;}
    public double getMaxSpeed() {return maxspeed;}

    public void setSpeed(double speed) {
        try {
            con.do_job_set(Vehicle.setSpeed(id, speed));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
