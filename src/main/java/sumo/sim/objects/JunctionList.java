package sumo.sim.objects;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds every JunctionWrap Object
 * @author simonr
 */
public class JunctionList {
    private final ArrayList<JunctionWrap> junctions = new ArrayList<>(); // List of TrafficLights

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(JunctionList.class.getName());

    /**
     *
     * @param con
     */
    public JunctionList(SumoTraciConnection con) {
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Junction.getIDList()); // returns string array
            for (String id : list) {
                junctions.add(new JunctionWrap(id, con)); // every existing id in .rou is created as TrafficWrap + added in List
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load JunctionList", e);
            throw new RuntimeException(e);
        }
    }

    // getter

    public JunctionWrap getJunction(String id) {
        for (JunctionWrap jw : junctions) {
            if (jw.getID().equals(id)) {
                return jw;
            }
        }
        return null;
    }

    public double getMinPosX(){
        double minX = Double.MAX_VALUE; // max value so the first element is always the smallest, still needs check if list is empty
        for (JunctionWrap jw : junctions) {
            if (jw.getPosition().x < minX) minX = jw.getPosition().x;
        }
        return minX;
    }

    public double getMinPosY(){
        double minY = Double.MAX_VALUE;
        for (JunctionWrap jw : junctions) {
            if (jw.getPosition().y < minY) minY = jw.getPosition().y;
        }
        return minY;
    }

    public double getMaxPosY(){
        double maxY = -Double.MAX_VALUE;
        for (JunctionWrap jw : junctions) {
            if (jw.getPosition().y > maxY) maxY = jw.getPosition().y;
        }
        return maxY;
    }

    public double getMaxPosX(){
        double maxX = -Double.MAX_VALUE;
        for (JunctionWrap jw : junctions) {
            if (jw.getPosition().x > maxX) maxX = jw.getPosition().x;
        }
        return maxX;
    }


    public double getCenterPosX() {
        if (junctions.isEmpty()) return 0;

        double minX = Double.MAX_VALUE; // max possible so always next element min
        double maxX = -Double.MAX_VALUE;

        for (JunctionWrap jw : junctions) {
            double x = jw.getPosition().getX();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
        }

        return (minX + maxX) / 2;
    }

    public double getCenterPosY() {
        if (junctions.isEmpty()) return 0;

        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (JunctionWrap jw : junctions) {
            double y = jw.getPosition().getY();
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        return (minY + maxY) / 2;
    }

    public ArrayList<JunctionWrap> getJunctions() {
        return junctions;
    }

}
