package sumo.sim;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StreetList {
    // List of streets (like TL_List)
    private final ArrayList<Street> streets = new ArrayList<>();
    private int count;
    private SumoTraciConnection connection;

    public StreetList(SumoTraciConnection con) {
        try {
            XML xml = new XML(WrapperController.getCurrentNet());
            Map<String, List<String>> data = xml.getStreetsData();

            this.connection = con;
            for(Map.Entry<String, List<String>> entry : data.entrySet()) {
                streets.add(new Street(entry.getKey(),entry.getValue(), con));
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

    public List<Street> getStreets() {
        return streets;
    }

    public void testPrint() {
        for (Street s : streets) {
            System.out.println(s.getFromJunction());
            System.out.println(s.getToJunction());
        }
    }

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

    public void updateStreets(){
        for (Street s : streets) {
            s.updateStreet();
        }
    }
}
