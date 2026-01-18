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
    private void loadDefaultMaps() {
        try {
            String jarPath = new File(SumoMapManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();

            File frankfurtDir = new File(jarPath, "SumoConfig/Frankfurt1");
            maps.put("Frankfurt1", new SumoMapConfig(
                    "Frankfurt1",
                    new File(frankfurtDir, "frankfurt1_fixed.net.xml"),
                    new File(frankfurtDir, "frankfurt1_fixed.rou.xml"),
                    new File(frankfurtDir, "frankfurt1_fixed.sumocfg")
            ));

            File frankfurt2Dir = new File(jarPath, "SumoConfig/Frankfurt2");
            maps.put("Frankfurt2", new SumoMapConfig(
                    "Frankfurt2",
                    new File(frankfurt2Dir, "frankfurt2.net.xml"),
                    new File(frankfurt2Dir, "frankfurt2.rou.xml"),
                    new File(frankfurt2Dir, "frankfurt2.sumocfg")
            ));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Konnte Basis-Pfad für Maps nicht bestimmen", e);
        }
    }

    public void chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Sumo Config");
        // limits choosable files
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Sumo Config files", "*.sumocfg"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))); // start directory

        // opens os-specific file manager
        File file  = fileChooser.showOpenDialog(stage);
        checkFile(file);
    }

    private void checkFile(File file) {
        if (file==null || !file.exists()) {
            // file does not exist
            return;
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
            }
        } else {
            logger.log(Level.WARNING, "Failed to load Sumo Config file");
            return;
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
