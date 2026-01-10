package sumo.sim.objects;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.*;

/**
 * Holds every JunctionWrap Object
 * @author simonr
 */
public class JunctionList {
    //private final Set<JunctionWrap> junctions = new HashSet<>();
    private final ArrayList<JunctionWrap> junctions = new ArrayList<>(); // List of TrafficLights
    private int count;
    private Map<String, Set<String>> adjacency = new HashMap<>();
    private final StreetList streets;

    /**
     *
     * @param con
     * @param streets
     */
    public JunctionList(SumoTraciConnection con, StreetList streets) {
        this.streets = streets;
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Junction.getIDList()); // returns string array
            for (String id : list) {
                junctions.add(new JunctionWrap(id, con)); // every existing id in .rou is created as TrafficWrap + added in List
                count++;
            }
            updateAdjacency();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the Adjacence Matrix used for Route Generation
     */
    public void updateAdjacency(){

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

    /**
     * Prints the Adjacency Matrix
     */
    public void printAdjacency() {
        for (String j : adjacency.keySet()) {
            System.out.println(j + " → " + adjacency.get(j));
        }
    }

    /**
     * Get one specific Junction from the Junction List
     * @param id
     * @return Junction
     */
    public JunctionWrap getJunction(String id) {
        for (JunctionWrap jw : junctions) {
            if (jw.getID().equals(id)) {
                return jw;
            }
        }
        return null;
    }

    /**
     * Get the corresponding adjacent junctions of one junction
     * @param junctionID
     * @return Set<String> Vertexes adjacent to one junction
     */
    public Set<String> getAdjacentVertexes(String junctionID) {
        return adjacency.getOrDefault(junctionID, Collections.emptySet()) ;
    }

    /**
     * returns Street based on its from and to Junction
     * @param from
     * @param to
     * @return Street
     */
    public String findEdgeID(String from, String to) {
        for (Street s : streets.getStreets()) {
            String sFrom = s.getFromJunction();
            String sTo = s.getToJunction();
            String sId = s.getId();

            if (sFrom == null || sTo == null) continue;

            if (sId.startsWith(":")) continue;

            if (sFrom.equals(from) && sTo.equals(to)) {
                return sId;
            }

            String cleanFrom = sFrom.startsWith(":") ? sFrom.substring(sFrom.indexOf("J")) : sFrom;
            String cleanTo = sTo.startsWith(":") ? sTo.substring(sTo.indexOf("J")) : sTo;

            if (cleanFrom.equals(from) && cleanTo.equals(to)) {
                return sId;
            }
        }

        System.err.println("Edge not found from " + from + " to " + to);
        return null;
    }


    public double getMinPosX(){
        double minX = Double.MAX_VALUE; // max value so the first element is always the smallest, still needs check if list is empty
        for (JunctionWrap jw : junctions) {
            if (jw.getPosition().x < minX) minX = jw.getPosition().x;
        }
        return minX;
    }

    public double getMinPosY(){
        double minY = Double.MAX_VALUE;
        for (JunctionWrap jw : junctions) {
            if (jw.getPosition().y < minY) minY = jw.getPosition().y;
        }
        return minY;
    }

    public double getMaxPosY(){
        double maxY = -Double.MAX_VALUE;
        for (JunctionWrap jw : junctions) {
            if (jw.getPosition().y > maxY) maxY = jw.getPosition().y;
        }
        return maxY;
    }

    public double getMaxPosX(){
        double maxX = -Double.MAX_VALUE;
        for (JunctionWrap jw : junctions) {
            if (jw.getPosition().x > maxX) maxX = jw.getPosition().x;
        }
        return maxX;
    }


    public double getCenterPosX() {
        if (junctions.isEmpty()) return 0;

        double minX = Double.MAX_VALUE; // max possible so always next element min
        double maxX = -Double.MAX_VALUE;

        for (JunctionWrap jw : junctions) {
            double x = jw.getPosition().getX();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
        }

        return (minX + maxX) / 2;
    }

    public double getCenterPosY() {
        if (junctions.isEmpty()) return 0;

        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (JunctionWrap jw : junctions) {
            double y = jw.getPosition().getY();
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        return (minY + maxY) / 2;
    }

    /**
     * get all Junctions as an ArrayList
     * @return junctions
     */
    public ArrayList<JunctionWrap> getJunctions() {
        return junctions;
    }


}
