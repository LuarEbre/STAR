package sumo.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.LinkedList;
import java.util.List;

public class Vehicle_List {
    private final List<VehicleWrap> vehicles = new LinkedList<>(); // List of Vehicles
    private final SumoTraciConnection con; // main connection created in main wrapper
    private int ids; // latest car id
    // needs possible routes maybe? for car creation

    public Vehicle_List(SumoTraciConnection con) {
        this.ids = 0;
        this.con = con;
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Vehicle.getIDList()); // returns string array
            for (String id : list) {
                vehicles.add(new VehicleWrap(id, con)); // every existing id in .rou is created as TrafficWrap + added in List
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addVehicle(String type) { // more arguments later? maybe overloaded methods with different args.
        try {
            con.do_job_set(Vehicle.addFull("v" + ids, "r1", type, // ids -> latest car id
                    "now", "0", "0", "max",
                    "current", "max", "current", "",
                    "", "", 0, 0)
            );
            vehicles.add(new VehicleWrap("v" + ids, con)); // adds new vehicle
            ids++; // increment to prevent identical car ids
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public VehicleWrap getTL_action(String id) {
        int i = 0;
        for (VehicleWrap v : vehicles) {
            if (v.getID().equals(id)) { // searching for TrafficLight object
                return v;
            }
        }
        return null; // if not existent
    }
}
