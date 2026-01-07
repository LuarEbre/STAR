package sumo.sim;

import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.objects.SumoGeometry;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.util.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;
import java.util.LinkedList;

/**
 * A wrapper of {@link Lane} allowing for instancing of individual Lanes within a Street
 */
public class LaneWrap {

    private final String laneID;
    private final SumoTraciConnection connection;
    private final String edgeID;
    private int index; // which TL state index -> if -1 -> no incoming lane for tl
    private final double[] shapeX;
    private final double[] shapeY;
    private final double width;
    private final double length;

    /**
     * Initializes this lane's shape via {@link SumoTraciConnection#do_job_get(SumoCommand)}
     * @param laneID Lane ID
     * @param connection an instance of {@link SumoTraciConnection}
     * @param edgeID Edge in which the Lane lies
     */
    public LaneWrap(String laneID,SumoTraciConnection connection, String edgeID) {
        this.laneID = laneID;
        this.connection = connection;
        this.edgeID = edgeID;
        this.index = -1; // if no change -> no index for tl
        //System.out.println("Lane:"+laneID+"edge: "+edgeID);
        SumoGeometry geometry;
        try {
            geometry = (SumoGeometry) this.connection.do_job_get(Lane.getShape(laneID));
            LinkedList<SumoPosition2D> coords = geometry.coords;
            int numPoints = coords.size();
            shapeX = new double[numPoints];
            shapeY = new double[numPoints];
            length = (double) this.connection.do_job_get(Lane.getLength(laneID));

            for (int i = 0; i < numPoints; i++) {
                SumoPosition2D point = coords.get(i);
                shapeX[i] = point.x;
                shapeY[i] = point.y;
            }

            width = (double) connection.do_job_get(Lane.getWidth(laneID));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return this Lane's ID.
     */
    public String getLaneID() {
        return laneID;
    }
    /**
     * @return Array of Y coordinates defining the shape of this lane.
     */
    public double[] getShapeX(){
        return shapeX;
    }
    /**
     * @return Array of Y coordinates defining the shape of this lane.
     */
    public double[] getShapeY(){
        return shapeY;
    }
    /**
     * @return Width of this lane.
     */
    public double getWidth(){
        return width;
    }

    public double getLength(){return length;}
}
