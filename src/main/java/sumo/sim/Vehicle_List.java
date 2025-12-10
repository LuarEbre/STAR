package sumo.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.scene.paint.Color;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Vehicle_List {
    private final ArrayList<VehicleWrap> vehicles = new ArrayList<>(); // List of Vehicles
    private final SumoTraciConnection con;// main connection created in main wrapper
    private int count; // vehicles in list, latest car number: "v"+ count
    // needs possible routes maybe? for car creation

    public Vehicle_List(SumoTraciConnection con) {
        this.count = 0;
        this.con = con;
    }

    public void addVehicle(int n, String type, String route, Color color) { // more arguments later? maybe overloaded methods with different args.
        try {
            for (int i=0; i<n; i++) {
                con.do_job_set(Vehicle.addFull("v" + count, route, type, // ids -> latest car id
                        "now", "0", "0", "0",
                        "current", "max", "current", "",
                        "", "", 0, 0)
                );
                vehicles.add(new VehicleWrap("v" + count, con, type, route, color)); // adds new vehicle
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

    public boolean exists(String ID) {
        return this.getVehicle(ID).exists();
    }

    public void updateAllVehicles() {
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Vehicle.getIDList()); // all current cars
            for (VehicleWrap v : vehicles) {
                if(list.contains(v.getID())) {
                    v.setExists(true);
                    v.updateVehicle();
                }
                else v.setExists(false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Point2D.Double> getAllPositions() {
        ArrayList<Point2D.Double> positions = new ArrayList<>();
        for (VehicleWrap v : vehicles) {
            positions.add(v.getPosition());
        }
        return positions;
    }

    public void printVehicles() {
        for (VehicleWrap v : vehicles) {
            if(v.exists()) {
                Point2D.Double pos = v.getPosition();

                System.out.printf(
                        // forces US locale, making double values be separated via period, rather than comma
                        Locale.US,
                        // print using format specifiers, 2 decimal places for double values, using leading 0s to pad for uniform spacing
                        "             %s: type =  %s, speed = %f, position = (%06.2f | %06.2f), angle = %06.2f, avgSpeed = %f, accel = %f%n               waited %.0fs, active for %ds, stopped %d times, alive for %ds%n",
                        v.getID(),
                        v.getType(),
                        v.getSpeed(),
                        pos.x,
                        pos.y,
                        v.getAngle(),
                        v.getAvgSpeed(),
                        v.getAccel(),
                        v.getWaitingTime(),
                        v.getActiveTime(),
                        v.getnStops(),
                        v.getTotalLifetime()
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

    public ArrayList<VehicleWrap> getVehicles() {
        return vehicles;
    }
}
