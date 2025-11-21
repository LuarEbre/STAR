package sumo.sim;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Vehicle;

public class Main {
    public static void main(String[] args) {

        // Select Windows (.exe) or UNIX binary based on static function Util.getOSType()
        String sumoBinary = Util.getOSType().equals("Windows")
                // using sumo-gui for visualisation now, will later be replaced by our own rendered map
                ? "src/main/resources/Binaries/sumo-gui.exe"
                : "src/main/resources/Binaries/sumo-gui";

        // config knows both .rou and .net XMLs
        String configFile = "src/main/resources/Map 1/test5.sumocfg";

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
            VehicleWrap v = null;

            connection.do_timestep();

            // spawn 50 vehicles
            for (int i = 0; i < 50; i++) {
                connection.do_job_set(Vehicle.addFull("v" + i, "r1", "t_0", // vehID declared, type Id in .rou
                        "now", "0", "0", "max",
                        "current", "max", "current", "",
                        "", "", 0, 0)
                );
                cars[i] = new VehicleWrap("v" + i, connection);
                cars[i].setSpeed();
            }

            // run simulation for 200 steps = 200 seconds
            while (step < 200) {
                // get speed while v_0 exists
                if (step < 36 && step > 0) {
                    if (cars[0] != null) {
                        System.out.println(cars[0].getID() + "'s speed is: " + cars[0].getSpeed());
                    }
                }
                // print out current time in seconds
                double timeSeconds = (double) connection.do_job_get(Simulation.getTime());
                System.out.println("Time: " + timeSeconds);

                step++;
                connection.do_timestep();
            }
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        } finally {
            connection.close();
        }
    }
}