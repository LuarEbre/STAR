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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.text.SimpleDateFormat;

public class GuiController {

    @FXML
    private AnchorPane dataPane, root, middlePane, addMenu, filtersMenuSelect, mapMenuSelect, viewMenuSelect;
    @FXML
    private VBox fileMenuSelect;
    @FXML
    private ToggleButton playButton, selectButton, addButton, stressTestButton;
    @FXML
    private Button stepButton, addVehicleButton;
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
    @FXML
    private ListView<String> listData; // list displaying data as a string
    private final int defaultDelay;
    private final int maxDelay;
    private GraphicsContext gc;
    private SimulationRenderer sr;

    private WrapperController wrapperController;

    public GuiController() {
        defaultDelay = 50;
        maxDelay = 999;
    }

    public void setConnectionToWrapperCon(WrapperController wrapperController) {
        this.wrapperController = wrapperController;
        initializeRender();
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

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = // manages spinner
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, defaultDelay); //min,max, start
        delaySelect.setValueFactory(valueFactory);
        delaySelect.setEditable(true); // no longer read only

        // scales data field
        dataPane.prefWidthProperty().bind(middlePane.widthProperty().multiply(0.20));
        updateDataList();

        TextField delayTextField = delaySelect.getEditor(); // split spinner into its components -> text field
        delayTextField.setOnAction(e -> validateInput(delayTextField)); // action = enter, check input after "enter"

        // listener gets called whenever text inside delayTextField ist touched -> focused
        // listener arguments: obs -> property , old / Value of text field, focused -> user inside textfield
        delayTextField.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) { // if user exits text field re-evaluate input
                validateInput(delayTextField);
            }
        });

        map.widthProperty().bind(middlePane.widthProperty().multiply(0.79));
        map.heightProperty().bind(middlePane.heightProperty());

    }

    @FXML
    protected void onPlayStart() {
        stepButton.setDisable(true);
        playButton.setDisable(false);
        if (playButton.isSelected()) { // toggled
            wrapperController.startSim();
            //playSlider.setVisible(true);
        } else {
            wrapperController.stopSim();
            //playSlider.setVisible(false);
            stepButton.setDisable(false);
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
        wrapperController.doStepUpdate();
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
            //enableAllButtons();
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
        updateDelay();
        renderUpdate();
        // rendering?
        // connection time_step?
    }

    public void updateDelay() {
        if (delaySelect.getValue() != wrapperController.getDelay()) {
            wrapperController.changeDelay(delaySelect.getValue());
        }
    }

    public void updateTime() {
        int time = (int) wrapperController.getTime();
        StringBuilder b1 = new StringBuilder();
        int hours = time / 3600; // every 3600 ms is one hour
        int minutes = time % 3600 / 60; // minutes 0 to 3599 / 60
        int seconds =  time % 60; // seconds 0 - 59
        b1.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        timeLabel.setText(b1.toString());
    }

    public void updateDataList() {
        // list of data should be returned from vehicle/tl lists -> entry for every object, maybe list in listdata.getItems().addAll
        listData.getItems().clear();

        for (int i=0; i<4 ;i++) {
            listData.getItems().add("---- Vehicle #X ----");
            listData.getItems().addAll("Vehicle ID: ", "Type: ", "Route ID", "Color: ", "Speed: ", "Position: ",
                    "Angle: ", "Accel: ", "Decel: ", "Stop Time: ", ""
            ); // change = set/add(index, String) ; append = set(index, old + " + new text");
            // needs formula to calculate index for appending?
        }
    }

    public void validateInput(TextField editor) {
        try {
            int val = Integer.parseInt(editor.getText()); // if it is not an Integer -> exception
            if (val > maxDelay) {
                val = maxDelay;
            }
            if (val <= 0) {
                val = 1;
            }
            delaySelect.getValueFactory().setValue(val); // setting value
            editor.setText(String.valueOf(val));

        } catch (NumberFormatException e) { // catches exception
            delaySelect.getValueFactory().setValue(defaultDelay); // value of spinner resets
            editor.setText(String.valueOf(defaultDelay)); // displayed value resets to default
        }
    }

    public void initializeRender(){
        gc = map.getGraphicsContext2D();
        sr = new SimulationRenderer(map,gc,wrapperController.get_junction(),wrapperController.get_sl());
        renderUpdate();
    }

    public void renderUpdate(){
        sr.initRender();
    }

    @FXML
    public void addVehicle(){
        // parameters from addMenu components
        // static test
        wrapperController.addVehicle();
    }

    @FXML
    protected void mapClick(){
        System.out.println("mapClick");
        sr.moveX(100);
    }

    protected void zoomMap(){
        sr.zoomMap(1);
    }

}

