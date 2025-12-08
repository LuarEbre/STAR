package sumo.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Locale;

public class Vehicle_List {
    private final ArrayList<VehicleWrap> vehicles = new ArrayList<>(); // List of Vehicles
    private final SumoTraciConnection con; // main connection created in main wrapper
    private int count; // vehicles in list, latest car number: "v"+ count
    // needs possible routes maybe? for car creation

    public Vehicle_List(SumoTraciConnection con) {
        this.count = 0;
        this.con = con;
    }

    public void addVehicle(int n, String type) { // more arguments later? maybe overloaded methods with different args.
        try {
            for (int i=0; i<n; i++) {
                con.do_job_set(Vehicle.addFull("v" + count, "r1", type, // ids -> latest car id
                        "now", "0", "0", "0",
                        "current", "max", "current", "",
                        "", "", 0, 0)
                );
                vehicles.add(new VehicleWrap("v" + count, con, type)); // adds new vehicle
                count++; // increment to prevent identical car ids
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

    public void updateAllVehicles() {
        for (int i = 0; i < this.count; i++) {
            if (this.exists("v"+i)) { // if still exists
                this.vehicles.get(i).updateVehicle();
            }
        }
    }

    public void printVehicles() {
        for (int i = 0; i < this.count; i++) { // for all vehicles in the list, later via gui without this loop
            if (this.exists("v"+i)) {

                VehicleWrap currVehicle = this.getVehicle("v"+i);
                Point2D.Double pos = currVehicle.getPosition();

                System.out.printf(
                        // forces US locale, making double values be separated via period, rather than comma
                        Locale.US,
                        // print using format specifiers, 2 decimal places for double values, using leading 0s to pad for uniform spacing
                        "            %s: speed = %f, position = (%06.2f | %06.2f), angle = %06.2f, avgSpeed = %f, accel = %f%n               waited %.0fs, active for %ds, stopped %d times, alive for %ds%n",
                        currVehicle.getID(),
                        currVehicle.getSpeed(),
                        pos.x,
                        pos.y,
                        currVehicle.getAngle(),
                        currVehicle.getAvgSpeed(),
                        currVehicle.getAccel(),
                        currVehicle.getWaitingTime(),
                        currVehicle.getActiveTime(),
                        currVehicle.getnStops(),
                        currVehicle.getTotalLifetime()
                );
            }
        }
    }

    public String getVehiclesData() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.count; i++) {
            VehicleWrap currVehicle = this.getVehicle("v" + i);
            Point2D.Double pos = currVehicle.getPosition();

            sb.append(currVehicle.getID()).append(",");
            //sb.append(currVehicle.getSpeed()).append(",");
            sb.append(currVehicle.getType()).append(",");
            sb.append(currVehicle.getnStops()).append(",");
            sb.append(currVehicle.getWaitingTime()).append(",");
            sb.append(currVehicle.getMaxSpeed()).append(",");
            //sb.append(pos.x).append(",").append(pos.y).append(",");
            //sb.append(currVehicle.getAngle()).append("\n");
            sb.append("\n");

        }
        return sb.toString();
    }


    public int getCount() {
        return count;
    }
}
