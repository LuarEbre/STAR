package sumo.sim;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class GuiController {

    @FXML
    private AnchorPane dataPane, root, middlePane, addMenu, filtersMenuSelect, mapMenuSelect, viewMenuSelect;
    @FXML
    private ToggleButton playButton, selectButton, addButton;
    @FXML
    private Label timeLabel;
    @FXML
    private Spinner <Integer> delaySelect;

    public void closeAllMenus() {
        if (filtersMenuSelect != null) filtersMenuSelect.setVisible(false);
        if (mapMenuSelect != null) mapMenuSelect.setVisible(false);
        if (viewMenuSelect != null) viewMenuSelect.setVisible(false);
    }

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = // manages spinner
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 500, 50); //min,max, start
        delaySelect.setValueFactory(valueFactory);

        // scales data field
        dataPane.prefWidthProperty().bind(middlePane.widthProperty().multiply(0.20));

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
        FadeTransition fade = new FadeTransition(Duration.millis(200), addMenu);
        if (addButton.isSelected()) { // toggled
            addMenu.setVisible(true);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        } else {
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.play();
            addMenu.setVisible(false);
        }
    }

    @FXML
    protected void onFiltersHover(MouseEvent event){
        closeAllMenus();
        filtersMenuSelect.setVisible(true);
    }
    @FXML
    protected void onFilterMenuExit(MouseEvent event) {
        filtersMenuSelect.setVisible(false);
    }

    @FXML
    protected void onMapsHover(MouseEvent event){
        // deactivate all menus
        closeAllMenus();
        // activate Map menu
        mapMenuSelect.setVisible(true);
    }
    @FXML
    protected void onMapsMenuExit(MouseEvent event) { // needs check if mouse exited on the left
        mapMenuSelect.setVisible(false);
    }

    @FXML
    protected void onViewHover(MouseEvent event){
        closeAllMenus();
        viewMenuSelect.setVisible(true);
    }

    @FXML
    protected void onViewMenuExit(MouseEvent event){
        closeAllMenus();
    }


    @FXML
    protected void onFileHover(MouseEvent event){
        closeAllMenus();
    }

    @FXML
    protected void onMiddlePaneHover(){

    }

    @FXML
    protected void closeApplication() { // later extra button in file
        Platform.exit();
    }

}

