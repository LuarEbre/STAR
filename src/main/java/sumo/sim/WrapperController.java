package sumo.sim;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// Main Controller class connecting everything and running the sim.
public class WrapperController {
    // Colors for printing , to be removed later
    public static final String RED = "\u001B[31m";
    public static final String RESET = "\u001B[0m"; // white
    private final SumoTraciConnection connection;
    private final GuiController guiController;
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

    //public static String currentNet = "src/main/resources/SumoConfig/RedLightDistrict/redlightdistrict.net.xml";
    //public static String currentRou = "src/main/resources/SumoConfig/RedLightDistrict/redlightdistrict.rou.xml";

    //public static String currentNet = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt_kfz.net.xml";
    //public static String currentRou = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt_routes_only.xml";

    public static String currentNet = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt..net.xml";
    public static String currentRou = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.rou.xml";

    public WrapperController(GuiController guiController) {
        // Select Windows (.exe) or UNIX binary based on static function Util.getOSType()
        String sumoBinary = Util.getOSType().equals("Windows")
                // using sumo-gui for visualisation now, will later be replaced by our own rendered map
                ? "src/main/resources/Binaries/sumo.exe"
                : "src/main/resources/Binaries/sumo";

        // config knows both .rou and .net XMLs
        //String configFile = "src/main/resources/SumoConfig/Map_1/test5.sumocfg";
        //String configFile = "src/main/resources/SumoConfig/RedLightDistrict/redlightdistrict.sumocfg";
        //String configFile = "src/main/resources/SumoConfig/Map_3/test6.sumocfg";
        String configFile = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.sumocfg";
        // create new connection with the binary and map config file
        this.connection = new SumoTraciConnection(sumoBinary, configFile);
        this.guiController = guiController;
        this.terminated = false;
        this.paused = true;
        this.simTime = 0;
        connectionConfig();
    }

    public void connectionConfig() {
        // add various connection options
        //connection.addOption("delay", "50");
        connection.addOption("start", "true");
        connection.addOption("quit-on-end", "true");
        try {
            connection.runServer(8813);

            // Connection has been established
            System.out.println("Connected to Sumo.");
            vl = new VehicleList(connection);
            sl = new StreetList(this.connection);
            tl = new TrafficLightList(connection, sl);
            jl = new JunctionList(connection, sl);
            typel = new TypeList(connection);
            rl = new RouteList(currentRou);
            typel = new TypeList(connection);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        start();
    }

    public void start() { // maybe with connection as argument? closing connection opened prior
        executor = Executors.newSingleThreadScheduledExecutor(); // creates scheduler thread
        executor.scheduleAtFixedRate(() -> {
            if (!paused) {
                if (terminated) {
                    executor.shutdownNow();
                    return;
                }
                try {
                    double timeSeconds = (double) connection.do_job_get(Simulation.getTime());
                    doStepUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            }, 0, delay, TimeUnit.MILLISECONDS); // initialdelay, delay, unit
    }

    // methods controlling the simulation / also connected with the guiController

    public void addVehicle(int amount, String type, String route, Color color) { // int number, String type, Color color ,,int amount, String type, String route
        // used by guiController
        // executes addVehicle from WrapperVehicle
        vl.addVehicle(amount, type, route, color);
    }

    public void changeDelay(int delay) {
        this.delay = delay;
        if (!executor.isShutdown() && executor!= null) {
            executor.shutdown();
            start();
        }
    }

    public void startSim() {
        paused = false;
    }

    public void stopSim() {
        paused = true;
    }

    public void doStepUpdate() {
        // updating gui and simulation
        try {
            connection.do_timestep();
            vl.updateAllVehicles();
            tl.updateAllCurrentState();
            //vl.printVehicles();
            simTime = (double) connection.do_job_get(Simulation.getTime());
            Platform.runLater(guiController::doSimStep);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    // fixed Exceptions thrown by Simulation when trying to close during step
    // closing can still throw exceptions on JavaFX Application Thread caused by trying to render while simulation is closed (java.lang.IllegalStateException: connection is closed)
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

    // getter

    public static String getCurrentNet(){
        return currentNet;
    }

    public double getTime() {
        return simTime;
    }

    public int getDelay() {
        return delay;
    }

    public JunctionList getJunctions() {
        return jl;
    }

    public StreetList getStreets() {
        return sl;
    }

    public VehicleList getVehicles() {
        return vl;
    }

    public TrafficLightList getTrafficLights() {
        return tl;
    }

    public String[] getTypeList() {
        return typel.getAllTypes();
    }

    public String[] getRouteList() {
        return rl.getAllRoutesID();
    }

    public boolean isRouteListEmpty() {
        return rl.isRouteListEmpty();
    }

    //setter

    public String[] setTypeList() {
        return typel.getAllTypes();
    }
}