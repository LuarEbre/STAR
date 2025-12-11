package sumo.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteList {

    private final List<RouteWrap> allRoutes;

    public RouteList(String rouXmlFilePath) throws Exception {

        // parssing the xml file
        XML xmlReader = new XML(rouXmlFilePath);

        // map of routes(using getRoutes from XML class)
        Map<String, List<String>> routeMap = xmlReader.getRoutes();

        // converting to list of RouteWhrap objects
        this.allRoutes = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : routeMap.entrySet()) {
            // using RouteWhrap constructor
            RouteWrap route = new RouteWrap(entry.getKey(), entry.getValue());
            this.allRoutes.add(route);
        }

    }
    public String[] getAllRoutes() {
        String[] ret = new String[allRoutes.size()];
        for (int i = 0; i < allRoutes.size(); i++) {
            RouteWrap route = allRoutes.get(i);
            ret[i] = route.getId();
        }

        return ret;
    }

    public RouteWrap getRouteById(String id) {
        for (RouteWrap route : allRoutes) {
            if (route.getId().equals(id)) {
                return route;
            }
        }
        return null;
    }
    //getter for routecount(use in logic to check if any route is availabl)
    public boolean isRouteListEmpty() {
        return (this.allRoutes.isEmpty());
    }
}