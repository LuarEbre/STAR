package sumo.sim;

import it.polito.appeal.traci.SumoTraciConnection;

// Class connecting everything and creating WrapperController, maybe methods like getTime(); ?
public class SumoConnection {
    private SumoTraciConnection connection;
    private WrapperController wrapperController;
    private static String configFile;
    private static String rou_configFile;
    private static String net_configFile;

    public SumoConnection() {
        // Select Windows (.exe) or UNIX binary based on static function Util.getOSType()
        String sumoBinary = Util.getOSType().equals("Windows")
                // using sumo-gui for visualisation now, will later be replaced by our own rendered map
                ? "src/main/resources/Binaries/sumo.exe"
                : "src/main/resources/Binaries/sumo";

        // config knows both .rou and .net XMLs
        //String configFile = "src/main/resources/SumoConfig/Map_1/test5.sumocfg";
        configFile = "src/main/resources/SumoConfig/Map_2/test.sumocfg";
        rou_configFile = "src/main/resources/SumoConfig/Map_2/test.rou.xml";
        net_configFile = "src/main/resources/SumoConfig/Map_2/test.net.xml";
        //String configFile = "src/main/resources/SumoConfig/Map_3/test6.sumocfg";

        // create new connection with the binary and map config file
        connection = new SumoTraciConnection(sumoBinary, configFile);
        wrapperController = null;
    }

    public void start() {

        try {
            // add various connection options
            connection.addOption("delay", "50");
            connection.addOption("start", "true");
            connection.addOption("quit-on-end", "true");
            connection.runServer(8813);
            wrapperController = new WrapperController(connection);

            // Connection has been established
            System.out.println("Connected to Sumo.");

            Street_List sl = new Street_List(connection);
            Junction_List jl = new Junction_List(connection, sl);
            jl.update_adjacency();

            RouteWrap route1 = Util.generate_route("J1", "J7", jl);
            route1.printRoute();

        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        } finally {
            // Close the simulation in any case
             //connection.close();
        }
    }

    public static String get_current_config(){
        return configFile;
    }

    public static String get_current_rou_config(){
        return rou_configFile;
    }

    public static String get_current_net_config(){
        return net_configFile;
    }
}