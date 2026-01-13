package sumo.sim.logic;

import java.io.File;

public class SumoMapConfig {
    private final String name;
    private final File netPath;
    private final File rouPath;
    private final File configPath;
    private boolean chosen;

    public SumoMapConfig(String name, File netPath, File rouPath, File configPath) {
        this.name = name;
        this.rouPath = rouPath;
        this.netPath = netPath;
        this.configPath = configPath;
    }

    // getter

    public String  getName() {
        return name;
    }
    public File getRouPath() {
        return rouPath;
    }
    public File getNetPath() {
        return netPath;
    }
    public File getConfigPath() {
        return configPath;
    }

    public boolean isChosen() {
        return chosen;
    }

    // setter

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }
}
