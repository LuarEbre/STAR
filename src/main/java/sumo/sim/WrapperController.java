package sumo.sim;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.application.Application;

import java.awt.geom.Point2D;
import java.util.Locale;

// Main Controller class connecting everything and running the sim.
public class WrapperController {
    // Colors for printing , to be removed later
    public static final String RED = "\u001B[31m";
    public static final String RESET = "\u001B[0m"; // white
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    private final GuiController guiController;
    private final SumoTraciConnection connection;

    WrapperController(SumoTraciConnection connection) {
            guiController = new GuiController(this);
            this.connection = connection;
    }

    // methods controlling the simulation / also connected with the guiController

    public double getTime() {
        try {
            return (double) connection.do_job_get(Simulation.getTime());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}