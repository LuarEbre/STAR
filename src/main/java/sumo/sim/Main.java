package sumo.sim;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Vehicle;

import java.awt.geom.Point2D;
import java.util.Locale;

public class Main {
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
            connection.do_timestep();

            VehicleWrap[] cars = new VehicleWrap[50];
            TrafficLights_List t1 = new TrafficLights_List(connection);

            // do a single step so vehicles can be created

            // spawn 50 vehicles
            for (int i = 0; i < 50; i++) {
                connection.do_job_set(Vehicle.addFull("v" + i, "r1", "t_0", // vehID declared, type Id in .rou
                        "now", "0", "0", "max",
                        "current", "max", "current", "",
                        "", "", 0, 0)
                );
                cars[i] = new VehicleWrap("v" + i, connection);
                cars[i].setSpeed(10);
            }

            // run simulation for 200 steps = 200 seconds
            while (step < 200) {
                // get speed while v_0 exists
                if (step > 0 && step < 36) {
                    if (cars[0] != null) {
                        // will not be replaced by a procedure, as it is only used temporarily
                        // getPosition returns a 2D point which later has to be split into X and Y coordinates
                        Point2D.Double pos = cars[0].getPosition();
                        System.out.printf(
                            // forces US locale, making double values be separated via period, rather than comma
                            Locale.US,
                            // print using format specifiers, 2 decimal places for double values, using leading 0s to pad for uniform spacing
                            "%s: speed = %05.2f, position = (%06.2f, %06.2f), angle = %06.2f%n",
                            cars[0].getID(),
                            cars[0].getSpeed(),
                            pos.x,
                            pos.y,
                            cars[0].getAngle()
                        );
                    }
                }
                // print out current time in seconds
                double timeSeconds = (double) connection.do_job_get(Simulation.getTime());
                System.out.println("Time: " + timeSeconds);
                step++;
                connection.do_timestep();
            }
            t1.printIDs(); // prints all existing trafficlights
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        } finally {

            connection.close();
        }
    }
}