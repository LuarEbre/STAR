package sumo.sim;

import java.io.FileWriter;
import java.io.IOException;

public class CSV {

    private final String File;
    private final FileWriter fw;

    public CSV(String csv_file) throws IOException {
        this.File = csv_file;
        fw = new FileWriter(this.File, true);
        fw.write("\n");
    }


    public void add_to_CsvFile(String new_data) {
        try {
            fw.append(new_data + "\n");

        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public void close() throws IOException {
        fw.close();
    }

}