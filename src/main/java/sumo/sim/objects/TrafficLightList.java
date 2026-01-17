package sumo.sim.objects;

import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Trafficlight;
import sumo.sim.data.XML;
import sumo.sim.logic.WrapperController;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for managing all TrafficLights
 * @author simonr
 */
public class TrafficLightList {
    private final ArrayList<TrafficLightWrap> trafficlights = new ArrayList<>(); // List of TrafficLights
    private final SumoTraciConnection con; // main connection created in main wrapper
    private final StreetList streetList;

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(TrafficLightList.class.getName());

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
                try {
                    TrafficLightWrap tl = new TrafficLightWrap(id, attributes, con);
                    trafficlights.add(tl);

                } catch (Exception e) {
                    logger.log(Level.FINE, "Failed to initialize Traffic Light List", e);
                    System.out.println(e.getMessage()); // fails if not known in Sumo and skips tl
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize Traffic Light List and Data", e);
            throw new RuntimeException(e);
        }
        setAllControlledStreets();
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
            logger.log(Level.FINE, "Failed to set all Controlled Streets", e);
            throw new RuntimeException(e);
        }
    }

    public void deselectAll() {
        for(TrafficLightWrap tl : trafficlights) {
            tl.deselect();
        }
    }

    public void adaptiveUpdate() {
        for (TrafficLightWrap tl : trafficlights) {
            tl.adaptiveStateUpdate();
        }
    }

    // getter

    public TrafficLightWrap getTL(String id) {
        for (TrafficLightWrap tl : trafficlights) {
            if (tl.getId().equals(id)) { // searching for TrafficLight object
                return tl;
            }
        }
        return null; // if not existent
    }

    public String[] getIDs() {
        String [] ret = new String[trafficlights.size()];
        int i = 0;
        for (TrafficLightWrap tl : trafficlights) {
            ret[i] = tl.getId();
            i++;
        }
        return ret;
    }

    public HashMap<String, Integer> getCurrentGYR() {
        HashMap<String, Integer> gyr = new HashMap<>();
        gyr.put("G", 0);
        gyr.put("Y", 0);
        gyr.put("R", 0);

        for (TrafficLightWrap tl : trafficlights) {
            String temp = tl.getCurrentStateString();

            if(temp.contains("g") || temp.contains("G")) {
                gyr.put("G", gyr.get("G") + 1);
            }

            if(temp.contains("y") || temp.contains("Y")) {
                gyr.put("Y", gyr.get("Y") + 1);
            }

            if(temp.contains("r") || temp.contains("R")) {
                gyr.put("R", gyr.get("R") + 1);
            }
        }

        return gyr;
    }

    public ArrayList<TrafficLightWrap> getTrafficlights() {
        return trafficlights;
    }
    public int getCount() { return trafficlights.size(); }

}
