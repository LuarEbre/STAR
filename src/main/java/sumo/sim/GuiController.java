package sumo.sim;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class GuiController {

    @FXML
    private AnchorPane dataPane, root, middlePane, addMenu, filtersMenuSelect, mapMenuSelect, viewMenuSelect;
    @FXML
    private ToggleButton playButton, selectButton, addButton;
    @FXML
    private Spinner <Integer> delaySelect;
    @FXML
    private Circle circ1, circ2;
    @FXML
    private Rectangle rect1;

    public void closeAllMenus() {
        if (filtersMenuSelect != null) filtersMenuSelect.setVisible(false);
        if (mapMenuSelect != null) mapMenuSelect.setVisible(false);
        if (viewMenuSelect != null) viewMenuSelect.setVisible(false);
        openZoomMenu();
        // still needs fix for small gap between buttons and menus at the top
    }

    public void closeZoomMenu() {
        circ1.setVisible(false);
        circ2.setVisible(false);
        rect1.setVisible(false);
    }

    public void openZoomMenu() {
        circ1.setVisible(true);
        circ2.setVisible(true);
        rect1.setVisible(true);
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
        closeZoomMenu();
        filtersMenuSelect.setVisible(true);
    }
    @FXML
    protected void onFilterMenuExit(MouseEvent event) {
        closeAllMenus();
    }

    @FXML
    protected void onMapsHover(MouseEvent event){
        // deactivate all menus
        closeAllMenus();
        closeZoomMenu();
        // activate Map menu
        mapMenuSelect.setVisible(true);
    }
    @FXML
    protected void onMapsMenuExit(MouseEvent event) { // needs check if mouse exited on the left
        closeAllMenus();;
    }

    @FXML
    protected void onViewHover(MouseEvent event){
        closeAllMenus();
        closeZoomMenu();
        viewMenuSelect.setVisible(true);
    }

    @FXML
    protected void onViewMenuExit(MouseEvent event){
        closeAllMenus();
    }


    @FXML
    protected void onFileHover(MouseEvent event){
        closeAllMenus();
        closeZoomMenu();
    }

    @FXML
    protected void onMiddlePaneHover(){

    }

    @FXML
    protected void closeApplication() { // later extra button in file
        Platform.exit();
    }

}

