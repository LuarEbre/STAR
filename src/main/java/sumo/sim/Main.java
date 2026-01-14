package sumo.sim;

import javafx.application.Application;
import java.io.IOException;

public class Main {
    // Initializing logging tool
    /**
     * Main class to start the application -> launch is performed
     * @param args
     */
    public static void main(String[] args) {
        //Set up Logger
        StarLogger.setupLogger();

        // Main program to run the entire simulation
            //LOG.log(Level.INFO,"Launching application...");
            Application.launch(GuiApplication.class);
    }
}