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
            throw new RuntimeException(e);
        }

        Map<String, String> inputs = xml.getConfigInputs(); // all inputs in sumoconfig
        // filter only get net and route files (problem if multiple?)
        String netFileString = inputs.get("net-file");
        String rouFileString = inputs.get("route-files");

        if (netFileString !=null && rouFileString !=null) {

            File netFile = new File(file.getParent(), netFileString);
            File rouFile = new File(file.getParent(), rouFileString);

            if (netFile.exists() && rouFile.exists()) {

                String mapName = file.getName().replace(".sumocfg", ""); // name from sumo config
                mapName = Util.checkDuplicate(maps, mapName); // check and changes Name if there is a duplicate
                SumoMapConfig newConfig = new SumoMapConfig(mapName, netFile, rouFile, file);
                maps.put(mapName, newConfig); // put in list
                System.out.println(maps.get(mapName));
                System.out.println(netFile);
            }
        }

        // should have output error messages
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
            if (!key.equals("Frankfurt") && !key.equals("RugMap")) {
                result.add(key);
            }
        }
        return result;
    }
}
