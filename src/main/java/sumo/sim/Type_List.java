package sumo.sim;

import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.cmd.Vehicletype;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Type_List {
    // has a list of all current (and newly created) Vehicle types

    private final Map<String, Type> types = new HashMap<>(); // string key unique: type id
    private final SumoTraciConnection connection;

    /*
    type_id -> list(color,max speed)
     */
    public Type_List(SumoTraciConnection connection) {
        this.connection = connection;
        try {
            SumoStringList list = (SumoStringList) connection.do_job_get(Vehicletype.getIDList());
            for (String id : list) {
                //several type attributes should be added here
                String color = connection.do_job_get(Vehicletype.getColor(id)).toString();
                double speed = (double) connection.do_job_get(Vehicletype.getMaxSpeed(id));
                //System.out.println("id: " + id);

                Type type = new Type(id, color, speed);
                types.put(id, type); // type and hashmap same ID
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getAllTypes() {
        String[] ret = new String[types.size()];
        int i = 0;
        for (String id : types.keySet()) {
            ret[i] = id;
            i++;
        }
        return ret;
    }
}
