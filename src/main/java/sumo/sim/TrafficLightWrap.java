package sumo.sim;

import de.tudresden.sumo.cmd.Trafficlight;
import it.polito.appeal.traci.SumoTraciConnection;

public class TrafficLightWrap {
    SumoTraciConnection conn;
    private String id;
    private String state; // color switch e.g. "GGGrrrrr"
    //String[] phaseNames = {"NS_Green", "EW_Green", "All_Red"}; <- North x south, east x west
    private int duration; // time

    public TrafficLightWrap(String id, SumoTraciConnection conn){
        this.id = id;
        this.conn = conn;
    }

    public TrafficLightWrap(){
    }

    public int getPhaseNumber() {
        try {
            return (int) conn.do_job_get(Trafficlight.getPhase(id)); // gets phase of tl = 1, 2, 3
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPhaseName() {
        try {
            return (String) conn.do_job_get(Trafficlight.getPhaseName(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getDuration() {
        try {
            return (int) conn.do_job_get(Trafficlight.getPhaseDuration(id)); // gets phase of tl = 1, 2, 3
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getId() {return id;}

}
