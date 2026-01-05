package sumo.sim;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.objects.SumoGeometry;
import de.tudresden.sumo.objects.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import static java.lang.Math.abs;

/**
 * Class for the Junctions
 *
 * @author simonr
 * @author LeleZss
 */
public class JunctionWrap {
    private final String id;
    private final Point2D.Double position;
    private final SumoTraciConnection con;
    private String type; // dead end , tl
    private boolean spawnable; // true -> to display start points

    private double[] shapeX;
    private double[] shapeY;
    private double minX, maxX, minY, maxY;

    private double distance = Integer.MAX_VALUE; //Used for Dijkstra Initialization
    private String predecessor = null; //Used for Dijkstra

    /**
     * Constructor for JunctionWrap.
     * Initializes all parameters
     * @param id
     * @param con
     */
    public JunctionWrap(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
        this.spawnable = false;
        try {
            SumoPosition2D pos2D = (SumoPosition2D) this.con.do_job_get(Junction.getPosition(id)); // position
            this.position = new Point2D.Double(pos2D.x, pos2D.y);

            SumoGeometry geometry = (SumoGeometry) this.con.do_job_get(Junction.getShape(id)); // return SumoGeometry
            LinkedList<SumoPosition2D> coords = geometry.coords;
            int numPoints = coords.size();
            this.shapeX = new double[numPoints];
            this.shapeY = new double[numPoints];

            for (int i = 0; i < numPoints; i++) { // every point allocated to shape X and Y
                SumoPosition2D point = coords.get(i);
                this.shapeX[i] = point.x;
                this.shapeY[i] = point.y;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the Distance of one Junction.
     * Only used for Route generation
     * @param distance
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Set the Predecessor of one Junction
     * Only used for Route generation
     * @param predecessor
     */
    public void setPredecessor(String predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * Calculate and returns the distance of this junction to Junction u
     * @param u
     * @return distance to Junction u
     */
    public double distanceTo(JunctionWrap u) {
        return abs((this.position.x + this.position.y) - (u.position.x + u.position.y));
    }


    public void calculateBounds() {
        minX = Double.MAX_VALUE;
        maxX = -Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (double x : shapeX) {
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
        }
        for (double y : shapeY) {
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

    }

    // getter

    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }


    public Point2D.Double getPosition() {
        return position;
    }
    public String getID() {
        return id;
    }
    public double getDistance() {
        return distance;
    }
    public String getPredecessor() {
        return predecessor;
    }
    public double[] getShapeX() {
        return shapeX;
    }
    public double[] getShapeY() {
        return shapeY;
    }
    public boolean isSpawnable() { return spawnable; }
}

