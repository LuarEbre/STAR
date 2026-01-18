package sumo.sim.util;

/**
 * Is thrown when there is an issue with loading a config file
 */
public class MapLoadingException extends Exception {

    public MapLoadingException(String message) {
        super(message);
    }

    public MapLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
