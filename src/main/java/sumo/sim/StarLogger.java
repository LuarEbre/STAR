package sumo.sim;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.*;
import java.util.logging.Logger;

/**
 * Custom Logger for STAR
 * @author simonr
 * @see LoggFormatter
 * @see LoggFilter
 */

public class StarLogger {

    /**
     * Initiates the Logger
     * Uses LoggFormatter and LoggFilter to format the log entries
     * Logs are saved in Logs/logger.log
     *
     * @see LoggFilter
     * @see LoggFormatter
     */
    public static void setupLogger() {
        try {

            Logger rootLogger = Logger.getLogger("");

            for (Handler h : rootLogger.getHandlers()) {
                rootLogger.removeHandler(h);
            }

            // FileHandler
            if(Files.notExists(Paths.get("Logs"))) {
                new File("Logs").mkdirs();
            }

            FileHandler fileHandler = new FileHandler("Logs/logger.log", true);
            fileHandler.setFormatter(new LoggFormatter());
            fileHandler.setFilter(new LoggFilter());

            // ConsoleHandler
            ConsoleHandler consoleHandler = new ConsoleHandler();

            rootLogger.addHandler(fileHandler);
            rootLogger.addHandler(consoleHandler);

            rootLogger.setLevel(Level.INFO);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
