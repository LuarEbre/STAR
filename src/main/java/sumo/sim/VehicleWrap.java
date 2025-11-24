package sumo.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.*;
import java.awt.geom.Point2D;

public class VehicleWrap {
    private final String id;
    private String type;
    private final SumoTraciConnection con;
    private boolean marked;

    public VehicleWrap(String id , SumoTraciConnection con) {
        this.id = id;
        this.con = con;
    }

    public double getSpeed() {
        try {
            return (double)con.do_job_get(Vehicle.getSpeed(id)); // returns SumoCommand, which is then performed by do_job_get
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Point2D.Double getPosition() {
        try {
            SumoPosition2D pos2D = (SumoPosition2D)con.do_job_get(Vehicle.getPosition(id)); // casted on SumoPosition2d
            return new Point2D.Double(pos2D.x, pos2D.y); // SumoPosition values stored in Point2d object
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public double getAngle() {
        try {
            return (double)con.do_job_get(Vehicle.getAngle(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    // boolean exists(?)
}
