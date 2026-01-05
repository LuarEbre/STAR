package sumo.sim;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * author
 */

public class WrapperController {
    // connections
    private final SumoTraciConnection connection;
    private final GuiController guiController;
    // lists
    private StreetList sl;
    private TrafficLightList tl;
    private VehicleList vl;
    private JunctionList jl;
    private TypeList typel;
    private RouteList rl;

    private boolean terminated;
    private ScheduledExecutorService executor;
    private int delay = 50;
    private boolean paused;
    private double simTime;
    private XML netXml;

    // Sumo configs (temporary)

    //public static String currentNet = "src/main/resources/SumoConfig/RedLightDistrict/redlightdistrict.net.xml";
    //public static String currentRou = "src/main/resources/SumoConfig/RedLightDistrict/redlightdistrict.rou.xml";

    //public static String currentNet = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt_kfz.net.xml";
    //public static String currentRou = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt_routes_only.xml";

    // public static String currentNet = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.net.xml";
    // public static String currentRou = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.rou.xml";

    public static String currentNet = "src/main/resources/SumoConfig/rugmap/RugMap.net.xml";
    public static String currentRou = "src/main/resources/SumoConfig/rugmap/RugMap.rou.xml";

    //public static String currentNet = "src/main/resources/SumoConfig/Map_2/test.net.xml";
    //public static String currentRou = "src/main/resources/SumoConfig/Map_2/test.rou.xml";

    /**
     * The constructor of the Wrapper controller.
     *
     * @param guiController
     */
    public WrapperController(GuiController guiController) {
        // Select Windows (.exe) or UNIX binary based on static function Util.getOSType()
        String sumoBinary = Util.getOSType().equals("Windows")
                // using sumo-gui for visualisation now, will later be replaced by our own rendered map
                ? "src/main/resources/Binaries/sumo.exe"
                : "src/main/resources/Binaries/sumo";

        // config knows both .rou and .net XMLs
        //String configFile = "src/main/resources/SumoConfig/RedLightDistrict/redlightdistrict.sumocfg";
        //String configFile = "src/main/resources/SumoConfig/Map_2/test.sumocfg";
        String configFile = "src/main/resources/SumoConfig/rugmap/rugmap.sumocfg";
        // String configFile = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.sumocfg";
        // create new connection with the binary and map config file
        this.connection = new SumoTraciConnection(sumoBinary, configFile);
        this.guiController = guiController;
        this.terminated = false;
        this.paused = true;
        this.simTime = 0;
        connectionConfig();
    }

