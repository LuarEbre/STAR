package sumo.sim;

import java.util.*;

/**
 * The Class for all Routes of the Simulation
 * Different to other Objects, there is no RouteWrap, instead every RouteOperation is handled here
 * @author simonr
 */
public class RouteList {

    private final Map<String, List<String>> allRoutes;
    private  XML xmlReader;

    /**
     * Constructor for RouteList
     * uses the rou.xml to read all Routes
     * @param rouXmlFilePath
     * @throws Exception
     */
    public RouteList(String rouXmlFilePath) throws Exception {

        // parssing the xml file
        xmlReader = new XML(rouXmlFilePath);
        // map of routes(using getRoutes from XML class)
        allRoutes = xmlReader.getRoutes();

    }

    /**
     * Returns all Routes as a Hashmap of their Ids and their Path
     * @return Map<String,List<String> allRoutes
     */
    public Map<String, List<String>> getAllRoutes() {
        return allRoutes;
    }

    /**
     * Returns the ID of every Route
     * @return allRouteIds
     */
    public String[] getAllRoutesID() {
        String[] ret = new String[allRoutes.size()+1];
        int i = 0;
        for (String key : allRoutes.keySet()) {
            ret[i] = key;
            i++;
        }

        return ret;
    }

    /**
     * Prints every Route
     * used for debugging
     */
    public void printRouteList() {
        System.out.println("Route list:");
        for(String key : allRoutes.keySet()) {
            System.out.println(key + ": " + allRoutes.get(key));
            System.out.println(allRoutes.get(key));
        }
    }

    /**
     * Generates a new Route based on a Start and End Junction
     * New Route has optimal path of Start to End
     * adds new Route to RouteList and to rou.xml
     * @param start ID of startJunction
     * @param end ID of endJunction
     * @param routeID ID of new Route
     * @param jl the JunctionList
     */
    public void generateRoute(String start, String end, String routeID, JunctionList jl) {

        for (JunctionWrap jw : jl.getJunctions()) {
            jw.setDistance(Double.MAX_VALUE);
            jw.setPredecessor(null);
        }

        JunctionWrap startNode = jl.getJunction(start);
        JunctionWrap endNode = jl.getJunction(end);

        if (startNode == null || endNode == null) {
            throw new RuntimeException("Start or End Junction does not exist!");
        }

        startNode.setDistance(0.0);

        PriorityQueue<JunctionWrap> queue = new PriorityQueue<>(Comparator.comparingDouble(JunctionWrap::getDistance));

        queue.add(startNode);

        while (!queue.isEmpty()) {
            JunctionWrap u = queue.poll();
            if (u.getDistance() > jl.getJunction(u.getID()).getDistance())
                continue;
            if (u == endNode)
                break;
            for (String neighborID : jl.getAdjacentVertexes(u.getID())) {
                JunctionWrap v = jl.getJunction(neighborID);
                if (v == null) continue;
                double alt = u.getDistance() + u.distanceTo(v);
                if (alt < v.getDistance()) {
                    v.setDistance(alt);
                    v.setPredecessor(u.getID());
                    queue.add(v);
                }
            }
        }

        System.out.println("Dijkstra finished. End node: " + endNode.getID());
        System.out.println("Distance to end: " + endNode.getDistance());
        System.out.println("Predecessor of end: " + endNode.getPredecessor());

        LinkedList<String> junctionPath = new LinkedList<>();

        for (JunctionWrap step = endNode; step != null; step = jl.getJunction(step.getPredecessor())) {
            junctionPath.addFirst(step.getID());
        }

        List<String> edgeList = new ArrayList<>(junctionPath.size() - 1);

        for (int i = 0; i < junctionPath.size() - 1; i++) {

            String from = junctionPath.get(i);
            String to   = junctionPath.get(i + 1);

            String edgeID = jl.findEdgeID(from, to);

            if (edgeID == null) {
                throw new RuntimeException("Edge not found between: " + from + " → " + to);
            }

            edgeList.add(edgeID);
        }

        if (edgeList.isEmpty()) {
            throw new RuntimeException("Route " + routeID + " is empty – cannot write to SUMO!");
        }

        System.out.println("Junction Path: " + junctionPath); // Ist diese Liste leer?
        System.out.println("Junction Path Size: " + junctionPath.size());

        allRoutes.put(routeID, edgeList);
        xmlReader.newRoute(routeID, edgeList);
    }

    /**
     * Get a specific Route by its ID
     * @param id
     * @return Route
     */
    public List<String> getRouteById(String id) {
            return allRoutes.get(id);
        }

    /**
     * Checks if RouteList is empty
     * @return boolean
     */
    //getter for routecount(use in logic to check if any route is availabl)
    public boolean isRouteListEmpty() {
        return allRoutes.isEmpty();
    }
}