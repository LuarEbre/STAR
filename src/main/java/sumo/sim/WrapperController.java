package sumo.sim;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.geom.Point2D;
import java.util.Locale;

// Main Controller class connecting everything and running the sim.
public class WrapperController {
    // Colors for printing , to be removed later
    public static final String RED = "\u001B[31m";
    public static final String RESET = "\u001B[0m"; // white
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";

    static void main(String[] args) {

        // Select Windows (.exe) or UNIX binary based on static function Util.getOSType()
        String sumoBinary = Util.getOSType().equals("Windows")
                // using sumo-gui for visualisation now, will later be replaced by our own rendered map
                ? "src/main/resources/Binaries/sumo-gui.exe"
                : "src/main/resources/Binaries/sumo-gui";

        // config knows both .rou and .net XMLs
        //String configFile = "src/main/resources/SumoConfig/Map_1/test5.sumocfg";
        String configFile = "src/main/resources/SumoConfig/Map_2/test.sumocfg";
        //String configFile = "src/main/resources/SumoConfig/Map_3/test6.sumocfg";

        // create new connection with the binary and map config file
        SumoTraciConnection connection = new SumoTraciConnection(sumoBinary, configFile);

        try {
            // add various connection options
            connection.addOption("delay", "50");
            connection.addOption("start", "true");
            connection.addOption("quit-on-end", "true");
            connection.runServer(8813);
            // Connection has been established
            System.out.println("Connected to Sumo.");

            // initialize list with all existing vehicles and traffic lights from .rou / .net xml files
            TrafficLights_List t1 = new TrafficLights_List(connection);
            Vehicle_List v1 = new Vehicle_List(connection);

            // spawn 50 vehicles of type "t_0"
            v1.addVehicle(50, "t_0"); // type t_0 (can be chosen)

            // iterator for steps
            int step = 0;
            // run simulation for 400 steps = 400 seconds
            while (step <= 400) { // short demo

                // get current time
                double timeSeconds = (double) connection.do_job_get(Simulation.getTime()); // to be replaced by SumoTraciConnectionWrapper method
                // print out current time
                System.out.println(RED + "Time: " + timeSeconds + RESET);
                // print out all vehicles currently on screen
                v1.updateAllVehicles();
                v1.printVehicles();

                // do a single step through the simulation
                connection.do_timestep();
                step++;
            }
            // print all traffic light data (later featuring phases of incoming edges)
            t1.printALL();
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        } finally {
            // Close the simulation in any case
            connection.close();
        }
    }
}