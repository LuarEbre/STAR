package sumo.sim.objects;

import it.polito.appeal.traci.SumoTraciConnection;
import sumo.sim.data.XML;
import sumo.sim.logic.WrapperController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages an {@link ArrayList} of {@link Street} objects.
 */

public class StreetList {
    private final ArrayList<Street> streets = new ArrayList<>();

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(StreetList.class.getName());

    /**
     * Initializes the {@link Street} objects inside the List via {@link XML#readAllEdges()}
     * @param con an instance of {@link SumoTraciConnection}
     */
    public StreetList(SumoTraciConnection con) {
        try {
            XML xml = new XML(WrapperController.getCurrentNet());
            Map<String, String[]> data = xml.readAllEdges();

            for (Map.Entry<String, String[]> entry : data.entrySet()) {
                String id = entry.getKey();
                String from = entry.getValue()[0];
                String to = entry.getValue()[1];
                try {
                    // if id is not known -> error , needs to be checked in other lists too
                    Street s = new Street(id, from, to, con);
                    streets.add(s);
                } catch (RuntimeException e) {
                    logger.log(Level.WARNING, "Failed to initialize Streets", e);
                    // System.out.println("Info: Skipping Ghost Edge '" + id + "' (not inside SUMO sim).");
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize Streets and Data", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows for batch updating of Streets
     */
    public void updateStreets(){
        for (Street s : streets) {
            s.updateStreet();
        }
    }

    // getter

    public List<Street> getStreets() {
        return streets;
    }

    public Street getStreet(String id) {
        for (Street s : streets) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    public Street getStreetBasedOnLane(String laneID){
        for(Street s : streets){
            if(s.getStreetBasedOnLane(laneID) != null){
                return s;
            };
        }
        return null;
    }
}
