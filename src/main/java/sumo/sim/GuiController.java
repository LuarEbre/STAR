package sumo.sim;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class GuiController {

    @FXML
    private AnchorPane dataPane, root, middlePane, addMenu, filtersMenuSelect, mapMenuSelect, viewMenuSelect;
    @FXML
    private VBox fileMenuSelect;
    @FXML
    private ToggleButton playButton, selectButton, addButton, stressTestButton;
    @FXML
    private Button stepButton;
    @FXML
    private Spinner <Integer> delaySelect;
    @FXML
    private Circle circ1, circ2;
    @FXML
    private Rectangle rect1;
    @FXML
    private Canvas map;
    @FXML
    private Label timeLabel;
    @FXML
    private Slider playSlider;

    private WrapperController wrapperController;

    public GuiController() {

    }

    public void setConnectionToWrapperCon(WrapperController wrapperController) {
        this.wrapperController = wrapperController;
    }

    public void closeAllMenus() {
        if (filtersMenuSelect != null) filtersMenuSelect.setVisible(false);
        if (mapMenuSelect != null) mapMenuSelect.setVisible(false);
        if (viewMenuSelect != null) viewMenuSelect.setVisible(false);
        if (fileMenuSelect != null) fileMenuSelect.setVisible(false);
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

    private void redraw(GraphicsContext gc, Image img) {
        gc.clearRect(0, 0, map.getWidth(), map.getHeight());
        gc.drawImage(img, 0, 0, map.getWidth(), map.getHeight());
    }

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = // manages spinner
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 500, 50); //min,max, start
        delaySelect.setValueFactory(valueFactory);

        // scales data field
        dataPane.prefWidthProperty().bind(middlePane.widthProperty().multiply(0.20));

        /*
        GraphicsContext gc = map.getGraphicsContext2D();
        Image img = new Image("/Gui/Render/mapEx.png");

        map.widthProperty().bind(middlePane.widthProperty().multiply(0.79));
        map.heightProperty().bind(middlePane.heightProperty().multiply(0.98));

        map.widthProperty().addListener((obs, oldV, newV) -> {
            redraw(gc, img);
        });
        map.heightProperty().addListener((obs, oldV, newV) -> {
            redraw(gc, img);
        });

        middlePane.sceneProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) redraw(gc, img);
        });
            */
    }

    @FXML
    protected void onPlayStart() {
        disableAllButtons();
        playButton.setDisable(false);
        if (playButton.isSelected()) { // toggled
            wrapperController.startSim();
            //playSlider.setVisible(true);
        } else {
            wrapperController.stopSim();
            //playSlider.setVisible(false);
            enableAllButtons();
        }
    }

    @FXML
    protected void onSelect(){
        if (selectButton.isSelected()) { // toggled

        } else {
            System.out.println("Stopped");
        }
    }

    @FXML
    protected void onStep() {
        wrapperController.doSingleStep();
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
            enableAllButtons();
        }
    }

    @FXML
    protected void onFiltersHover(MouseEvent event){
        closeAllMenus();
        closeZoomMenu();
        filtersMenuSelect.setVisible(true);
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
    protected void onViewHover(MouseEvent event){
        closeAllMenus();
        closeZoomMenu();
        viewMenuSelect.setVisible(true);
    }

    @FXML
    protected void onFileHover(MouseEvent event){
        closeAllMenus();
        closeZoomMenu();
        fileMenuSelect.setVisible(true);
    }

    @FXML
    protected void onMiddlePaneHover(){

    }

    @FXML
    protected void closeApplication() { // later extra button in file
        Platform.exit();
        wrapperController.terminate();
    }

    // functionality

    public void disableAllButtons(){
        selectButton.setDisable(true);
        playButton.setDisable(true);
        addButton.setDisable(true);
        stressTestButton.setDisable(true);
        stepButton.setDisable(true);
    }

    public void enableAllButtons(){
        selectButton.setDisable(false);
        playButton.setDisable(false);
        addButton.setDisable(false);
        stressTestButton.setDisable(false);
        stepButton.setDisable(false);
    }

    public void doSimStep() {
        updateTime();
        // rendering?
        // connection time_step?
    }

    public void updateTime() {
        // exception handling needed -> if getTime connection is closed
        int time = (int) wrapperController.getTime();
        StringBuilder b1 = new StringBuilder();
        int hours = time / 3600; // every 3600 ms is one hour
        int minutes = time % 3600 / 60; // minutes 0 to 3599 / 60
        int seconds =  time % 60; // seconds 0 - 59
        b1.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        timeLabel.setText(b1.toString());
    }
}

