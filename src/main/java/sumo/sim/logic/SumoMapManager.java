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
    private final File mapFolder;

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(SumoMapManager.class.getName());

    public SumoMapManager(){
        this.mapFolder = initializeMaps();
    }


    /**
     * Checks file paths.
     * Directory is saved in mapFolder.
     */
    private File initializeMaps() {
        String userDir = System.getProperty("user.dir");
        File releaseFolder = new File(userDir + File.separator + "SumoConfig");
        File devFolder = new File("src/main/resources/SumoConfig");

        if (releaseFolder.exists() && releaseFolder.isDirectory()) {
            logger.log(Level.INFO, "Release mode: " + releaseFolder.getAbsolutePath());
            loadSpecificMap(releaseFolder, "Frankfurt1", "Frankfurt1", "frankfurt1_fixed.sumocfg");
            loadSpecificMap(releaseFolder, "Frankfurt2", "Frankfurt2", "frankfurt2.sumocfg");

            return releaseFolder;
        } else {
            logger.log(Level.INFO, "Dev mode " + devFolder.getAbsolutePath());
            loadDefaultMapsDev();
            return devFolder;
        }
    }

    /**
     * Loads all Default Maps with hardcoded paths
     */
    private void loadDefaultMapsDev() {
        try {
            maps.put("Frankfurt1", new SumoMapConfig(
                    "Frankfurt1",
                    new File("src/main/resources/SumoConfig/Frankfurt1/frankfurt1_fixed.net.xml"),
                    new File("src/main/resources/SumoConfig/Frankfurt1/frankfurt1_fixed.rou.xml"),
                    new File("src/main/resources/SumoConfig/Frankfurt1/frankfurt1_fixed.sumocfg")
            ));

            maps.put("Frankfurt2", new SumoMapConfig(
                    "Frankfurt2",
                    new File("src/main/resources/SumoConfig/Frankfurt2/frankfurt2.net.xml"),
                    new File("src/main/resources/SumoConfig/Frankfurt2/frankfurt2.rou.xml"),
                    new File("src/main/resources/SumoConfig/Frankfurt2/frankfurt2.sumocfg")
            ));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading default maps", e);
        }
    }


    private void loadSpecificMap(File baseFolder, String mapKey, String subFolder, String fileName) {
        if (!baseFolder.exists()) return;

        File mapFile = new File(baseFolder, subFolder + File.separator + fileName);

        try {
            checkFileCustomName(mapFile, mapKey);
        } catch (MapLoadingException e) {
            logger.log(Level.WARNING, "Could not load default map: " + mapKey + " (" + mapFile.getAbsolutePath() + ")", e);
        }
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

    /**
     * For Release only
     * @param file
     * @param customName
     * @throws MapLoadingException
     */
    public void checkFileCustomName(File file, String customName) throws MapLoadingException {
        if (file == null || !file.exists()) {
            throw new MapLoadingException("Could not find Files: " + (file != null ? file.getAbsolutePath() : "null"));
        }

        XML xml = new XML(file.getAbsolutePath());
        Map<String, String> inputs = xml.getConfigInputs();

        String netFileString = inputs.get("net-file");
        String rouFileString = inputs.get("route-files");

        if (netFileString != null && rouFileString != null) {
            File netFile = new File(file.getParent(), netFileString);
            File rouFile = new File(file.getParent(), rouFileString);

            // if inputs do not exist
            if (!netFile.exists()) throw new MapLoadingException("Net-File misses: " + netFile.getAbsolutePath());
            if (!rouFile.exists()) throw new MapLoadingException("Rou-File misses: " + rouFile.getAbsolutePath());

            SumoMapConfig newConfig = new SumoMapConfig(customName, netFile, rouFile, file);
            maps.put(customName, newConfig);

            logger.log(Level.INFO, "Map loaded: " + customName);
        } else {
            throw new MapLoadingException("Incomplete Config: " + file.getName());
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
