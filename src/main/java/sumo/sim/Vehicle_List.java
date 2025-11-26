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
    private int count; // vehicles in list
    // needs possible routes maybe? for car creation

    public Vehicle_List(SumoTraciConnection con) {
        this.ids = 0;
        this.con = con;
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Vehicle.getIDList()); // returns string array
            for (String id : list) {
                vehicles.add(new VehicleWrap(id, con)); // every existing id in .rou is created as TrafficWrap + added in List
                count += 1;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addVehicle(int n, String type) { // more arguments later? maybe overloaded methods with different args.
        try {
            for (int i=0; i<n; i++) {
                con.do_job_set(Vehicle.addFull("v" + ids, "r1", type, // ids -> latest car id
                        "now", "0", "0", "0",
                        "current", "max", "current", "",
                        "", "", 0, 0)
                );
                vehicles.add(new VehicleWrap("v" + ids, con)); // adds new vehicle
                ids++; // increment to prevent identical car ids
                count += 1;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public VehicleWrap getVehicle(String id) {
        int i = 0;
        for (VehicleWrap v : vehicles) {
            if (v.getID().equals(id)) { // searching for TrafficLight object
                return v;
            }
        }
        return null; // if not existent
    }

    public void printIDs() {
        int counter = 0;
        for (VehicleWrap v : vehicles) {
            System.out.println("Vehicles "+  counter + ": " + v.getID());
        }
    }

    public boolean exists(String ID){
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Vehicle.getIDList()); // all current cars
            for (String id : list) {
               if  (id.equals(ID)) { // if car with certain id is still on the road
                   return true;
               }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false; // if despawned , delete from list?
    }

    public int getCount() {
        return count;
    }
}
