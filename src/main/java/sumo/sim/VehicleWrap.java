package sumo.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.*;
import java.awt.geom.Point2D;

public class VehicleWrap {
    private final String id;
    private final String type;
    private final SumoTraciConnection con;
    //private final SumoColor color;
    private boolean marked;

    private double speed;
    private double maxspeed;
    private double accel;
    private double decel;
    private Point2D.Double position;
    private double angle;
    private int routeId; // which route is assigned

    public VehicleWrap(String id , SumoTraciConnection con, String type) {
        this.id = id;
        this.con = con;
        this.type = type;
        updateVehicle();
    }

    public void updateVehicle(){ // updates attributes each step
        try {
            this.speed = (double)con.do_job_get(Vehicle.getSpeed(id)); // returns SumoCommand, which is then performed by do_job_get
            SumoPosition2D pos2D = (SumoPosition2D)con.do_job_get(Vehicle.getPosition(id)); // casted on SumoPosition2d
            this.position = new Point2D.Double(pos2D.x, pos2D.y); // SumoPosition values stored in Point2d object
            this.angle = (double)con.do_job_get(Vehicle.getAngle(id));
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

    public void setSpeed(double speed) {
        try {
            con.do_job_set(Vehicle.setSpeed(id, speed));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
