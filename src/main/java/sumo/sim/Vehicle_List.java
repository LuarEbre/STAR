package sumo.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.LinkedList;
import java.util.List;

public class Vehicle_List {
    private final List<VehicleWrap> vehicles = new LinkedList<>(); // List of Vehicles
    private final SumoTraciConnection con; // main connection created in main wrapper

    public Vehicle_List(SumoTraciConnection con) {
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
}
