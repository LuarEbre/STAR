package sumo.sim;

import java.util.*;

public class RouteList {

    private final Map<String, List<String>> allRoutes;

    public RouteList(String rouXmlFilePath) throws Exception {

        // parssing the xml file
        XML xmlReader = new XML(rouXmlFilePath);

        // map of routes(using getRoutes from XML class)
        allRoutes = xmlReader.getRoutes();

    }

    public Map<String, List<String>> getAllRoutes() {
        return allRoutes;
    }

    public String[] getAllRoutesID() {
        String[] ret = new String[allRoutes.size()+1];
        ret[0] = "CUSTOM";
        int i = 1;
        for (String key : allRoutes.keySet()) {
            ret[i] = key;
            i++;
        }

        return ret;
    }

    public void addRoute(String id, List<String> edges) {
        allRoutes.put(id, edges);
    }

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
                System.err.println("Edge not found between: " + from + " → " + to);
                return;
            }

            edgeList.add(edgeID);
        }

        addRoute(routeID, edgeList);
    }

    public List<String> getRouteById(String id) {
            return allRoutes.get(id);
        }

    //getter for routecount(use in logic to check if any route is availabl)
    public boolean isRouteListEmpty() {
        return allRoutes.isEmpty();
    }
}