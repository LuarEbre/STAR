package sumo.sim.util;

/**
 * Custom Simulation Exception, extends RuntimeException
 * @author simonr
 * @see RuntimeException
 */
public class SimulationException extends RuntimeException {

    public SimulationException(String message) {
        super(message);
        this.printStackTrace();
    }

    public SimulationException(String message, String detailedMessage,  Throwable cause) {
        super(message);
        System.err.println(detailedMessage);
        this.printStackTrace();
    }
}
