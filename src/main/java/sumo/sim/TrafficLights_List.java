package sumo.sim;

import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Trafficlight;
import java.util.List;
import java.util.LinkedList;


public class TrafficLights_List {
    private final List<TrafficWrap> TrafficLight_list = new LinkedList<>(); // List of TrafficLights
    private final SumoTraciConnection con; // main connection created in main wrapper

    public TrafficLights_List(SumoTraciConnection con) {
        this.con = con;
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Trafficlight.getIDList()); // returns string array
            for (String id : list) {
                TrafficLight_list.add(new TrafficWrap(id, con)); // every existing id in .rou is created as TrafficWrap + added in List
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TrafficWrap getTL_action(String id) {
        int i = 0;
        for (TrafficWrap tl : TrafficLight_list) {
            if (tl.getId().equals(id)) { // searching for TrafficLight object
                return tl;
            }
        }
        return null; // if not existent
    }

    public void printIDs() {

    }

}
