package sumo.sim;

public class RenderingException extends RuntimeException {

    public RenderingException(String message) {
        super(message);
        this.printStackTrace();
    }

    public RenderingException(String message, String detailedMessage,  Throwable cause) {
        super(message);
        System.err.println(detailedMessage);
        this.printStackTrace();
    }
}
