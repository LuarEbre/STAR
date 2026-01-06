package sumo.sim;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileReader {

    public FileReader() {

    }

    public void chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Sumo Config Files");
        // limits choosable files
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Sumo Config files", "*.sumocfg"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))); // start directory

        // opens os-specific file manager
        File file  = fileChooser.showOpenDialog(stage);
    }

    private void checkFile(File file) {
        if (file!=null) {

        }
    }
}
