package sumo.sim;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.LinkedList;
import java.util.List;

public class Street_List {
    // List of streets (like TL_List)
    private final List<Street> streets = new LinkedList<>(); // List of TrafficLights
    private int count;
    public Street_List(SumoTraciConnection con) {
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Edge.getIDList()); // returns string array
            for (String id : list) {
                streets.add(new Street(id, con));
                count++;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Street getStreet(String id) {
        for (Street s : streets) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }
}
