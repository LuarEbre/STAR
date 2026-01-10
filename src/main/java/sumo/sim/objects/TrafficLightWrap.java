package sumo.sim.objects;

import de.tudresden.sumo.cmd.Trafficlight;
import de.tudresden.sumo.objects.SumoLink;
import de.tudresden.sumo.objects.SumoTLSController;
import de.tudresden.sumo.objects.SumoTLSPhase;
import de.tudresden.sumo.objects.SumoTLSProgram;
import it.polito.appeal.traci.SumoTraciConnection;
import sumo.sim.SimulationRenderer;
import sumo.sim.data.XML;
import sumo.sim.logic.WrapperController;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;

/**
 * A wrapper class representing a single Traffic Light, gets created by {@link TrafficLightList}
 * <p>
 * This class communicates with TraaS Trafficlight to control traffic light phases,
 * programs, and states. It also stores information such as position,
 * controlled lanes (links), and incoming streets.
 * </p>
 *
 */
public class TrafficLightWrap {

    private final SumoTraciConnection con;
    private final String id;
    private final Set<Street> controlledStreets;

    private String type; // types: static, actuated, delay based, offline, special, rail signal
    // program :
    // offset : adjusts start time of phase cycle
    // program id :
    // <tlLogic id="J11" type="static" programID="0" offset="0"> ---> can have multiple of this with same TL id diff p id
    //        <phase duration="42" state="GGrrrGGg"/>
    //
    // </tlLogic>

    private List<TrafficLightPhase> phases; // G = green priority , g , y, r , u = red_yellow , o = off;
    //String[] phaseNames = {"NS_Green", "EW_Green", "All_Red"}; <- North x south, east x west
    private int duration; // time
    private final Point2D.Double position; // position as a junction
    String [] stateArray;
    private final List<SumoLink> controlledLinks;
    private final List<String> incomingLanes;
    private XML xml;

    /**
     * Constructor is called by {@link TrafficLightList#TrafficLightList(SumoTraciConnection, StreetList)} constructor
     * <p>
     * Instantiates all attributes based on the data provided from the parsed {@code .net.xml} file
     * </p>
     *
     * @param id   The unique ID of the traffic light, used to call do_job methods.
     * @param Data A map containing attributes parsed from the network XML (e.g., x, y, incLanes).
     * @param con  The active SumoTraciConnection object created in {@link WrapperController}.
     * @throws RuntimeException if there is an error parsing data or communicating with TraCI.
     */
    public TrafficLightWrap(String id, Map<String,String> Data, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
        this.controlledStreets = new HashSet<>();
        this.phases = new ArrayList<>();
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
            throw new RuntimeException(e);
        }
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
                SumoTLSProgram prog = programsMap.values().iterator().next();
                if (this.phases == null) {
                    this.phases = new ArrayList<>(); // if there is already a list
                }
                this.phases.clear(); // empty list

                int index = 0;
                for (SumoTLSPhase p : prog.phases) {
                    String rawString = p.toString(); // phase : "Grryrr#3#3" etc.
                    String cleanState = rawString.split("#")[0]; // cutting of everything after #
                    this.phases.add(new TrafficLightPhase(index, cleanState, p.duration));
                    index++;
                }

            }

        } catch (Exception e) {
            // needs catching
        }
    }

    /**
     * Should automatically adjust Traffic Light configurations based on Vehicle density and waiting time.
     */
    public void enableAdaptiveTrafficLightLogic() {
        // based on numbers of vehicles and waiting time -> adjust tl timings
    }

    // setter

    /**
     * Constructs an array in this format:
     *
     * <p>
     *     Array: [index0, lane controlled by index0...]
     *     State e.g. of "Grr" state index 0 is "G" and its controlled lane {@link LaneWrap} is stored after wards by its id.
     * </p>
     *
     *<p>
     *     This is to ensure {@link SimulationRenderer} renders Traffic lights correctly.
     *</p>
     */
    public void setCurrentState() {
        int currentPhaseIndex = getPhaseNumber(); // which state the tl is in -> applies to all controlled tl
        // -> state differs from index to index (index is controlled lanes that have tl)
        String currentState;
        // links.get(0).from
        try {
            currentState = (String) con.do_job_get(Trafficlight.getRedYellowGreenState(this.id));
        } catch (Exception e) {
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


    /**
     * Sets the active phase of the traffic light to the specified index.
     *
     * @param index The index of the phase to switch to.
     * @throws RuntimeException if the TraCI command fails.
     */
    public void setPhaseNumber(int index) {
        try {
            con.do_job_set(Trafficlight.setPhase(id,index));
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            return;
        }
    }


    public void setProgram(String programID) {
        try {
            con.do_job_set(Trafficlight.setProgram(id, programID));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setRedYellowGreenState(String state) {
        try {
            con.do_job_set(Trafficlight.setRedYellowGreenState(id, state));
        } catch (Exception e) {
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
            throw new RuntimeException(e);
        }
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
     * Updates TL phase
     */
    public void updateTL() {
        try {
            //this.phase = (int) con.do_job_get(Trafficlight.getPhase(this.id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}