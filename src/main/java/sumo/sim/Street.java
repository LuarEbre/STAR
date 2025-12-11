package sumo.sim;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Lane;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.List;

public class Street {
    private double maxSpeed; // same attributes as in .net
    private final SumoTraciConnection con;
    private final String id;
    // List of <Lane> objects
    private final ArrayList<LaneWrap> lanes = new ArrayList<>();
    private String fromJunction;
    private String toJunction;
    private double density;
    private double noise;

    private XML xml;

    public Street(String id, List<String> data, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
        try {
            updateStreet();

            if(data.contains("from")) {
                this.fromJunction = data.get(data.indexOf("from"));
            }else if(data.contains("to")) {
                this.toJunction = data.get(data.indexOf("to"));
            }else {
                this.fromJunction = null;
                this.toJunction = null;
            }

            int laneCount = (Integer) con.do_job_get(Edge.getLaneNumber(id));
            for (int i = 0; i < laneCount; i++) {
                String currentLaneID = id + "_" + i; // street id_lane
                lanes.add(new LaneWrap(currentLaneID, con, id));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Street(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
    }

    public ArrayList<LaneWrap> getLanes() {
        return lanes;
    }

    public String getId() {
        return id;
    }

    public String getFromJunction() {
        return fromJunction;
    }

    public Street getStreet() { return this; }

    public String getToJunction() {
        return toJunction;
    }

    public void setDensity(double den) {
        this.density = den;
    }

    public double getDensity() {
        return density;
    }

    public void calcDensity(){
        try{
            Number num = (Number) con.do_job_get(Edge.getLastStepVehicleNumber(id));
            Number length = (Number) con.do_job_get(Lane.getLength(id+"_0"));

            double num_val = num.doubleValue();
            double length_val = length.doubleValue();

            this.density = num_val/length_val/1000;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void updateStreet(){
        try {
            calcDensity();
            this.noise = (double)con.do_job_get(Edge.getNoiseEmission(id));

        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }



}
