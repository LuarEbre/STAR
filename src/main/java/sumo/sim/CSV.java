package sumo.sim;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CSV {

    private final String File;
    private final FileWriter fw;

    public CSV(String csv_file) throws IOException {
        this.File = csv_file;
        fw = new FileWriter(this.File, true);
        fw.write("\n");
    }


    public void add_to_CsvFile(String[] new_data) {
        try {
            for(int i = 0; i < new_data.length; i++){
                fw.write(new_data[i]);
                fw.write("\n");
            }

        } catch (IOException i) {
            System.out.println("Could not write Data to CSV File" + i);
        }
    }

    public void close() throws IOException {
        fw.close();
    }

}