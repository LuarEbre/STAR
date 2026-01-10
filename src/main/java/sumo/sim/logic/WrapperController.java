package sumo.sim.logic;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import sumo.sim.*;
import sumo.sim.objects.*;
import sumo.sim.util.Util;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import java.io.File;
import java.util.ArrayList;
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
    private SumoTraciConnection connection;
    private final GuiController guiController;
    private final SumoMapManager mapManager;
    // lists
    private StreetList sl;
    private TrafficLightList tl;
    private VehicleList vl;
    private JunctionList jl;
    private TypeList typel;
    private RouteList rl;

    // simulation
    private boolean terminated;
    private ScheduledExecutorService executor;
    private int delay = 50;
    private boolean paused;
    private double simTime;
    //private XML netXml;

    // config
    private SumoMapConfig mapConfig;
    public static String currentNet = null;
    public static String currentRou = null;
    public String sumoBinary;

    //Logger
    private static final Logger logger = Logger.getLogger(WrapperController.class.getName());

    /**
     * The constructor of the Wrapper controller.
     *
     * @param guiController
     */
    public WrapperController(GuiController guiController,  SumoMapManager mapManager) {
        // Select Windows (.exe) or UNIX binary based on static function Util.getOSType()
        sumoBinary = Util.getOSType().equals("Windows")
                // using sumo-gui for visualisation now, will later be replaced by our own rendered map
                ? "src/main/resources/Binaries/sumo.exe"
                : "src/main/resources/Binaries/sumo";

        // config knows both .rou and .net XMLs
        mapConfig = mapManager.getConfig("Frankfurt"); // Frankfurt, TestMap
        String configFile = mapConfig.getConfigPath().toString();
        currentNet = mapConfig.getNetPath().toString();
        currentRou = mapConfig.getRouPath().toString();

        // create new connection with the binary and map config file
        this.connection = new SumoTraciConnection(sumoBinary,configFile);
        this.guiController = guiController;
        this.mapManager = mapManager;
        this.terminated = false;
        this.paused = true;
        this.simTime = 0;

        // initial setup to initiate server connection and start sim
        initializeSimulationStart();
    }

    private void initializeSimulationStart() {
        connection.addOption("start", "true");

        try {
            connection.runServer(8813); // preventing random port
            System.out.println("Connected to Sumo.");

            vl = new VehicleList(connection);
            sl = new StreetList(this.connection);
            tl = new TrafficLightList(connection, sl);
            jl = new JunctionList(connection, sl);
            typel = new TypeList(connection);
            rl = new RouteList(currentRou, connection, this);

            tl.updateAllCurrentState(); // important for rendering
            start();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start Sumo Simulation", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts/Continues the simulation.
     * If the connection is closed it will terminate immediate.
     */
    public void start() { // maybe with connection as argument? closing connection opened prior
        if (executor != null && !executor.isShutdown()) {
            return;
        }
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
                logger.log(Level.WARNING, "Failed to do a Simulation Step", e);
                terminate();
            }

            }, 0, delay, TimeUnit.MILLISECONDS); // initial delay, delay, unit
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
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) { // waits 2 sec
                    // force kill if it's stuck
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Failed to shutdown executor", e);
                executor.shutdownNow();
            }
        }
        // Close Sumo connection
        if (connection != null && !connection.isClosed()) {
            try {
                connection.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to close connection", e);
                System.err.println("Error while closing connection: " + e.getMessage());
                throw new RuntimeException();
            }
        }
    }

    // methods controlling the simulation / also connected with the guiController

    /**
     * Changes delay based on "delay" argument and reruns executor thread with new delay.
     * @param delay
     */
    public void changeDelay(int delay) {
        this.delay = delay;
        if (!executor.isShutdown() && executor!= null) {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        terminated = false;
        paused = false;
        start();
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
            sl.updateStreets();
            //vl.printVehicles();
            simTime = (double) connection.do_job_get(Simulation.getTime()); // exception thrown here needs fix
            if (!terminated) {
                Platform.runLater(guiController::doSimStep); // gui sim step (connected with wrapperCon)
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update Sim Step", e);
            terminate();
            throw new RuntimeException(e);
        }

    }

    public void mapSwitch(String mapName) {
        System.out.println("Map Switch to: " + mapName);
        paused = true;
        terminated = true; // stops executor

        // New thread for loading to prevent freezing
        new Thread(() -> {
            terminate(); // instantly forces termination of current thread

            // time to close and open old port
            try { Thread.sleep(500); } catch (InterruptedException e) {
                // should have something here
            }

            // load new config
            try {
                mapConfig= mapManager.getConfig(mapName);
                currentNet = mapConfig.getNetPath().toString();
                currentRou = mapConfig.getRouPath().toString();

                this.connection = new SumoTraciConnection(sumoBinary, mapConfig.getConfigPath().toString()); // new connection
                simTime = 0;

                // prevents new sim from starting instantly
                paused = true;
                Platform.runLater(guiController::doSimStep);

                terminated = false;

                // start again
                initializeSimulationStart();

                // initializes new map
                Platform.runLater(() -> guiController.initializeCon(this));

            } catch (Exception e) {
                logger.log(Level.FINE, "Failed to switch maps", e);
                System.err.println("Error switching maps: " + e.getMessage());
            }
        }).start();
    }

    // Main Button features

    /**
     * Used by {@link GuiController} to add Vechicles
     * @param amount How many Vehicles will spawn
     * @param type Sets type based on existing types in .rou XML
     * @param route Sets route
     * @param color Color based on Hex code
     */
    public void addVehicle(int amount, String type, String route, Color color) {
        // used by guiController, executes addVehicle from WrapperVehicle
        vl.addVehicle(amount, type, route, color);
    }

    public void addRoute(String start, String end, String id) {
        rl.addRoute(start,end,id);
    }

    public void updateRoutes() {
        Platform.runLater(guiController::initializeDropDowns);
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
     * Sets the duration of the phase the traffic light is currently on.
     * @param tlid
     * @param duration
     */
    public void setTlSettings(String tlid, int duration) {
        tl.getTL(tlid).setPhaseDuration(duration);
        //double check = tl.getTL(tlid).getDuration();
    }

    public void setTrafficLightDurationPermanently(String id, int phaseIndex,  double newDuration ) {
        tl.getTL(id).setPhaseDurationPermanently(phaseIndex, newDuration);
    }

    public void setTrafficLightPhase(String id, int phaseIndex) {
        tl.getTL(id).setPhaseNumber(phaseIndex);
    }

    // getter

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

    public String getChosenMap(){
        List<String> maps = mapManager.getNames();
        for(String key : maps) {
           if(mapManager.getConfig(key).isChosen()) {
               return key;
           }
        }
        // if no map is selected (error) automatically choose Map1
        mapSwitch("Frankfurt");
        return "Frankfurt";
    }

    public SelectableObject getSelectedObject() {
        // works because only one object can be selected at a time
        for(VehicleWrap v : vl.getVehicles()) if (v.isSelected()) return v;
        for(TrafficLightWrap tl : tl.getTrafficlights()) if (tl.isSelected()) return tl;
        return null;
    }

    public String[] getTLCurrentState(String id) {return tl.getTL(id).getCurrentState();}
    public static String getCurrentNet(){ return currentNet; }
    public double getTime() { return simTime; }
    public int getDelay() { return delay; }
    public JunctionList getJunctions() { return jl; }
    public StreetList getStreets() { return sl; }
    public VehicleList getVehicles() { return vl; }
    public TrafficLightList getTrafficLights() { return tl; }
    public RouteList getRoutes()  { return rl; }
    public String getPhaseAtIndex(String id, int index) {return tl.getTL(id).getPhaseAtIndex(index);}
    public int getCurrentTLPhaseIndex(String id) {return tl.getTL(id).getPhaseNumber();}
    public List<TrafficLightPhase> getTrafficLightPhases(String id){ return tl.getTL(id).getTrafficLightPhases();}

    // safe getter
    public String[] getTypeList() { return (typel != null) ? typel.getAllTypes() : new String[0]; } // returns empty array if null
    public String[] getRouteList() { return (rl != null) ? rl.getAllRoutesID() : new String[0]; }
    public String[] getTLids() { return (tl != null) ? tl.getIDs() : new String[0]; }
    public String[] getSelectableStreets() {return sl.getSelectableStreets(); }
    public boolean isRouteListEmpty() { return (rl == null) || rl.isRouteListEmpty(); }
    public int updateCountVehicle() { return (vl != null) ? vl.getExistingVehCount() : 0; }
    public int getAllVehicleCount() { return (vl != null) ? vl.getCount() : 0; }
    
}