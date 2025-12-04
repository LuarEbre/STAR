package sumo.sim;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.objects.SumoLink;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrafficLightWrap { // extends JunctionWrap later maybe?
    private final SumoTraciConnection con;
    private final String id;
    private Set<Street> controlledStreets;
    private String state; // color switch e.g. "GGGrrrrr"
    //String[] phaseNames = {"NS_Green", "EW_Green", "All_Red"}; <- North x south, east x west
    private int duration; // time
    private final Point2D.Double position; // position as a junction
    //private List<List<SumoLink>> controlledLinks; later used for defining incoming/outgoing streets

    public TrafficLightWrap(String id, SumoTraciConnection con){
        this.id = id;
        this.con = con;
        this.controlledStreets = new HashSet<>();
        try {
            SumoPosition2D pos2D = (SumoPosition2D) con.do_job_get(Junction.getPosition(id)); // position
            this.position = new Point2D.Double(pos2D.x, pos2D.y);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getPhaseNumber() {
        try {
            return (int) con.do_job_get(Trafficlight.getPhase(id)); // gets phase of tl = 1, 2, 3
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPhaseName() {
        try {
            return (String) con.do_job_get(Trafficlight.getPhaseName(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getDuration() {
        try {
            return (int) con.do_job_get(Trafficlight.getPhaseDuration(id)); // gets phase of tl = 1, 2, 3
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getId() {return id;}
    public Point2D.Double getPosition() {return position;}

    public void setControlledStreets(Street s) { this.controlledStreets.add(s); }

    public void printControlledStreets() {
        for(Street s: controlledStreets) {
            System.out.println(this.id + " controls " + s.getId());
        }
    }
}
