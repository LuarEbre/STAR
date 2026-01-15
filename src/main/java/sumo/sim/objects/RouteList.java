package sumo.sim.objects;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.objects.SumoStage;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import sumo.sim.data.XML;
import sumo.sim.logic.WrapperController;
import sumo.sim.util.Util;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class for all Routes of the Simulation
 * Different to other Objects, there is no RouteWrap, instead every RouteOperation is handled here
 * @author simon kevin
 */
public class RouteList {

    private final Map<String, List<String>> allRoutes;
    private XML xmlReader;
    private final SumoTraciConnection con;
    private final WrapperController controller;

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(RouteList.class.getName());

    /**
     * Constructor for RouteList
     * uses the rou.xml to read all Routes
     * @param rouXmlFilePath
     * @throws Exception
     */
    public RouteList(String rouXmlFilePath, SumoTraciConnection con, WrapperController controller) throws Exception {

        this.controller = controller;
        this.con = con;
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
        String[] ret = new String[allRoutes.size()];
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

    public boolean addRoute(String start, String end, String routeID) {
        SumoStringList route = new SumoStringList();
        //route.addAll(edgeList);
        SumoStage routeResult;
        try {
            routeResult = (SumoStage) con.do_job_get(Simulation.findRoute(start,end,"", 0 , 0));
        } catch (Exception e) {
            System.err.println("Critical error in findRoute!");
            e.printStackTrace();
            return false;
        }

        if (routeResult == null || routeResult.edges == null || routeResult.edges.isEmpty()) {
            logger.log(Level.WARNING, "Route Creation failed!");
            return false;
        }
        route.addAll(routeResult.edges);
        for (String s : route) {
            System.out.println(s);
        }

        // adding in Sumo
        // check if routeID duplicate
        routeID = Util.checkRouteDuplicate(allRoutes, routeID);

        try {
            con.do_job_set(Route.add(routeID, route));
            logger.log(Level.INFO, "Route added: " + routeID);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to add route", e);
            return false;
        }

        allRoutes.put(routeID, route);
        controller.updateRoutes();
        return true;
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