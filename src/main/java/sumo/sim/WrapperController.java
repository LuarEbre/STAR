package sumo.sim;

import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.cmd.Vehicle;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.io.IOException;
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
    private Street_List sl;
    private TrafficLights_List tl;
    private Vehicle_List vl;
    private Junction_List jl;
    private Type_List typel;
    private boolean terminated;
    private ScheduledExecutorService executor;
    private int delay = 50;
    private boolean paused;
    private double simTime;
    private XML netXml;

    public static String curr_net = "src/main/resources/SumoConfig/Map_2/test.net.xml";

    public WrapperController(GuiController guiController) {
        // Select Windows (.exe) or UNIX binary based on static function Util.getOSType()
        String sumoBinary = Util.getOSType().equals("Windows")
                // using sumo-gui for visualisation now, will later be replaced by our own rendered map
                ? "src/main/resources/Binaries/sumo.exe"
                : "src/main/resources/Binaries/sumo";

        // config knows both .rou and .net XMLs
        //String configFile = "src/main/resources/SumoConfig/Map_1/test5.sumocfg";
        String configFile = "src/main/resources/SumoConfig/Map_2/test.sumocfg";
        //String configFile = "src/main/resources/SumoConfig/Map_3/test6.sumocfg";
        //String configFile = "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.sumocfg";
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Connection has been established
        System.out.println("Connected to Sumo.");
        vl = new Vehicle_List(connection);
        sl = new Street_List(this.connection);
        tl = new TrafficLights_List(connection, sl);
        jl = new Junction_List(connection, sl);
        typel = new Type_List(connection);
        Type_List types = new Type_List(connection);
        start();
        startRenderer();
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
                    System.out.println(RED + "Time: " + timeSeconds + RESET);

                    vl.updateAllVehicles();
                    vl.printVehicles();
                    System.out.println("Delay:" + delay);

                    // Debug: Checking if onfig works properly

                    System.out.println(tl.getTrafficLightsData());

                    doStepUpdate();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            }, 0, delay, TimeUnit.MILLISECONDS); // initialdelay, delay, unit
    }

    public void startRenderer() { // maybe with connection as argument? closing connection opened prior
        AnimationTimer renderLoop = new AnimationTimer() { // javafx class -> directly runs on javafx thread
            @Override
            public void handle(long timestamp) {
                guiController.renderUpdate();
            }
        };
        renderLoop.start(); // runs 60 frames per second
    }

    // methods controlling the simulation / also connected with the guiController

    public void addVehicle() { // int number, String type, Color color ,,int amount, String type, String route
        // used by guiController
        // executes addVehicle from WrapperVehicle
        vl.addVehicle(1, "t_0"); // type t_0 (can be chosen)
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
            simTime = (double) connection.do_job_get(Simulation.getTime());
            Platform.runLater(guiController::doSimStep);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void terminate() {
        paused = false; // else executor would not terminate
        terminated = true;
        // needs exception handling , or some way to correctly terminate javafx thread
        connection.close();
    }

    // getter

    public static String get_current_net(){
        return curr_net;
    }

    public double getTime() {
        return simTime;
    }

    public int getDelay() {
        return delay;
    }

    public Junction_List get_junction() {
        return jl;
    }

    public Street_List get_sl() {
        return sl;
    }

    //setter

    public String[] setTypeList() {
        return typel.getAllTypes();
    }
}