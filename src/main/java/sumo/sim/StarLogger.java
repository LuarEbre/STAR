package sumo.sim;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.*;
import java.util.logging.Logger;

public class StarLogger {

    public static void setupLogger() {
        try {

           // LogManager.getLogManager().readConfiguration(new FileInputStream("mylogging.properties"));

            Logger rootLogger = Logger.getLogger("");

            for (Handler h : rootLogger.getHandlers()) {
                rootLogger.removeHandler(h);
            }

            // FileHandler
            FileHandler fileHandler = new FileHandler("src/main/resources/Logs/logger.log", 2_000_000, 5, true);
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
