package sumo.sim;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.geom.Point2D;

public class JunctionWrap {
    private final String id;
    private final Point2D.Double position;
    private final SumoTraciConnection con;

    public JunctionWrap(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;

        try {
            SumoPosition2D pos2D = (SumoPosition2D) this.con.do_job_get(Junction.getPosition(id)); // position
            this.position = new Point2D.Double(pos2D.x, pos2D.y);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
