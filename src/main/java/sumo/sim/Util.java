package sumo.sim;

import java.util.*;

public class Util {

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
}