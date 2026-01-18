package sumo.sim.logic;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// XML parser

import sumo.sim.data.XML;
import sumo.sim.util.MapLoadingException;
import sumo.sim.util.Util;

// Logger
import java.util.logging.Level;
import java.util.logging.Logger;

public class SumoMapManager {

    private final Map<String, SumoMapConfig> maps = new HashMap<>(); // hashmap of configs

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(SumoMapManager.class.getName());

    public SumoMapManager(){
        loadDefaultMaps();
    }

    /**
     * Loads all Default Maps with hardcoded paths
     */
    private void loadDefaultMaps(){
        maps.put("Frankfurt1", new SumoMapConfig(
                "Frankfurt1",
                new File("src/main/resources/SumoConfig/Frankfurt1/frankfurt1_fixed.net.xml"),
                new File("src/main/resources/SumoConfig/Frankfurt1/frankfurt1_fixed.rou.xml"),
                new File("src/main/resources/SumoConfig/Frankfurt1/frankfurt1_fixed.sumocfg")
        ));

        maps.put("Frankfurt2", new SumoMapConfig(
                "Frankfurt2",
                new File ("src/main/resources/SumoConfig/Frankfurt2/frankfurt2.net.xml"),
                new File ("src/main/resources/SumoConfig/Frankfurt2/frankfurt2.rou.xml"),
                new File ("src/main/resources/SumoConfig/Frankfurt2/frankfurt2.sumocfg")
        ));

    }

    public void chooseFile(Stage stage) throws MapLoadingException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Sumo Config");
        // limits choosable files
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Sumo Config files", "*.sumocfg"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))); // start directory

        // opens os-specific file manager
        File file  = fileChooser.showOpenDialog(stage);
        checkFile(file);
    }

    private void checkFile(File file) throws MapLoadingException {
        if (file==null || !file.exists()) {
            throw new MapLoadingException("Could not find map file");
        }

        // Scanning for .rou / .net in Config
        XML xml = null;
        try {
            xml = new XML(file.toString());
        } catch (Exception e) {
            // fail to create XML reader
            logger.log(Level.WARNING, "Failed to create XML reader", e);
            return;
        }

        Map<String, String> inputs = xml.getConfigInputs(); // all inputs in sumoconfig
        // filter only get net and route files (problem if multiple?)
        String netFileString = inputs.get("net-file"); // file of sumoconfig
        String rouFileString = inputs.get("route-files");

        if (netFileString !=null && rouFileString !=null) {

            File netFile = new File(file.getParent(), netFileString);
            File rouFile = new File(file.getParent(), rouFileString);

            if (netFile.exists() && rouFile.exists()) {

                String mapName = file.getName().replace(".sumocfg", ""); // name from sumo config
                mapName = Util.checkDuplicate(maps, mapName); // check and changes Name if there is a duplicate
                SumoMapConfig newConfig = new SumoMapConfig(mapName, netFile, rouFile, file);
                maps.put(mapName, newConfig); // put in list
                logger.log(Level.INFO, "New Sumo Config: " + maps.get(mapName));
            } else {
                throw new MapLoadingException(" Net, Rou files not found.");
            }
        } else {
            throw new MapLoadingException("Sumo files not complete.");
        }
    }

    public List<String> getNames() {
        return new ArrayList<>(maps.keySet()); // retrieves all keys, keys = names
    }

    public SumoMapConfig getConfig(String name) {
        return maps.get(name);
    }

    public List<String> getAllImportedMaps() {
        List<String> result = new ArrayList<>();
        // filtering standard maps
        for (String key : maps.keySet()) {
            if (!key.equals("Frankfurt1") && !key.equals("Frankfurt2")) {
                result.add(key);
            }
        }
        return result;
    }
}
