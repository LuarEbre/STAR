package sumo.sim;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class GuiTitleController {
    private static GuiController guiController;
    @FXML
    private Button startButton;

    @FXML
    protected void start() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Gui/gui.fxml"));
        try {
            Parent root = fxmlLoader.load();
            guiController = fxmlLoader.getController();
            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setFullScreen(true);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Gui/gui.css")).toExternalForm());
            stage.setScene(scene);
            WrapperController wrapper = new WrapperController(guiController);
            guiController.initializeCon(wrapper);

            stage.fullScreenExitHintProperty().setValue("Press Esc to exit");
            //stage.initStyle(StageStyle.UNDECORATED); // removes frame and title
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
