package sumo.sim.util;

/**
 * Custom Rendering Exception, extends RuntimeException
 * @author simonr
 * @see RuntimeException
 */
public class RenderingException extends RuntimeException {

    public RenderingException(String message) {
        super(message);
    }

    public RenderingException(String message, String detailedMessage,  Throwable cause) {
        super(message, cause);
    }
}
