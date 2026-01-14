package sumo.sim;

import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class Main {
    // Initializing logging tool
    public static final Logger LOG = LogManager.getLogger(Main.class.getName());
    /**
     * Main class to start the application -> launch is performed
     * @param args
     */
    public static void main(String[] args) {
        //Set up Logger
        StarLogger.setupLogger();

        // Main program to run the entire simulation
            //LOG.log(Level.INFO,"Launching application...");
            LOG.info("Launching application...");
            Application.launch(GuiApplication.class);
    }
}