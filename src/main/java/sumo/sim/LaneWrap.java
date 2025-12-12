package sumo.sim;

import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.objects.SumoGeometry;
import de.tudresden.sumo.objects.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.LinkedList;

public class LaneWrap {

    private final String laneID;
    private final SumoTraciConnection connection;
    private final String edgeID;
    private final double[] shapeX;
    private final double[] shapeY;
    private final double width;

    public LaneWrap(String laneID,SumoTraciConnection connection, String edgeID) {
        this.laneID = laneID;
        this.connection = connection;
        this.edgeID = edgeID;
        //System.out.println("Lane:"+laneID+"edge: "+edgeID);
        SumoGeometry geometry = null;
        try {
            geometry = (SumoGeometry) this.connection.do_job_get(Lane.getShape(laneID));
            LinkedList<SumoPosition2D> coords = geometry.coords;
            int numPoints = coords.size();
            shapeX = new double[numPoints];
            shapeY = new double[numPoints];

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
}
