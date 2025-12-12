package sumo.sim;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.objects.SumoGeometry;
import de.tudresden.sumo.objects.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.geom.Point2D;
import java.util.*;

public class TrafficLightWrap { // extends JunctionWrap later maybe?
    private final SumoTraciConnection con;
    private final String id;
    private final Set<Street> controlledStreets;
    private int phase; // color switch e.g. "GGGrrrrr"
    //String[] phaseNames = {"NS_Green", "EW_Green", "All_Red"}; <- North x south, east x west
    private int duration; // time
    private final Point2D.Double position; // position as a junction
    //private List<List<SumoLink>> controlledLinks; later used for defining incoming/outgoing streets
    private double[] shapeX;
    private double[] shapeY;
    private List<String> incomingLanes;

    public TrafficLightWrap(String id, Map<String,String> Data, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
        this.controlledStreets = new HashSet<>();
        try {// position
            this.position = new Point2D.Double();
            this.position.x = Double.parseDouble(Data.get("x"));
            this.position.y = Double.parseDouble(Data.get("x"));

            String incLanesString = Data.get("incLanes");
            this.incomingLanes = Arrays.asList(incLanesString.split("\\s+"));

            update_TL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // setter

    public void setPhaseNumber(int index) {
        //TODO: check if index exists in TL
        try {
            con.do_job_set(Trafficlight.setPhase(id,index));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setPhaseName(String tlPhaseName) {
        try {
            con.do_job_set(Trafficlight.setPhaseName(id, tlPhaseName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setPhaseDuration(double phaseDuration) {
        try {
            con.do_job_set(Trafficlight.setPhaseDuration(id, phaseDuration));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setProgram(String programID) {
        //TODO: check for programID
        try {
            con.do_job_set(Trafficlight.setProgram(id, programID));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setRedYellowGreenState(String state) {
        //TODO: string check
        try {
            con.do_job_set(Trafficlight.setRedYellowGreenState(id, state));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setControlledStreets(Street s) {
        this.controlledStreets.add(s);
        printControlledStreets();
    }

    // getter

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

    public List<String> getIncomingLanes() {
        return incomingLanes;
    }

    public String getId() {
        return id;
    }
    public int get_Phase(){
        return this.phase;
    }
    public Point2D.Double getPosition() {
        return position;
    }
    public Set<Street> getControlledStreets() {
        return controlledStreets;
    }

    public double[] getShapeX() {
        return shapeX;
    }

    public double[] getShapeY() {
        return shapeY;
    }

    // other

    public void printControlledStreets() {
        for (Street s : controlledStreets) {
            System.out.println(this.id + " controls " + s.getId());
        }
    }

    public void update_TL() {
        try {
            this.phase = (int) con.do_job_get(Trafficlight.getPhase(this.id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}