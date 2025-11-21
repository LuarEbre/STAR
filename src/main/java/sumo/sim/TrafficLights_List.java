package sumo.sim;

import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Trafficlight;
import java.util.List;
import java.util.LinkedList;


public class TrafficLights_List {
    private final List<TrafficWrap> TL_list = new LinkedList<>();
    private final SumoTraciConnection con;

    public TrafficLights_List(SumoTraciConnection con) {
        this.con = con;
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Trafficlight.getIDList()); // returns string array
            for (String id : list) {
                TL_list.add(new TrafficWrap(id, con));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TrafficWrap getTL_action(String id) {
        int i = 0;
        for (TrafficWrap tl : TL_list) {
            if (tl.getId().equals(id)) {
                return tl; // sofort zurückgeben
            }
        }
        return null;
    }

    public void printIDs() {

    }

}
