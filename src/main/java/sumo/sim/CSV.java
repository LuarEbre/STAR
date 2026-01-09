package sumo.sim;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CSV {

    private final String File;
    private final FileWriter fw;

    //Logger
    private static final Logger logger = java.util.logging.Logger.getLogger(CSV.class.getName());

    public CSV(String csvFile) throws IOException {
        this.File = csvFile;
        fw = new FileWriter(this.File, true);
        fw.write("\n");
    }


    public void addToCSV(String[] newData) {
        try {
            for(int i = 0; i < newData.length; i++){
                fw.write(newData[i]);
                fw.write("\n");
            }

        } catch (IOException i) {
            logger.log(Level.SEVERE, "Error while writing to CSV", i);
        }
    }

    public void close() throws IOException {
        fw.close();
    }

}