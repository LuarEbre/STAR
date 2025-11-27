package sumo.sim;

import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Trafficlight;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;


public class TrafficLights_List {
    private final List<TrafficLightWrap> trafficlights = new LinkedList<>(); // List of TrafficLights
    private final SumoTraciConnection con; // main connection created in main wrapper
    private int count;

    public TrafficLights_List(SumoTraciConnection con) {
        this.con = con;
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Trafficlight.getIDList()); // returns string array
            for (String id : list) {
                trafficlights.add(new TrafficLightWrap(id, con)); // every existing id in .rou is created as TrafficWrap + added in List
                count++;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TrafficLightWrap getTL(String id) {
        for (TrafficLightWrap tl : trafficlights) {
            if (tl.getId().equals(id)) { // searching for TrafficLight object
                return tl;
            }
        }
        return null; // if not existent
    }

    public int getCount() {
        return count;
    }

    public void printIDs() {
        int counter = 0;
        for (TrafficLightWrap tl : trafficlights) {
            System.out.println("Traffic lights "+  counter + ": " + tl.getId());
            counter++;
        }
    }

    public void printALL() { // for the demo
        for (TrafficLightWrap tl : trafficlights) {
            Point2D.Double pos = tl.getPosition();
            System.out.printf(
                    // forces US locale, making double values be separated via period, rather than comma
                    Locale.US,
                    // print using format specifiers, 2 decimal places for double values, using leading 0s to pad for uniform spacing
                    "%s: position = (%06.2f, %06.2f)%n",
                    tl.getId(),
                    pos.x,
                    pos.y
            );
        }
    }

}
