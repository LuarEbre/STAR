package sumo.sim;

import java.io.FileWriter;
import java.io.IOException;
import static sumo.sim.Main.LOG;

public class CSV {

    private final String File;
    private final FileWriter fw;

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
            LOG.error("Could not write Data to CSV File" + i);
        }
    }

    public void close() throws IOException {
        fw.close();
    }

}