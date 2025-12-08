package sumo.sim;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Lane;
import it.polito.appeal.traci.SumoTraciConnection;

public class Street {
    private double maxSpeed; // same attributes as in .net
    private final SumoTraciConnection con;
    private final String id;
    // List of <Lane> objects
    private String fromJunction;
    private String toJunction;
    XML xml = null;
    private double density;

    public Street(String id, SumoTraciConnection con) {
        this.id = id;
        this.con = con;
        try {
            xml = new XML(WrapperController.get_current_net());
            this.fromJunction = xml.get_from_junction(id);
            this.toJunction = xml.get_to_junction(id);
            updateStreet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }



}