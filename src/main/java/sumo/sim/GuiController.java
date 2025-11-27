package sumo.sim;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GuiController {
    @FXML
    private Label welcomeText;

    @FXML // example for button execution, doesnt do anything
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
