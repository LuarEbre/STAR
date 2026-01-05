package sumo.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SumoMapManager implements MapLoader{

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
                "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.net.xml",
                "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.rou.xml",
                "src/main/resources/SumoConfig/Frankfurt_Map/frankfurt.sumocfg"
        ));

        maps.put("TestMap", new SumoMapConfig(
                "TestMap",
                "src/main/resources/SumoConfig/Map_2/test.net.xml",
                "src/main/resources/SumoConfig/Map_2/test.rou.xml",
                "src/main/resources/SumoConfig/Map_2/test.sumocfg"
        ));
    }

    @Override
    public List<String> getNames() {
        return new ArrayList<>(maps.keySet()); // retrieves all keys, keys = names
    }

    @Override
    public SumoMapConfig getConfig(String name) {
        return maps.get(name);
    }
}
