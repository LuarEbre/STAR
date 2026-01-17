package sumo.sim;

import javafx.application.Application;
import sumo.sim.gui.GuiApplication;
import sumo.sim.util.StarLogger;

public class Main {
    /**
     * Main class to start the application -> launch is performed
     * @param args
     */
    public static void main(String[] args) {
        //Set up Logger
        StarLogger.setupLogger();

        // Main program to run the entire simulation
        Application.launch(GuiApplication.class);
    }
}