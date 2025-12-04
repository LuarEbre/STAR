package sumo.sim;

import java.util.List;

public class RouteWrap {
    // has route id and all edges(street) object in that route ;
    public List<String> id;
    // list of Street associated with that route;

    public RouteWrap(List <String> id) {
        this.id = id;
    }

    public List<String> getId() {
        return id;
    }

    public void printRoute() {
        for (int i = 0; i < id.size(); i++) {
            System.out.println(id.get(i));
        }
    }
}
