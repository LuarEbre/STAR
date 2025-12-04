package sumo.sim;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

//GUI application
public class GuiApplication extends Application {
    private static GuiController guiController; // static because you dont instance guiCon -> javafx instances the controller

    @Override
    public void start(Stage stage) throws IOException {
        // link to gui.fxml (more than one possible)
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Gui/gui.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        // link to css file
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Gui/gui.css")).toExternalForm());
        scene.setCursor(new ImageCursor(new Image("/Gui/Icons/cursor.png",  128, 128, true, true))); // changes cursor style
        guiController = fxmlLoader.getController();

        // stage (frame)
       // stage.setFullScreen(true); //needs escape button to close the appl.
        stage.fullScreenExitHintProperty().setValue("Press Esc to exit");
        //stage.initStyle(StageStyle.UNDECORATED); // removes frame and title
        stage.setScene(scene);
        stage.show(); // display gui

        WrapperController wrapper = new WrapperController(guiController);
        guiController.setConnectionToWrapperCon(wrapper);

    }
}
