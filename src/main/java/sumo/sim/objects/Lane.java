package sumo.sim.objects;

import de.tudresden.sumo.objects.SumoGeometry;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.util.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper of {@link de.tudresden.sumo.cmd.Lane} allowing for instancing of individual Lanes within a Street
 */
public class Lane {

    // connection
    private final String laneID;
    private final SumoTraciConnection con;

    // rendering
    private final double[] shapeX;
    private final double[] shapeY;
    private final double width;
    private final double length;

    private double density;

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(Lane.class.getName());

    /**
     * Initializes this lane's shape via {@link SumoTraciConnection#do_job_get(SumoCommand)}
     * @param laneID Lane ID
     * @param connection an instance of {@link SumoTraciConnection}
     * @param edgeID Edge in which the Lane lies
     */
    public Lane(String laneID, SumoTraciConnection connection, String edgeID) {
        this.laneID = laneID;
        this.con = connection;
        SumoGeometry geometry;
        try {
            geometry = (SumoGeometry) this.con.do_job_get(de.tudresden.sumo.cmd.Lane.getShape(laneID));
            LinkedList<SumoPosition2D> coords = geometry.coords;
            int numPoints = coords.size();
            shapeX = new double[numPoints];
            shapeY = new double[numPoints];
            length = (double) this.con.do_job_get(de.tudresden.sumo.cmd.Lane.getLength(laneID));

            for (int i = 0; i < numPoints; i++) {
                SumoPosition2D point = coords.get(i);
                shapeX[i] = point.x;
                shapeY[i] = point.y;
            }

            width = (double) connection.do_job_get(de.tudresden.sumo.cmd.Lane.getWidth(laneID));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load Lane Geometry", e);
            throw new RuntimeException(e);
        }
    }

    // getter
    public String getLaneID() {
        return laneID;
    }
    public double[] getShapeX(){
        return shapeX;
    }
    public double[] getShapeY(){
        return shapeY;
    }
    public double getWidth(){
        return width;
    }
    public double getLength(){return length;}

    public Lane getLane(String laneID){
        if(laneID.equals(this.laneID)){
            return this;
        }
        return null;
    }

}
