package sumo.sim;

import java.util.*;

public class RouteList {
    // stores all routes: key = route ID(e.g. "r_0.0", value = RouteWhrap-object
    private final Map<String, RouteWrap> routes = new HashMap<>();

    // stores the edge IDs which ar defined as starting points (set for fast checking)
    private final Set<String> validStartEdgeIDs = new HashSet<>();

    private final XML routesXML; // instance of xml.java

    /**
     * Konstruktor, der die Routendatei lädt und die validen Startpunkte extrahiert.
     * @param routesFilePath Der Dateipfad zu Ihrer frankfurt_routes_only.xml
     */
    public RouteList(String routesFilePath) throws Exception {
        this.routesXML = new XML(routesFilePath);
        loadRoutesFromXML();
    }

    /**
     * Lädt Routen aus der XML-Datei, nutzt die getRoutes() Methode der XML-Klasse,
     * und füllt die Map mit den RouteWrap-Objekten.
     */
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
    /**
     * Gibt eine Liste aller Kanten-IDs zurück, auf denen ein Fahrzeug gestartet werden kann.
     * Ihr GUI kann diese Liste zur Validierung oder für ein Dropdown-Menü verwenden.
     * @return Liste der gültigen Start-Kanten-IDs.
     */
    public List<String> getAllValidStartEdgeIDs() {
        return new ArrayList<>(validStartEdgeIDs);
    }

    /**
     * Prüft, ob eine Kante als gültiger Startpunkt für eine in der XML definierte Route dient.
     * @param edgeId Die zu prüfende Kanten-ID.
     * @return true, wenn ein Fahrzeug auf dieser Kante gestartet werden darf.
     */
    public boolean isValidStartEdge(String edgeId) {
        return validStartEdgeIDs.contains(edgeId);
    }

    /**
     * Gibt die Route-ID zurück, die mit der gewählten Startkante beginnt.
     * TraCI benötigt diese Route-ID, um das Fahrzeug korrekt einzufügen.
     * @param startEdgeId Die Kante, auf der das Auto starten soll.
     * @return Die Route-ID (String) oder null, wenn keine Route dort beginnt.
     */
    public String getRouteStartingWithEdge(String startEdgeId) {
        // searching the routes and return ID of the first route which starts with this edge
        for (Map.Entry<String, RouteWrap> entry : routes.entrySet()) {
            if (entry.getValue().getStartEdgeID().equals(startEdgeId)) {
                return entry.getKey(); // return the route ID
            }
        }
        return null; // no route found which starts at this edge
    }

    /**
     * Gibt die gesamte Map der geladenen Routen zurück.
     */
    public Map<String, RouteWrap> getRoutes() {
        return routes;
    }
}