package sumo.sim;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class GuiController {
    // attributes from FXML to communicate with objects
    @FXML
    public AnchorPane addMenue;
    @FXML
    private ToggleButton playButton;
    @FXML
    private ToggleButton selectButton;
    @FXML
    private ToggleButton addButton;
    @FXML
    private Label timeLabel;
    @FXML
    private Spinner <Integer> delaySelect;
    @FXML
    private VBox selectMenue;

    @FXML
    public void initialize() {
        timeLabel.setText("00:00");
        SpinnerValueFactory<Integer> valueFactory = // manages spinner
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 500, 50); //min,max, start
        delaySelect.setValueFactory(valueFactory);

    }

    @FXML
    protected void onPlayStart() {
        if (playButton.isSelected()) { // toggled
            System.out.println("Started");
        } else {
            System.out.println("Stopped");
        }
    }

    @FXML
    protected void onSelect(){
        if (selectButton.isSelected()) { // toggled
            System.out.println("Started");
        } else {
            System.out.println("Stopped");
        }
    }

    @FXML
    protected void onAdd(){ // experimental animation
        FadeTransition fade = new FadeTransition(Duration.millis(200), addMenue);
        if (addButton.isSelected()) { // toggled
            addMenue.setVisible(true);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        } else {
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.play();
            addMenue.setVisible(false);
        }
    }

}

