package sumo.sim;

import java.util.List;

public class RouteWrap {

    //stores the edge IDs which define the route
    private final String id;
    private final List<String> edges;

    public RouteWrap(String id, List<String> edges) {
        this.id = id;
        this.edges = edges; // stores the edge IDs
    }

    // returns id of route
    public String getId() {
        return id;
    }
    // returns list of edge IDs which belong to the route
    public List<String> getEdges() {
        return edges;
    }

    // returns edge ID which is the start of the route
    public String getStartEdgeID() {
        return edges.isEmpty() ? null : edges.get(0);
    }
    @Override
    public String toString() {
        return "Route [ID=" + id + ", Kanten: " + edges.size() + "]";
    }

    public void printRoute() {
        //uses list of edges
        for (String edgeId : edges) {
            System.out.println(edgeId);
        }
    }
}
