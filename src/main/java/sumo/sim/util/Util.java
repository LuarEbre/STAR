package sumo.sim.util;

import sumo.sim.logic.SumoMapConfig;

import java.util.*;

public class Util {

    private static int n = 1; // duplicate count

    // used for initial selection of binary path
    public static String getOSType() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows")) {
            return "Windows";
        }
        else {
            return "Other";
        }
    }

    // Check for duplicates
    public static String checkDuplicate(Map<String, SumoMapConfig> maps, String newName) {
        if (maps.isEmpty()) return newName;
        String returnString = newName;

        while (maps.containsKey(returnString)) {
            returnString = newName;
            returnString += "(" + n + ")";
            n++;
        }
        System.out.println(returnString);
        return returnString;
    }

    public static String checkRouteDuplicate(Map<String, List<String>> allRoutes, String newName) {
        if (allRoutes.isEmpty()) return newName;
        String returnString = newName;

        while (allRoutes.containsKey(returnString)) {
            returnString = newName;
            returnString += "(" + n + ")";
            n++;
        }
        System.out.println(returnString);
        return returnString;
    }
}