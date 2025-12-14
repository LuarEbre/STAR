package sumo.sim;

import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages an {@link ArrayList} of {@link Street} objects.
 */

public class StreetList {
    // List of streets (like TL_List)
    private final ArrayList<Street> streets = new ArrayList<>();
    private int count;
    private SumoTraciConnection connection;

    /**
     * Initializes the {@link Street} objects inside the List via {@link XML#readAllEdges()}
     * @param con an instance of {@link SumoTraciConnection}
     */
    public StreetList(SumoTraciConnection con) {
        try {
            XML xml = new XML(WrapperController.getCurrentNet());
            Map<String, String[]> data = xml.readAllEdges();

            this.connection = con;
            for (Map.Entry<String, String[]> entry : data.entrySet()) {
                String id = entry.getKey();
                String from = entry.getValue()[0];
                String to = entry.getValue()[1];
                streets.add(new Street(id, from, to, con));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a single {@link Street} based on ID
     * @param id Street ID
     */
    public Street getStreet(String id) {
        for (Street s : streets) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * @return {@link List} of {@link Street}
     */
    public List<Street> getStreets() {
        return streets;
    }

    /**
     * Outdated method for debugging purposes, prints every Street's from and to Junction
     */
    public void testPrint() {
        for (Street s : streets) {
            System.out.println(s.getFromJunction());
            System.out.println(s.getToJunction());
        }
    }

    /**
     * @return The densest Street inside the List
     */
    public Street getDensest(){
        Street densest = new Street("", connection);
        densest.setDensity(0);

        for (Street s : streets) {
            if(s.getDensity()>densest.getDensity()){
                densest = s;
            }
        }
        return densest;
    }

    /**
     * Allows for batch updating of Streets
     */
    public void updateStreets(){
        for (Street s : streets) {
            s.updateStreet();
        }
    }
}
