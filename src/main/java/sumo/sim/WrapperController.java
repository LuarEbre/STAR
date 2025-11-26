package sumo.sim;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.geom.Point2D;
import java.util.Locale;

public class WrapperController {
    // Colors for printing , removed later
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
        String configFile = "src/main/resources/SumoConfig/Test_Map/test.sumocfg";
        //String configFile = "src/main/resources/SumoConfig/Map_1/test5.sumocfg";

        // create new connection with the binary and map config file
        SumoTraciConnection connection = new SumoTraciConnection(sumoBinary, configFile);

        try {
            // Connection has been established
            connection.addOption("delay", "50");
            connection.addOption("start", "true");
            connection.addOption("quit-on-end", "true");
            connection.runServer(8813);
            System.out.println("Connected to Sumo.");

            int step = 0;
            // do a single step so vehicles can be created
            connection.do_timestep();

            // initialize list with all existing vehicles and traffic lights from .rou / .net xml files
            TrafficLights_List t1 = new TrafficLights_List(connection);
            Vehicle_List v1 = new Vehicle_List(connection);

            // spawn 50 vehicles
            v1.addVehicle(50, "t_0"); // type t_0 (can be chosen)

            // run simulation for 360 steps = 360 seconds
            while (step < 360) { // short demo
                for (int i = 0; i < v1.getCount(); i++) { // for all vehicles in the list, later via gui without this loop
                    if (v1.exists("v"+i)) {
                        Point2D.Double pos = v1.getVehicle("v"+i).getPosition();
                        System.out.printf(
                                // forces US locale, making double values be separated via period, rather than comma
                                Locale.US,
                                // print using format specifiers, 2 decimal places for double values, using leading 0s to pad for uniform spacing
                                "%s: speed = %05.2f, position = (%06.2f, %06.2f), angle = %06.2f%n",
                                v1.getVehicle("v"+i).getID(),
                                v1.getVehicle("v"+i).getSpeed(),
                                pos.x,
                                pos.y,
                                v1.getVehicle("v"+i).getAngle()
                        );
                    }
                }
                double timeSeconds = (double) connection.do_job_get(Simulation.getTime()); // later in a SumoTraciConnection class
                System.out.println(RED + "Time: " + timeSeconds + RESET);
                step++;
                connection.do_timestep();
            }
            t1.printALL(); // all traffic light data printed (later incoming edges and outgoing)
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        } finally {
            connection.close();
        }
    }
}