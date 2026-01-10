package sumo.sim.logic;

import de.tudresden.sumo.cmd.Vehicletype;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TypeList {
    // has a list of all current (and newly created) Vehicle types

    private final Map<String, Type> types = new HashMap<>(); // string key unique: type id
    private final SumoTraciConnection connection;

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(TypeList.class.getName());

    /*
    type_id -> list(color,max speed)
     */
    public TypeList(SumoTraciConnection connection) {
        this.connection = connection;
        try {
            SumoStringList list = (SumoStringList) connection.do_job_get(Vehicletype.getIDList());
            for (String id : list) {
                //several type attributes should be added here

                // filtering Default_types
                if (id.equals("DEFAULT_RAILTYPE") || id.equals("DEFAULT_BIKETYPE") ||
                        id.equals("DEFAULT_CONTAINERTYPE") || id.equals("DEFAULT_PEDTYPE")) continue;

                String color = connection.do_job_get(Vehicletype.getColor(id)).toString();
                double speed = (double) connection.do_job_get(Vehicletype.getMaxSpeed(id));
                //System.out.println("id: " + id);

                Type type = new Type(id, color, speed);
                types.put(id, type); // type and hashmap same ID
            }
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to initialize Types", e);
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

    public Type getSpecificType(String id) {
        return types.get(id);
    }
}
