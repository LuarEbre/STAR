package sumo.sim.objects;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.scene.paint.Color;
import sumo.sim.data.CSV;
import sumo.sim.logic.Type;
import sumo.sim.util.GenericList;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a {@link CopyOnWriteArrayList} of {@link VehicleWrap} objects.
 * <p>
 * This makes the class thread-safe (immune to race conditions), allowing the simulation
 * to iterate over vehicles while another thread tries to add or remove vehicles.
 * </p>
 */
public class VehicleList {
    private CopyOnWriteArrayList<VehicleWrap> vehicles = new CopyOnWriteArrayList<>(); // List of Vehicles
    private final SumoTraciConnection con;// main connection created in main wrapper
    private int count; // vehicles in list, latest car number: "v"+ count
    private int activeCount; // vehicles currently on the road network

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(VehicleList.class.getName());

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
    public void addVehicle(int n, String type, String route, Color color) {
        ArrayList<VehicleWrap> newVehicles = new ArrayList<>(n);
        try {
            for (int i=0; i<n; i++) {
                con.do_job_set(Vehicle.addFull("v" + count, route, type, // ids -> latest car id
                        "now", "0", "0", "0",
                        "current", "max", "current", "",
                        "", "", 0, 0)
                );
                newVehicles.add(new VehicleWrap("v"+count, con, type, route, color));
                count++; // increment counter to prevent identical car ids
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to add vehicle", e);
            throw new RuntimeException(e);
        }
        vehicles.addAll(newVehicles);
    }

    /**
     * <p></p>Calls {@link VehicleWrap#setExists(boolean)} for each vehicle based on whether they are on the road network or not</p>
     * <p>Calls {@link VehicleWrap#updateVehicle()} for every vehicle currently on the road network</p>
     */
    public void updateAllVehicles() {

        // get active vehicle's IDs from SUMO
        HashSet<String> activeIDs = this.getIDListAsHashSet();

        this.activeCount = activeIDs.size();

        for (VehicleWrap v : vehicles) {
            boolean isActive = activeIDs.contains(v.getID());
            if (isActive) {
                try {

                    v.updateVehicle();
                    // if vehicle is present in activeIDs it is no longer queued and assuredly is on the road network
                    if (v.isQueued()) v.setQueued(false);

                } catch (Exception e) {

                    String errorMessage = "Failed to update vehicle (" + v.getID() + ")";
                    logger.log(Level.FINE, errorMessage, e);
                    isActive = false;

                }

            }
            v.setExists(isActive);
        }
    }

    /**
     * Determines active vehicle count in case {@link VehicleList#updateAllVehicles()} fails or isn't called
     */
    public void determineActiveVehicleCount() {

        // get active vehicle's IDs from SUMO
        HashSet<String> activeIDs = this.getIDListAsHashSet();

        this.activeCount = activeIDs.size();

        for (VehicleWrap v : vehicles) {
            v.setExists(activeIDs.contains(v.getID()));
        }
    }

    /**
     * Used in Select Mode to deselect all other Vehicles once a Vehicle has been found
     */
    public void deselectAll() {
        for(VehicleWrap v : vehicles) {
            v.deselect();
        }
    }

    public void setVehicles(CopyOnWriteArrayList<VehicleWrap> vehicles) { this.vehicles = vehicles; }

    // getter

    /**
     * @return number of vehicles on the road network
     */
    public int getExistingVehCount() {
        int r = 0;
        for (VehicleWrap v : vehicles) {
            if (v.exists()) r++;
        }
        return r;
    }

    /**
     * @return HashSet of all active vehicle's IDs
     */
    private HashSet<String> getIDListAsHashSet() {
        try {
            SumoStringList sumoIDList = (SumoStringList) con.do_job_get(Vehicle.getIDList());
            HashSet<String> activeIDs = new HashSet<>(sumoIDList);
            return activeIDs;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get IDList", e);
            return null;
        }
    }

    /**
     * Used to render the density anchor, which shows the mean position of all current active vehicles
     * @return {@link Point2D.Double} mean position of all current active vehicles
     */
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

    public int getQueuedCount() {
        int count = 0;
        for(VehicleWrap v : vehicles) {
            if(v.isQueued()) count++;
        }
        return count;
    }

    public int getStoppedCount() {
        int count = 0;
        for(VehicleWrap v : vehicles) {
            if(v.exists() && v.isCurrentlyStopped()) count++;
        }
        return count;
    }

    public int getStoppedTime() {
        int seconds = 0;
        for(VehicleWrap v : vehicles) {
            if(v.exists()) seconds+= v.getWaitingTime();
        }
        return seconds;
    }

    /**
     * Calculates the current mean speed of all active vehicles in meters per second
     * @return mean speed of all active vehicles in m/s. Returns 0.0 if no vehicles are active.
     */
    public double getMeanSpeed() {
        double meanspeed = 0;
        if(this.activeCount == 0) return meanspeed;
        for(VehicleWrap v : vehicles) {
            if(v.exists()) meanspeed += v.getSpeed();
        }
        meanspeed /= this.activeCount;
        return meanspeed;
    }

    /**
     * Calculates the current standard deviation of speed within the current vehicles
     * @return
     */
    public double getSpeedStdDev() {
        // return 0.0 to avoid division by 0 down the line
        if(this.activeCount == 0) return 0.0;
        double meanspeed = this.getMeanSpeed();
        double sumofsquares = 0;
        for(VehicleWrap v : vehicles) {
            if(v.exists()) {
                double diff = v.getSpeed() - meanspeed;
                sumofsquares += diff*diff;
            }
        }
        return Math.sqrt(sumofsquares/this.activeCount);
    }

    public int getCount() {
        return count;
    }
    public CopyOnWriteArrayList<VehicleWrap> getVehicles() {
        return vehicles;
    }
    public int getActiveCount() { return activeCount; }

    public double getSpeedStdDevFiltered() {
        int activeVehicles = getExistingVehCount();
        if(activeVehicles == 0) return 0.0;
        double meanspeed = this.getMeanSpeed();
        double sumofsquares = 0;
        for(VehicleWrap v : vehicles) {
            if(v.exists()) {
                double diff = v.getSpeed() - meanspeed;
                sumofsquares += diff*diff;
            }
        }
        return Math.sqrt(sumofsquares/activeVehicles);
    }

    public int getVehiclesAmountByType(Type type) {
        List<VehicleWrap> vehiclesWithType = new ArrayList<>();
        for(VehicleWrap v : vehicles) {
            if(v.getType().equals(type.getId())) {
                vehiclesWithType.add(v);
            }
        }
        return vehiclesWithType.size();
    }

    public void deselectAll() {
        for(VehicleWrap v : vehicles) {
            v.deselect();
        }
    }
}
