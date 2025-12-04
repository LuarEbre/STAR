package sumo.sim;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.application.Application;
import javafx.scene.paint.Color;

import java.awt.geom.Point2D;
import java.util.Locale;

// Main Controller class connecting everything and running the sim.
public class WrapperController {
    // Colors for printing , to be removed later
    public static final String RED = "\u001B[31m";
    public static final String RESET = "\u001B[0m"; // white
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    private final SumoTraciConnection connection;
    private final GuiController guiController;

    public WrapperController(GuiController guiController) {
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
        this.connection = new SumoTraciConnection(sumoBinary, configFile);
        this.guiController = guiController;
        start();
    }

    public void start() { // maybe with connection as argument? closing connection opened prior

        try {
            // add various connection options
            connection.addOption("delay", "50");
            connection.addOption("start", "true");
            connection.addOption("quit-on-end", "true");
            connection.runServer(8813);
            // Connection has been established
            System.out.println("Connected to Sumo.");
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        } finally {
            // Close the simulation in any case
            //connection.close();
        }
    }

    // methods controlling the simulation / also connected with the guiController

    public double getTime() {
        try {
            return (double) connection.do_job_get(Simulation.getTime());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addVehicle(int number, String type, Color color) {
        // used by guiController
        // executes addVehicle from WrapperVehicle
    }

    public void terminate() {
        connection.close();
    }


}