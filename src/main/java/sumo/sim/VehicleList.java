package sumo.sim;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.scene.paint.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages a {@link CopyOnWriteArrayList} of {@link VehicleWrap} objects.
 * <p>
 * This makes the class thread-safe (immune to race conditions), allowing the simulation
 * to iterate over vehicles while another thread tries to add or remove vehicles.
 * </p>
 */
public class VehicleList {
    private final CopyOnWriteArrayList<VehicleWrap> vehicles = new CopyOnWriteArrayList<>(); // List of Vehicles
    private final SumoTraciConnection con;// main connection created in main wrapper
    private int count; // vehicles in list, latest car number: "v"+ count
    private int activeCount; // vehicles currently on the road network
    // needs possible routes maybe? for car creation

    /**
     * Initializes our VehicleList with a count of 0 vehicles
     * @param con an instance of {@link SumoTraciConnection}
     */
    public VehicleList(SumoTraciConnection con) {
        this.count = 0;
        this.con = con;
    }

    /**
     * Adds n vehicles to the SUMO simulation {@link SumoTraciConnection} via the native {@link Vehicle#addFull(String, String, String, String, String, String, String, String, String, String, String, String, String, int, int)}
     * @param n number of desired vehicles
     * @param type vehicle type (e.g. STANDARD_VEH)
     * @param route desired route
     */
    public void addVehicle(int n, String type, String route, Color color) { // more arguments later? maybe overloaded methods with different args.
        ArrayList<VehicleWrap> newVehicles = new ArrayList<>(n);
        try {
            for (int i=0; i<n; i++) {
                con.do_job_set(Vehicle.addFull("v" + count, route, type, // ids -> latest car id
                        "now", "0", "0", "0",
                        "current", "max", "current", "",
                        "", "", 0, 0)
                );
                //vehicles.add(new VehicleWrap("v" + count, con, type, route, color)); // adds new vehicle
                newVehicles.add(new VehicleWrap("v"+count, con, type, route, color));
                count++; // increment to prevent identical car ids
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        vehicles.addAll(newVehicles); // thread save list is really slow
    }

    /**
     * Returns a single {@link VehicleWrap} based on ID
     * @param id Vehicle ID
     */
    public VehicleWrap getVehicle(String id) {
        for (VehicleWrap v : vehicles) {
            if (v.getID().equals(id)) { // searching through VehicleWrap objects
                return v;
            }
        }
        return null; // if not found
    }

    /**
     * <p></p>Calls {@link VehicleWrap#setExists(boolean)} for each vehicle based on whether they are on the road network or not</p>
     * <p>Calls {@link VehicleWrap#updateVehicle()} for every vehicle currently on the road network</p>
     */
    public void updateAllVehicles() {
        try {
            SumoStringList list = (SumoStringList) con.do_job_get(Vehicle.getIDList());
            HashSet<String> activeIDs = new HashSet<>(list); // much faster
            this.activeCount = activeIDs.size();
            for (VehicleWrap v : vehicles) {
                if (activeIDs.contains(v.getID())) {
                    try {
                        v.updateVehicle();
                        v.setExists(true);
                    } catch (Exception e) {
                        v.setExists(false); // if vehicle despawns
                    }
                } else {
                    v.setExists(false);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return {@link ArrayList} of {@link Point2D.Double} of all vehicles positions
     */
    public ArrayList<Point2D.Double> getAllPositions() {
        ArrayList<Point2D.Double> positions = new ArrayList<>();
        for (VehicleWrap v : vehicles) {
            if (v.exists() && v.getPosition() != null) {
                positions.add(v.getPosition());
            }
        }
        return positions;
    }

    /**
     * <b>Outdated method for debugging purposes</b>
     * <p>Prints various data for all vehicles currently on the road network</p>
     */
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

    /**
     * Returns all valuable data of each vehicle, separated by commas for {@link CSV} output
     * @return Array of {@link String}
     */
    public String[] getVehiclesData() {
        String[] vehiclesData = new String[this.count];
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

            vehiclesData[i] = sb.toString();

        }
        return vehiclesData;
    }

    /**
     * @return int - number of vehicles on the road network
     */
    public int getExistingVehCount() {
        int r = 0;
        for (VehicleWrap v : vehicles) {
            if (v.exists()) r++;
        }
        return r;
    }

    public Point2D.Double getMeanPosition() {
        double meanX = 0;
        double meanY = 0;
        for (VehicleWrap v : this.vehicles) {
            if(v.exists()) {
                meanX += v.getPosition().x;
                meanY += v.getPosition().y;
            }
        }
        meanX /= this.activeCount;
        meanY /= this.activeCount;
        return new Point2D.Double(meanX, meanY);
    }

    /**
     * @return int - number of overall vehicles in the simulation (included yet-to-exist and past vehicles)
     */
    public int getCount() {
        return count;
    }

    /**
     * @return {@link VehicleWrap}'s internal {@link CopyOnWriteArrayList}
     */
    public CopyOnWriteArrayList<VehicleWrap> getVehicles() {
        return vehicles;
    }
}
