package sumo.sim;

import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Trafficlight;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class TrafficLightList {
    private final ArrayList<TrafficLightWrap> trafficlights = new ArrayList<>(); // List of TrafficLights
    private final SumoTraciConnection con; // main connection created in main wrapper
    private final StreetList streetList;
    private int count;

    public TrafficLightList(SumoTraciConnection con, StreetList s1) {
        this.con = con;
        this.streetList = s1;
        try {
            XML xml = new XML(WrapperController.getCurrentNet());
            Map<String, Map<String,String>> TLData = xml.getTrafficLightsData();

            for (var entry : TLData.entrySet()) {
                String id = entry.getKey();
                Map<String, String> attributes = entry.getValue();

                trafficlights.add(new TrafficLightWrap(id, attributes, con));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setAllControlledStreets();
    }

    public TrafficLightWrap getTL(String id) {
        for (TrafficLightWrap tl : trafficlights) {
            if (tl.getId().equals(id)) { // searching for TrafficLight object
                return tl;
            }
        }
        return null; // if not existent
    }

    public ArrayList<TrafficLightWrap> getTrafficlights() {
        return trafficlights;
    }

    public int getCount() { return count; }

    public String[] getIDs() {
        int counter = 0;
        String [] ret = new String[trafficlights.size()];
        int i = 0;
        for (TrafficLightWrap tl : trafficlights) {
            ret[i] = tl.getId();
            i++;
        }
        return ret;
    }

    public void printALL() { // for the demo
        System.out.println("");
        for (TrafficLightWrap tl : trafficlights) {
            Point2D.Double pos = tl.getPosition();
            System.out.printf(
                    // forces US locale, making double values be separated via period, rather than comma
                    Locale.US,
                    // print using format specifiers, 2 decimal places for double values, using leading 0s to pad for uniform spacing
                    "Traffic Light %s: position = (%06.2f | %06.2f)%n",
                    tl.getId(),
                    pos.x,
                    pos.y
            );
        }
    }

    public void updateAllCurrentState() {
        for (TrafficLightWrap tl : trafficlights) {
            tl.setCurrentState();
        }
    }

    public String getTrafficLightsData() {
        StringBuilder sb = new StringBuilder();

        for (TrafficLightWrap tl : trafficlights) {
            Point2D.Double pos = tl.getPosition();

            sb.append(tl.getId()).append(",");
            sb.append(pos.x).append(",");
            sb.append(pos.y).append(",");
            sb.append(tl.getPhaseNumber()).append(",");

            sb.append("\n");

        }
        return sb.toString();
    }

    public void setAllControlledStreets() {
        try {
            for (TrafficLightWrap tl : trafficlights) {
                SumoStringList string = (SumoStringList) con.do_job_get(Trafficlight.getControlledLanes(tl.getId()));
                for (String s : string) {
                    String[] parts = s.split("_");
                    tl.setControlledStreets(streetList.getStreet(parts[0]));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void printAllControlledStreets() {
        for (TrafficLightWrap tl : trafficlights) {
            tl.printControlledStreets();
        }
    }

    public void updateTLs(){
        for(TrafficLightWrap tl : trafficlights ){
            tl.update_TL();
        }

    }
}
