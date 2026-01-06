package sumo.sim;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SumoMapManager {

    private final Map<String, SumoMapConfig> maps = new HashMap<>(); // hashmap of configs

    public SumoMapManager(){
        loadDefaultMaps();
    }

    /**
     * Loads all Default Maps with hardcoded paths
     */
    private void loadDefaultMaps(){
        maps.put("Frankfurt", new SumoMapConfig(
                "Frankfurt",
                new File("src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.net.xml"),
                new File("src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.rou.xml"),
                new File("src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.sumocfg")
        ));

        maps.put("RugMap", new SumoMapConfig(
                "RugMap",
                new File ("src/main/resources/SumoConfig/RugMap/rugmap.net.xml"),
                new File ("src/main/resources/SumoConfig/RugMap/rugmap.rou.xml"),
                new File ("src/main/resources/SumoConfig/RugMap/rugmap.sumocfg")
        ));
    }

    public List<String> getNames() {
        return new ArrayList<>(maps.keySet()); // retrieves all keys, keys = names
    }

    public SumoMapConfig getConfig(String name) {
        return maps.get(name);
    }
}
