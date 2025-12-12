package sumo.sim;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.objects.SumoLink;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoTLSPhase;
import de.tudresden.sumo.objects.SumoTLSProgram;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;

public class TrafficLightWrap { // extends JunctionWrap later maybe?
    private final SumoTraciConnection con;
    private final String id;
    private final Set<Street> controlledStreets;

    private String type; // types: static, actuated, delay based, offline, special, rail signal
    // program :
    // offset : adjusts start time of phase cycle
    // program id :
    // <tlLogic id="J11" type="static" programID="0" offset="0"> ---> can have multiple of this with same TL id diff p id
    //        <phase duration="42" state="GGrrrGGg"/>
    //
    // </tlLogic>

    private int phase; // G = green priority , g , y, r , u = red_yellow , o = off;
    //String[] phaseNames = {"NS_Green", "EW_Green", "All_Red"}; <- North x south, east x west
    private int duration; // time
    private final Point2D.Double position; // position as a junction
    String [] stateArray;
    private final List<SumoLink> controlledLinks;
    private final List<String> incomingLanes;

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

            this.controlledLinks = (List<SumoLink>) con.do_job_get(Trafficlight.getControlledLinks(id));
            update_TL();
            //getCurrentState();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void setCurrentState() {
        int currentPhaseIndex = getPhaseNumber(); // which state the tl is in -> applies to all controlled tl
        // -> state differs from index to index (index is controlled lanes that have tl)
        String currentState;
        // links.get(0).from
        try {
            currentState = (String) con.do_job_get(Trafficlight.getRedYellowGreenState(this.id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        stateArray = new String[currentState.length()*2]; // saves state in arr -> to get indices
        for (int i = 0; i < currentState.length(); i+=2 ) {
            int sumoIndex = i/2; // to not skip values
            stateArray[i] = currentState.charAt(sumoIndex) + ""; // every current state e.g = Grrryy (length definded)
            stateArray[i+1] = controlledLinks.get(sumoIndex).from; // index i -> i+1 = lane
            //System.out.println("Index " + (i) + stateArray[i] + " controls"  + stateArray[i+1]); // -> phase duration defined
            // [G, lane_G ,y , lane_y , r, lane_r ] format
        }
       // System.out.println(id);

    }

    public String[] getCurrentState() {
        return stateArray;
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
        //printControlledStreets();
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

    public String getProgram() {
        try {
            return (String) con.do_job_get(Trafficlight.getProgram(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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