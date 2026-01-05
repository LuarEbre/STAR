package sumo.sim;

public class SumoMapConfig {
    private final String name;
    private final String netPath;
    private final String rouPath;
    private final String configPath;
    private boolean chosen;

    public SumoMapConfig(String name, String netPath, String rouPath, String configPath) {
        this.name = name;
        this.rouPath = rouPath;
        this.netPath = netPath;
        this.configPath = configPath;
    }

    // getter

    public String  getName() {
        return name;
    }
    public String getRouPath() {
        return rouPath;
    }
    public String getNetPath() {
        return netPath;
    }
    public String getConfigPath() {
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
