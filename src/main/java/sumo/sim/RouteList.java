package sumo.sim;

import java.util.*;

public class RouteList {
    // stores all routes: key = route ID(e.g. "r_0.0", value = RouteWrap-object
    private final Map<String, RouteWrap> routes = new HashMap<>();

    // stores the edge IDs which ar defined as starting points (set for fast checking)
    private final Set<String> validStartEdgeIDs = new HashSet<>();

    private final XML routesXML; // instance of xml.java

    public RouteList(String routesFilePath) throws Exception {
        this.routesXML = new XML(routesFilePath);
        loadRoutesFromXML();
    }

    private void loadRoutesFromXML() throws Exception {
        Map<String, List<String>> loadedRoutes = routesXML.getRoutes();

        for (Map.Entry<String, List<String>> entry : loadedRoutes.entrySet()) {
            String id = entry.getKey();
            List<String> edges = entry.getValue();

            if (!edges.isEmpty()) {
                // 1. store the route
                RouteWrap routeWrap = new RouteWrap(edges);
                routes.put(id, routeWrap);

                // 2. Store the first edge of the route as valid starting point
                String startEdgeId = edges.get(0);
                validStartEdgeIDs.add(startEdgeId);
            }
        }

        System.out.println("RouteList loaded: " + routes.size() + " routes, "
                + validStartEdgeIDs.size() + " valid starting points found.");
    }

    // public methods for gui implementation
    public List<String> getAllValidStartEdgeIDs() {
        return new ArrayList<>(validStartEdgeIDs);
    }

    public boolean isValidStartEdge(String edgeId) {
        return validStartEdgeIDs.contains(edgeId);
    }


    public String getRouteStartingWithEdge(String startEdgeId) {
        // searching the routes and return ID of the first route which starts with this edge
        for (Map.Entry<String, RouteWrap> entry : routes.entrySet()) {
            if (entry.getValue().getStartEdgeID().equals(startEdgeId)) {
                return entry.getKey(); // return the route ID
            }
        }
        return null; // no route found which starts at this edge
    }

    public Map<String, RouteWrap> getRoutes() {
        return routes;
    }
}