package sumo.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        String[] ret = new String[allRoutes.size()];

        ret =  allRoutes.keySet().toArray(ret);

        return ret;
    }

    public List<String> getRouteById(String id) {
            return allRoutes.get(id);
        }

    //getter for routecount(use in logic to check if any route is availabl)
    public boolean isRouteListEmpty() {
        return allRoutes.isEmpty();
    }
}