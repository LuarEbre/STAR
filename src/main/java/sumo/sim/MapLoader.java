package sumo.sim;

import java.util.List;

public interface MapLoader {
    List<String> getNames();
    SumoMapConfig getConfig(String name); // returns specific SumoConfig

}
