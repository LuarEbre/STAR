package sumo.sim;

import java.util.List;

public class RouteWrap {

    //stores the edge IDs which define the route
    private final List<String> edges;

    public RouteWrap(List<String> edges) {
        this.edges = edges; // stores the edge IDs
    }

    // returns list of edge IDs which belong to the route
    public List<String> getEdges() {
        return edges;
    }

    // returns edge ID which is the start of the route
    public String getStartEdgeID() {
        return edges.isEmpty() ? null : edges.get(0);
    }

    public void printRoute() {
        //uses list of edges
        for (String edgeId : edges) {
            System.out.println(edgeId);
        }
    }
}