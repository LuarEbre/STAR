package sumo.sim.objects;

import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.objects.SumoLink;
import de.tudresden.sumo.objects.SumoTLSController;
import de.tudresden.sumo.objects.SumoTLSPhase;
import de.tudresden.sumo.objects.SumoTLSProgram;
import it.polito.appeal.traci.SumoTraciConnection;
import sumo.sim.gui.SimulationRenderer;
import sumo.sim.data.XML;
import sumo.sim.logic.WrapperController;
import sumo.sim.util.ExportableData;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper class representing a single Traffic Light, gets created by {@link TrafficLightList}
 * <p>
 * This class communicates with TraaS Trafficlight to control traffic light phases,
 * programs, and states. It also stores information such as position,
 * controlled lanes (links), and incoming streets.
 * </p>
 *
 */
public class TrafficLight extends SelectableObject implements ExportableData {

    private final SumoTraciConnection con;
    private final String id;
    private final Set<Street> controlledStreets;
    private List<TrafficLightPhase> phases; // G = green priority , g , y, r , u = red_yellow , o = off;
    private final List<String> changeTlHistory = new ArrayList<>();

    // Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(TrafficLight.class.getName());

    // rendering
    private int duration; // time
    private final Point2D.Double position; // position as a junction
    private String [] stateArray;
    private final List<SumoLink> controlledLinks;
    private final List<String> incomingLanes;
    private XML xml;
    private final WrapperController controller;

