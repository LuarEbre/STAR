package sumo.sim;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Junction_List {
    private final ArrayList<JunctionWrap> junctions = new ArrayList<>(); // List of TrafficLights
    private int count;

    public Junction_List(SumoTraciConnection con) {
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Junction.getIDList()); // returns string array
            for (String id : list) {
                junctions.add(new JunctionWrap(id, con)); // every existing id in .rou is created as TrafficWrap + added in List
                count++;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