    /**
     * Initial setup to initiate server connection.
     */
    public void connectionConfig() {
        connection.addOption("start", "true");
        //connection.addOption("quit-on-end", "true");
        try {
            connection.runServer(8813); // preventing random port

            // Connection has been established
            System.out.println("Connected to Sumo.");
            // initializing all lists
            vl = new VehicleList(connection);
            sl = new StreetList(this.connection);
            tl = new TrafficLightList(connection, sl);
            jl = new JunctionList(connection, sl);
            typel = new TypeList(connection);
            rl = new RouteList(currentRou);
            typel = new TypeList(connection);

            tl.updateAllCurrentState(); // important for rendering

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        start();
    }

    /**
     * Starts/Continues the simulation.
     * If the connection is closed it will terminate immediate.
     */
    public void start() { // maybe with connection as argument? closing connection opened prior
        executor = Executors.newSingleThreadScheduledExecutor(); // creates scheduler thread, runs repeatedly
        executor.scheduleAtFixedRate(() -> {
            if (paused || terminated) return;

            if (connection.isClosed()) {
                terminate(); // if connection is closed terminate instantly
                return;
            }
            try {
                doStepUpdate(); // sim step
            } catch (IllegalStateException e) {
                terminate();
            }

            }, 0, delay, TimeUnit.MILLISECONDS); // initial delay, delay, unit
    }

    // methods controlling the simulation / also connected with the guiController

    /**
     * Used by {@link GuiController} to add Vehicles
     * @param amount How many Vehicles will spawn
     * @param type Sets type based on existing types in .rou XML
     * @param route Sets route
     * @param color Color based on Hex code
     */
    public void addVehicle(int amount, String type, String route, Color color) { // int number, String type, Color color ,,int amount, String type, String route
        // used by guiController
        // executes addVehicle from WrapperVehicle
        vl.addVehicle(amount, type, route, color);
    }

    /**
     * Changes delay based on "delay" argument and reruns executor thread with new delay.
     * @param delay
     */
    public void changeDelay(int delay) {
        this.delay = delay;
        if (!executor.isShutdown() && executor!= null) {
            executor.shutdown();
            start();
        }
    }

    /**
     * Sets the paused parameter to false, so that the simulation can continue.
     */
    public void startSim() {
        paused = false;
    }

    /**
     * Sets the paused parameter to true. The simulation will be halted.
     */
    public void stopSim() {
        paused = true;
    }

    /**
     * Performs one simulation step and gui simulation step.
     * All important updates are done here -> e.g. vl.updateAllVehicles()
     */
    public void doStepUpdate() {
        // updating gui and simulation
        try {
            connection.do_timestep();
            vl.updateAllVehicles();
            tl.updateAllCurrentState();
            //vl.printVehicles();
            simTime = (double) connection.do_job_get(Simulation.getTime()); // exception thrown here needs fix
            if (!terminated) {
                Platform.runLater(guiController::doSimStep); // gui sim step (connected with wrapperCon)
            }
        } catch (Exception e) {
            terminate();
            throw new RuntimeException(e);
        }

    }

    /**
     * Terminates the simulation.
     */
    public void terminate() {
        paused = false; // else executor would not terminate
        terminated = true; // Flag to stop new logic

        if (executor != null) {
            // no longer allow new tasks to be scheduled
            executor.shutdown();
            try {
                // awaitTermination returns TRUE if termination occurs within delay ms, giving the simulation time to finalize current step
                // otherwise it returns FALSE, in which case we immediately run shutdownNow(), risking errors
                if (!executor.awaitTermination(delay, TimeUnit.MILLISECONDS)) {
                    // force kill if it's stuck
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
        try {
            connection.close();
        } catch (Exception e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Spread the amount of vehicles determined by the stress test setting evenly across all existing routes
     * @param amount number of cars (set in Stress Test Menu)
     * @param color {@link Color}
     * @param type Type ID (defaults to "DEFAULT_VEHTYPE" if null)
     */
    public void StressTest(int amount, Color color, String type) {
        Map<String, List<String>> Routes = rl.getAllRoutes();
        int amount_per = amount/Routes.size();
        type = (type == null) ? "DEFAULT_VEHTYPE" : type;
        for(String key : Routes.keySet()) {
            addVehicle(amount_per, "DEFAULT_VEHTYPE", key, color);
        }
    }

    /**
     * Returns the duration of the phase of which the selected traffic light is currently on
     *
     * @param tlID
     * @return e.g.: [g,r,y,80] -> state , last element is duration
     */
    public String[] getTlStateDuration(String tlID) {
        String [] ret = new String[tl.getTL(tlID).getCurrentState().length/2 + 2]; // 2 extra values: dur, remain
        int j = 0;
        for (int i=0; i<ret.length-2; i++) {
            ret[i] = tl.getTL(tlID).getCurrentState()[j];
            j += 2; // 0,2,4,8
        }
        ret[ret.length-2] = ""+(tl.getTL(tlID).getDuration());
        ret[ret.length-1] = ""+(tl.getTL(tlID).getNextSwitch());

        return ret; // [g,r,y,80] -> state , last element is duration
    }

    /**
     * Sets the duration of the phase the traffic light is currently on.
     * @param tlid
     * @param duration
     */
    public void setTlSettings(String tlid, int duration) {
        tl.getTL(tlid).setPhaseDuration(duration);
        double check = tl.getTL(tlid).getDuration();
        System.out.println("Duration: " + check);

    }

    // getter

    public static String getCurrentNet(){ return currentNet; }
    public double getTime() { return simTime; }
    public int getDelay() { return delay; }
    public JunctionList getJunctions() { return jl; }
    public StreetList getStreets() { return sl; }
    public VehicleList getVehicles() { return vl; }
    public TrafficLightList getTrafficLights() { return tl; }
    public String[] getTypeList() { return typel.getAllTypes(); }
    public String[] getRouteList() { return rl.getAllRoutesID(); }
    public String[] getTLids() { return tl.getIDs(); }
    public boolean isRouteListEmpty() { return rl.isRouteListEmpty(); }
    public int updateCountVehicle() { return vl.getExistingVehCount(); }
    public int getAllVehicleCount() { return vl.getCount(); }
    public RouteList getRoutes()  { return rl; }
}