    /**
     * Constructor is called by {@link TrafficLightList#TrafficLightList(SumoTraciConnection, StreetList, WrapperController)} constructor
     * <p>
     * Instantiates all attributes based on the data provided from the parsed {@code .net.xml} file
     * </p>
     *
     * @param id   The unique ID of the traffic light, used to call do_job methods.
     * @param Data A map containing attributes parsed from the network XML (e.g., x, y, incLanes).
     * @param con  The active SumoTraciConnection object created in {@link WrapperController}.
     * @throws RuntimeException if there is an error parsing data or communicating with TraCI.
     */
    public TrafficLight(String id, Map<String,String> Data, SumoTraciConnection con, WrapperController controller, StreetList streetList) {
        super();
        this.id = id;
        this.con = con;
        this.controlledStreets = new HashSet<>();
        this.phases = new ArrayList<>();
        this.controller = controller;
        try {
            xml = new XML(WrapperController.getCurrentNet());
            this.position = new Point2D.Double();
            this.position.x = Double.parseDouble(Data.get("x"));
            this.position.y = Double.parseDouble(Data.get("y"));
            String incLanesString = Data.get("incLanes");
            this.incomingLanes = Arrays.asList(incLanesString.split("\\s+"));

            this.controlledLinks = (List<SumoLink>) con.do_job_get(Trafficlight.getControlledLinks(id));
            //updateTL();
            //getCurrentState();
            loadPhases();

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse XML", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * Returns the export category for traffic light analysis.
     * @return A string identifying this data as Traffic Light Adaption Analysis.
     */
    @Override
    public String getExportCategory() {
        return "Traffic Light Adaption Analysis";
    }
    /**
     * Defines the headers for the traffic light export table.
     * @return An array of strings containing the column titles.
     */
    @Override
    public String[] getColumnHeaders() {
        return new String[] {
                "ID",
                "State Duration",
                "Street: From/to",
                "Avg/Peak Density",
                "Interventions"
        };
    }
    /**
     * Compiles the current state, controlled street statistics, and intervention history
     * into a single row for data export.
     * <p>
     * This method aggregates data from all streets controlled by this traffic light,
     * calculates the collective average density, and formats the change history log.
     * @return A string array representing one row of traffic light data.
     */
    @Override
    public String[] getRowData() {
        TrafficLightPhase currentPhase = getCurrentPhaseObject();

        String stateInfo = (currentPhase != null ? currentPhase.getState() : "unknown")
                + " (" + getDuration() + "s)";
        StringBuilder streetDetails = new StringBuilder();
        double sumOfAverages = 0;
        double peakDensity = 0;
        int count = 0;

        for (Street s : controlledStreets) {
            // infor about streets from/to
            streetDetails.append(s.getId())
                    .append(" (").append(s.getFromJunction()).append("to").append(s.getToJunction()).append(")")
                    .append("\n"); // line breaks for overview

            // statistics
            sumOfAverages = sumOfAverages + s.getAverageDensity();
            if (s.getMaxDensity() > peakDensity) {
                peakDensity = s.getMaxDensity();
            }
            count++;
        }
        double totalAvg = 0;
        if (count > 0) {
            totalAvg = sumOfAverages / count;
        }

        // sumup change history
        String historyLog = String.join(" | ", changeTlHistory);
        if (historyLog.isEmpty()) historyLog = "No changes";

        return new String[]{
                this.id,
                stateInfo,
                streetDetails.toString().trim(),
                String.format(java.util.Locale.US, "Avg: %.2f / Peak: %.2f", totalAvg, peakDensity),
                historyLog
        };
    }
    /**
     * Loads all Traffic Light phases this. TL
     *
     * <p>
     *     Creates {@link TrafficLightPhase} objects containing phase index, state, duration of each Phase.
     * </p>
     */
    private void loadPhases() {
        try {

            SumoTLSController controller = (SumoTLSController) con.do_job_get(Trafficlight.getCompleteRedYellowGreenDefinition(this.id));

            Map<String, SumoTLSProgram> programsMap = controller.programs; // get controller program of SumoTLSController

            if (programsMap != null && !programsMap.isEmpty()) {
                // check if existent
                SumoTLSProgram prog = programsMap.values().iterator().next(); // gets next available program
                if (this.phases == null) {
                    this.phases = new ArrayList<>(); // if there is already a list
                }
                this.phases.clear(); // empty list

                int index = 0;
                // iterate sumo tl phase
                for (SumoTLSPhase p : prog.phases) {
                    String rawString = p.toString(); // phase : "Grryrr#3#3" etc.
                    String cleanState = rawString.split("#")[0]; // cutting of everything after #
                    this.phases.add(new TrafficLightPhase(index, cleanState, p.duration));
                    index++;
                }

            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load TL phases", e);
            throw new RuntimeException(e);
        }
    }

    // setter

    /**
     * Constructs an array in this format:
     *
     * <p>
     *     Array: [index0, lane controlled by index0...]
     *     State e.g. of "Grr" state index 0 is "G" and its controlled lane {@link Lane} is stored after wards by its id.
     * </p>
     *
     *<p>
     *     This is to ensure {@link SimulationRenderer} renders Traffic lights correctly.
     *</p>
     */
    public void setCurrentState() {
        // -> state differs from index to index (index is controlled lanes that have tl)
        String currentState;
        // links.get(0).from
        try {
            currentState = (String) con.do_job_get(Trafficlight.getRedYellowGreenState(this.id));
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to set Current State of Traffic Light", e);
            throw new RuntimeException(e);
        }
        stateArray = new String[currentState.length()*2]; // saves state in arr -> to get indices
        for (int i = 0; i < stateArray.length; i+=2 ) {
            int sumoIndex = i/2; // to not skip values
            stateArray[i] = currentState.charAt(sumoIndex) + ""; // every current state e.g = Grrryy (length definded)
            stateArray[i+1] = controlledLinks.get(sumoIndex).from; // index i -> i+1 = lane
            //System.out.println("Index " + (i) + stateArray[i] + " controls"  + stateArray[i+1]); // -> phase duration defined
            // [G, lane_G ,y , lane_y , r, lane_r ] format
        }
    }

    // setter

    /**
     * Adaptive States based on Density of Controlled Lanes
     * Replaces setCurrentState if adaptive is checked
     */
    public void adaptiveStateUpdate() {

        if (phases == null || phases.isEmpty()) return;

        int currentPhase = getPhaseNumber();

        TrafficLightPhase bestPhase = null;
        double bestScore = -1;

        for (TrafficLightPhase phase : phases) {

            double score = 0;
            List<Integer> greenLanesIndex = phase.getGreenLanes();
            List<String> greenLanesID = new ArrayList<>();

            for(Integer greenLaneIndex : greenLanesIndex){
                greenLanesID.add(controlledLinks.get(greenLaneIndex).from);
            }

            for (String laneID : greenLanesID) {

                try {
                    double veh = (int) con.do_job_get(de.tudresden.sumo.cmd.Lane.getLastStepVehicleNumber(laneID));
                    double len = (double) con.do_job_get(de.tudresden.sumo.cmd.Lane.getLength(laneID));

                    double maxVeh = len / 7.5;
                    double density = veh / Math.max(1.0, maxVeh);

                    score += density;

                } catch (Exception e) {
                    logger.fine("Cannot read lane " + laneID);
                }
            }

            if (bestPhase == null || score > bestScore) {
                bestPhase = phase;
                bestScore = score;
            }
        }

        if (bestPhase == null) return;

        if (bestPhase.getIndex() == currentPhase) {
            setPhaseDuration(getNextSwitch() + 5);
        } else {
            setPhaseNumber(bestPhase.getIndex());
        }
    }

    /**
     * Sets the active phase of the traffic light to the specified index.
     *
     * @param index The index of the phase to switch to.
     * @throws RuntimeException if the TraCI command fails.
     */
    public void setPhaseNumber(int index) {
        try {
            con.do_job_set(Trafficlight.setPhase(id,index));
            triggerReset("Changed phase to " + index);
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to set phase number of Traffic Light", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Assigns a custom name to the current phase of the traffic light (not used)
     *
     * @param tlPhaseName name given
     * @throws RuntimeException if the TraCI command fails.
     */
    public void setPhaseName(String tlPhaseName) {
        try {
            con.do_job_set(Trafficlight.setPhaseName(id, tlPhaseName));
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to set phase name of Traffic Light", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the remaining duration for the current phase (overwrites current remaining duration)
     *
     * @param phaseDuration The duration in seconds.
     * @throws RuntimeException if the TraCI command fails.
     */
    public void setPhaseDuration(double phaseDuration) {
        //getPhaseNumber(); // -> only applies to phase currently active -> should display phase in gui for reference?
        try {
            con.do_job_set(Trafficlight.setPhaseDuration(id, phaseDuration));
            triggerReset("Set duration temporaly to " + phaseDuration + " s");
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to set phase duration of Traffic Light", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Forces a permanent duration change for any TrafficLight phase.
     *
     * <p>
     *     By retrieving the program from {@link SumoTLSProgram} and selecting a specific phase
     *     from {@link SumoTLSPhase} this method adjust the duration value stored inside .net XML
     *     to a new value, until the program is terminated.
     * </p>
     *
     * @param phaseIndex to select the Phase index of the current Traffic Light
     * @param newDuration value to change the duration with.
     */
    public void setPhaseDurationPermanently(int phaseIndex, double newDuration) {
        // program id check how many T-logic -> else always 0 // force logic 0 else need ProgramID
        try {
            SumoTLSController controller = (SumoTLSController) con.do_job_get(Trafficlight.getCompleteRedYellowGreenDefinition(id));
            SumoTLSProgram program = controller.programs.get("0"); // specific hashmap index (state)
            if (program == null && !controller.programs.isEmpty()) {
                program = controller.programs.values().iterator().next(); // take the next if null
            }
            if (program != null) {
                SumoTLSPhase phase = program.phases.get(phaseIndex); // gets specified phase
                phase.duration = newDuration; // overwrites new phase
                con.do_job_set(Trafficlight.setCompleteRedYellowGreenDefinition(id, program));
                phases.get(phaseIndex).setDuration(newDuration);
            }
            triggerReset("Set duration at phase index " + phaseIndex + " permanently to " + newDuration);
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to set phase duration of Traffic Light", e);
            return;
        }
    }


    public void setProgram(String programID) {
        try {
            con.do_job_set(Trafficlight.setProgram(id, programID));
            triggerReset("Progamm ID changed to " + programID);
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to set program of Traffic Light", e);
            throw new RuntimeException(e);
        }
    }

    public void setRedYellowGreenState(String state) {
        try {
            con.do_job_set(Trafficlight.setRedYellowGreenState(id, state));
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to set red yellow green state of Traffic Light", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a {@link Street} object to the set of streets controlled by this traffic light.
     *
     * @param s The Street object to add.
     */
    public void setControlledStreets(Street s) {
        this.controlledStreets.add(s);
        //printControlledStreets();
    }

    // getter

    public int getPhaseNumber() {
        int ret = 0;
        try {
            ret = (int) con.do_job_get(Trafficlight.getPhase(id)); // gets phase of tl = 1, 2, 3
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to get phase number of Traffic Light", e);
            throw new RuntimeException(e);
        }
        return ret;
    }

    public int getProgramNumber() {
        try {
            SumoTLSController controller = (SumoTLSController) con.do_job_get(Trafficlight.getCompleteRedYellowGreenDefinition(id));
            return controller.programs.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getPhaseName() {
        try {
            return (String) con.do_job_get(Trafficlight.getPhaseName(id));
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to get phase name of Traffic Light", e);
            throw new RuntimeException(e);
        }
    }

    public String getPhaseAtIndex(int index) {
        SumoTLSController controller = null;
        try {
            controller = (SumoTLSController) con.do_job_get(Trafficlight.getCompleteRedYellowGreenDefinition(id));
            SumoTLSProgram program = controller.programs.get("0"); // specific hashmap index (state)
            if (program == null && !controller.programs.isEmpty()) {
                program = controller.programs.values().iterator().next(); // take the next if null
            }
            if (program != null) {
                return program.phases.get(index).phasedef; // gets specified phase "Grrr"
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }


    public double getDuration() {
        double duration = 0;
        try {
            duration =  (double) con.do_job_get(Trafficlight.getPhaseDuration(id)); // gets phase of tl = 1, 2, 3
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to get phase duration of Traffic Light", e);
            throw new RuntimeException(e);
        }
        return duration;
    }

    // returns time remaining until tl switches states
    public double getNextSwitch() {
        double duration = 0;
        try {
            duration =  (double) con.do_job_get(Trafficlight.getNextSwitch(id)); // gets phase of tl = 1, 2, 3
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to get next switch of Traffic Light", e);
            throw new RuntimeException(e);
        }
        return duration;
    }

    public List<TrafficLightPhase> getTrafficLightPhases(){
        return phases;
    }

    public void getControlledLanes() {
        // con.do_job_get(Trafficlight.getControlledLanes(id));
    }

    public String getProgram() {
        try {
            return (String) con.do_job_get(Trafficlight.getProgram(id));
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to get program of Traffic Light", e);
            throw new RuntimeException(e);
        }
    }
    public TrafficLightPhase getCurrentPhaseObject() {
        try {
            int currentIndex = getPhaseNumber();

            if (phases != null && currentIndex >= 0 && currentIndex < phases.size()) {
                return phases.get(currentIndex);
            }
        } catch (Exception e) {
            logger.log(Level.FINE, "Error: Phase not available", e);
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public Point2D.Double getPosition() {
        return position;
    }
    public Set<Street> getControlledStreets() {
        return controlledStreets;
    }

    public String[] getCurrentState() {
        return stateArray;
    }

    public String getCurrentStateString() {
        String state ="";
        try {
            state = (String) con.do_job_get(Trafficlight.getRedYellowGreenState(id));
        } catch (Exception e) {
            logger.log(Level.FINE, "Failed to get current state of Traffic Light", e);
            return state;
        }
        return state;
    }


    // other

    /**
     * Prints controlledStreets for debugging
     */
    public void printControlledStreets() {
        for (Street s : controlledStreets) {
            System.out.println(this.id + " controls " + s.getId());
        }
    }

    /**
     * Resets the tracking data (average, peak, measurement) for all streets
     * controlled by this traffic light.
     */
    public void triggerReset(String change) {
        System.out.println("Debug message " + this.id + " : " + change);

        if (controlledStreets != null) {
            for (Street s : controlledStreets) {
                s.resetDataTracking();
            }
            this.changeTlHistory.clear();
            this.changeTlHistory.add(change);
            logger.log(Level.INFO, "Reset controlled streets due to manual traffic light setting change.", this.id);
        }
    }
}