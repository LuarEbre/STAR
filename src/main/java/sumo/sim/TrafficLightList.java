package sumo.sim;

import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Trafficlight;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Class for all managing all TrafficLights
 * @author simonr
 */
public class TrafficLightList {
    private final ArrayList<TrafficLightWrap> trafficlights = new ArrayList<>(); // List of TrafficLights
    private final SumoTraciConnection con; // main connection created in main wrapper
    private final StreetList streetList;
    private int count;

    /**
     * Constructor for TrafficLightList
     * creates all TrafficLights as Objects in an ArrayList
     *
     * @param con SumoTraciConnection
     * @param s1 StreetList
     */
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

    /**
     * Get an TrafficLight by ID
     * @param id ID of TrafficLight
     * @return TrafficLightWrap
     */
    public TrafficLightWrap getTL(String id) {
        for (TrafficLightWrap tl : trafficlights) {
            if (tl.getId().equals(id)) { // searching for TrafficLight object
                return tl;
            }
        }
        return null; // if not existent
    }

    /**
     * Get all TrafficLights as an ArrayList
     * @return allTrafficLight
     */
    public ArrayList<TrafficLightWrap> getTrafficlights() {
        return trafficlights;
    }

    /**
     * Get amount of TrafficLights in TrafficLightList
     * @return amount of TrafficLights
     */
    public int getCount() { return count; }

    /**
     * Get all IDs of TrafficLights in TrafficLightList
     * @return String[] IDs
     */
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

    /**
     * Prints all TrafficLights in TrafficLightList
     */
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

    /**
     * Updates the State for all TrafficLights
     */
    public void updateAllCurrentState() {
        for (TrafficLightWrap tl : trafficlights) {
            tl.setCurrentState();
        }
    }

    /**
     * Appends all TrafficLights Attributes in a String and returns it
     * @return String of Data
     */
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

    /**
     * Set the Controlled Street for every TrafficLight
     */
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

    /**
     * Print all currently Controlled Streets of every TrafficLight
     */
    public void printAllControlledStreets() {
        for (TrafficLightWrap tl : trafficlights) {
            tl.printControlledStreets();
        }
    }

    /**
     * Does the update_TL method for every TrafficLight in TrafficLightList
     * This updates their attributes to current Simulation data
     */
    public void updateTLs(){
        for(TrafficLightWrap tl : trafficlights ){
            tl.update_TL();
        }

    }
}
