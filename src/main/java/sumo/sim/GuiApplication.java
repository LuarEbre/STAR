package sumo.sim;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;

//GUI application
public class GuiApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/gui.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("/Gui/gui.fxml")); // loading from fxml
        Scene scene = new Scene(root);
        stage.setTitle("SUMO");
        //stage.setResizable(false);
        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.show(); // display gui
    }
}
