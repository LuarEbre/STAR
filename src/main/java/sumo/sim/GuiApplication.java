package sumo.sim;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sumo.sim.logic.SumoMapManager;
import sumo.sim.logic.WrapperController;

import java.io.IOException;
import java.util.Objects;

/**
 * Class for loading FXML and CSS files. Extends JavaFX Application and therefor must override {@link #start(Stage)}
 *
 * <p>
 *
 * </p>
 */
//GUI application
public class GuiApplication extends Application {
    private static GuiController guiController; // static because you dont instance guiCon -> javafx instances the controller
    private WrapperController wrapper;

    /**
     * Loads FXML file and CSS file and sets the stage
     *
     * <p>
     *     When loading and setting the FXML file. {@link GuiController} is created by JavaFX
     *     and WrapperController constructor is called to enable the connection
     *     between GUI and Simulation
     * </p>
     * @param stage , visible JavaFX frame acting as root element and containing all FXML (GUI) objects
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
        // link to gui.fxml (more than one possible)
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Gui/gui.fxml"));
        //FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Gui/Title/title.fxml")); // experimental

        Scene scene = new Scene(fxmlLoader.load());

        Image appIcon = new Image(getClass().getResourceAsStream("/Gui/Icons/STAR.png"));
        stage.getIcons().add(appIcon);

        // link to css file
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Gui/gui.css")).toExternalForm());
        scene.setCursor(new ImageCursor(new Image("/Gui/Icons/cursor.png",  128, 128, true, true))); // changes cursor style
        guiController = fxmlLoader.getController();

        // stage (frame)
        stage.setFullScreen(true); //needs escape button to close the appl.
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (KeyCode.F11.equals(event.getCode())) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });

        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (KeyCode.ENTER.equals(event.getCode()) && event.isAltDown()) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });

        stage.fullScreenExitHintProperty().setValue("Press Esc to exit");
        stage.initStyle(StageStyle.UNDECORATED); // removes frame and title
        stage.setScene(scene);
        stage.show(); // display gui

        // Establishing connection between WrappCon and GuiCon

        SumoMapManager mapManager = new SumoMapManager(); // initializes Maps
        wrapper = new WrapperController(guiController, mapManager);
        guiController.initializeCon(wrapper);
        guiController.setStageAndManager(stage, mapManager);
    }

    @Override
    public void stop() throws Exception {
        if (this.wrapper != null) {
            this.wrapper.terminate();
        }
        // JavaFx stop
        super.stop();
        // Forces termination of all threads
        System.exit(0);
    }
}
