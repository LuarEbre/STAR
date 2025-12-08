package sumo.sim;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.objects.SumoGeometry;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.LinkedList;

public class Street {
    private double maxSpeed; // same attributes as in .net
    private final SumoTraciConnection con;
    private final String id;
    private final ArrayList<LaneWrap> lanes = new ArrayList<>(); // List of lanes in street

    private String fromJunction; //start
    private String toJunction; //end
    // get startPoint to get EndPoint : draw(startPoint, endPoint)
    XML xml = null;

    public Street(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
        try {
            xml = new XML(WrapperController.get_current_net());
            this.fromJunction = xml.get_from_junction(id);
            this.toJunction = xml.get_to_junction(id);
            int laneCount = (Integer) con.do_job_get(Edge.getLaneNumber(id));
            for (int i = 0; i < laneCount; i++) {
                String currentLaneID = id + "_" + i; // street id_lane
                lanes.add(new LaneWrap(currentLaneID, con, id));
                System.out.println("Lane hinzugefügt: " + currentLaneID + " zu Edge: " + id);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<LaneWrap> getLanes() {
        return lanes;
    }

    public String getId() {
        return id;
    }

    public String getFromJunction() {
        return fromJunction;
    }

    public Street getStreet() { return this; }

    public String getToJunction() {
        return toJunction;
    }

}
