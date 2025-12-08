package sumo.sim;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.*;

public class Junction_List {
    //private final Set<JunctionWrap> junctions = new HashSet<>();
    private final ArrayList<JunctionWrap> junctions = new ArrayList<>(); // List of TrafficLights
    private int count;
    private Map<String, Set<String>> adjacency = new HashMap<>();
    private final Street_List streets;


    public Junction_List(SumoTraciConnection con, Street_List streets) {
        this.streets = streets;
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Junction.getIDList()); // returns string array
            for (String id : list) {
                junctions.add(new JunctionWrap(id, con)); // every existing id in .rou is created as TrafficWrap + added in List
                count++;
            }
            update_adjacency();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void update_adjacency(){

        adjacency.clear();

        for(JunctionWrap jw : junctions){
            adjacency.put(jw.getID(), new HashSet<>());
        }

        for (Street s : streets.getStreets()){
            String from = s.getFromJunction();
            String to = s.getToJunction();

            if (from == null || to == null) continue;
            if (!adjacency.containsKey(from)) continue;
            if (!adjacency.containsKey(to)) continue;

            adjacency.get(from).add(to);
            adjacency.get(to).add(from);
        }
    }


    public void print_adjacency() {
        for (String j : adjacency.keySet()) {
            System.out.println(j + " → " + adjacency.get(j));
        }
    }


    public JunctionWrap getJunction(String id) {
        for (JunctionWrap jw : junctions) {
            if (jw.getID().equals(id)) {
                return jw;
            }
        }
        return null;
    }

    public Set<String> getAdjacentVertexes(String junctionID) {
        return adjacency.getOrDefault(junctionID, Collections.emptySet()) ;
    }

    public String findEdgeID(String from, String to) {
        for (Street s : streets.getStreets()) {

            String sFrom = s.getFromJunction();
            String sTo = s.getToJunction();

            if (sFrom == null || sTo == null)
                continue;

            if (sFrom.equals(from) && sTo.equals(to)) {
                return s.getId();
            }

            if (s.getId().startsWith(":")) continue;

        }
        return null;
    }

    public double getMinPosX(){
        double minX = 0.0;
        for (JunctionWrap jw : junctions) {
            if (jw.getPosition().x < minX) minX = jw.getPosition().x;
        }
        return minX;
    }

    public double getMinPosY(){
        double minY = 0.0;
        for (JunctionWrap jw : junctions) {
            if (jw.getPosition().y < minY) minY = jw.getPosition().y;
        }
        return minY;
    }

    public ArrayList<JunctionWrap> getJunctions() {
        return junctions;
    }


}
