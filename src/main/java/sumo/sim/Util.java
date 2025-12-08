package sumo.sim;

import com.sun.jdi.connect.spi.TransportService;

import java.util.*;

public class Util {

    // used for initial selection of binary path
    public static String getOSType() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows")) {
            return "Windows";
        }
        else {
            return "Other";
        }
    }

    public static RouteWrap generate_route(String start, String end, Junction_List jl) {

        // --- Initialize ---
        for (JunctionWrap jw : jl.getJunctions()) {
            jw.setDistance(Double.MAX_VALUE);
            jw.setPredecessor(null);
        }

        JunctionWrap startNode = jl.getJunction(start);
        JunctionWrap endNode = jl.getJunction(end);

        if (startNode == null || endNode == null) {
            System.err.println("Start or End Junction does not exist!");
            return new RouteWrap(Collections.emptyList());
        }

        startNode.setDistance(0);

        PriorityQueue<JunctionWrap> queue = new PriorityQueue<>(
                Comparator.comparingDouble(JunctionWrap::getDistance)
        );

        queue.add(startNode);

        // --- Dijkstra ---
        while (!queue.isEmpty()) {

            JunctionWrap u = queue.poll();

            if (u == endNode) break;

            for (String neighbourID : jl.getAdjacentVertexes(u.getID())) {

                JunctionWrap v = jl.getJunction(neighbourID);
                if (v == null) continue;

                double weight = u.distance_to(v);
                double alt = u.getDistance() + weight;

                if (alt < v.getDistance()) {
                    v.setDistance(alt);
                    v.setPredecessor(u.getID());
                    queue.add(v);
                }
            }
        }

        // RECONSTRUCT NODE PATH (Junctions)
        List<String> junction_Path = new LinkedList<>();
        JunctionWrap step = endNode;

        while (step != null) {
            junction_Path.add(0, step.getID());
            step = jl.getJunction(step.getPredecessor());
        }

        // RECONSTRUCT EDGE LIST
        List<String> edge_List = new ArrayList<>();

        for (int i = 0; i < junction_Path.size() - 1; i++) {
            String from = junction_Path.get(i);
            String to = junction_Path.get(i + 1);

            String edgeID = jl.findEdgeID(from, to);

            if (edgeID == null) {
                System.err.println("⚠️ Edge not found between " + from + " → " + to);
                continue;
            }

            edge_List.add(edgeID);

        }

        return new RouteWrap(edge_List);
    }


}