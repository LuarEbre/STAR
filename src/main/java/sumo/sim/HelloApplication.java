package sumo.sim;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/gui.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("/Gui/gui.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("SUMO");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}
