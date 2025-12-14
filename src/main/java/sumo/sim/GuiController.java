package sumo.sim;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import java.util.function.UnaryOperator;

import javafx.scene.paint.Color;

public class GuiController {

    @FXML
    private AnchorPane dataPane, root, middlePane, addMenu, filtersMenuSelect, mapMenuSelect, viewMenuSelect, stressTestMenu, trafficLightMenu;

    // performance update -> addMenu and StressTestMenu in separate fxml files

    @FXML
    private ColorPicker colorSelector;
    @FXML
    private VBox fileMenuSelect;
    @FXML
    private ToggleButton playButton, selectButton, addButton, stressTestButton, trafficLightButton;
    @FXML
    private Button stepButton, addVehicleButton, amountMinus, amountPlus, startTestButton;
    @FXML
    private Spinner <Integer> delaySelect, durationTL;
    @FXML
    private Canvas map;
    @FXML
    private Label timeLabel, vehicleCount;
    @FXML
    private Slider playSlider;
    @FXML
    private ListView<String> listData; // list displaying data as a string
    @FXML
    private ChoiceBox<String> typeSelector, routeSelector, stressTestMode, tlSelector;
    @FXML
    private TextField amountField, stateText;
    @FXML
    private HBox mainButtonBox;

    private final int defaultDelay;
    private final int maxDelay;
    private GraphicsContext gc;
    private SimulationRenderer sr;
    private AnimationTimer renderLoop;

    // panning
    private double mousePressedXOld;
    private double mousePressedYOld;
    private double mousePressedXNew;
    private double mousePressedYNew;
    private double panSen; // sensitivity

    private WrapperController wrapperController;

    public GuiController() {
        defaultDelay = 50;
        maxDelay = 999;
        panSen = 2;
    }

    public void initializeCon(WrapperController wrapperController) {
        this.wrapperController = wrapperController;
        initializeRender();

        // initializing which is only possible after wrapper con was created

        // displays all available types found in xml
        String[] arr = wrapperController.getTypeList();
        typeSelector.setItems(FXCollections.observableArrayList(arr));
        int i = 0;
        boolean found = false;
        while (i< arr.length && !found ) {
            if (arr[i].equals("DEFAULT_VEHTYPE")) {
                found = true;
                typeSelector.setValue(arr[i]); // default vehtype standard
            }
            i++;
        }

        // Drop down menus
        String[] modes = { "Light Test" , "Medium Test" , "Heavy Test" };
        stressTestMode.setItems(FXCollections.observableArrayList(modes));
        stressTestMode.setValue(modes[0]);

        String[] routes = wrapperController.getRouteList();
        routeSelector.setItems(FXCollections.observableArrayList(wrapperController.getRouteList()));
        routeSelector.setValue(routes[0]);

        tlSelector.setItems(FXCollections.observableArrayList(wrapperController.getTLids()));
        tlSelector.setValue(wrapperController.getTLids()[0]);
        // initializes map pan
        mapPan();
        // starts renderer loop
        startRenderer();
    }


    @FXML
    public void initialize() {

        rescale(); // rescales menu based on width and height

        SpinnerValueFactory<Integer> valueFactory = // manages spinner
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, defaultDelay); //min, max, start
        delaySelect.setValueFactory(valueFactory);
        delaySelect.setEditable(true); // no longer read only

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

        // AddMenu's amountField
        // initializes amountField to spawn with "1"
        amountField.setText("1");
        // force our amountField to only accept numerical input based on regex
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) { // regex allows only digits 0-9
                return change;
            }
            return null; // regex rejects letters/symbols
        };

        TextFormatter<Integer> formatter = new TextFormatter<>(
                new javafx.util.converter.IntegerStringConverter(), // format to integer
                1, // default value = 1
                filter // use our regex filter
        );

        amountField.setTextFormatter(formatter);

        // only allow values from 1 to 1000, excluding adding 0 cars or far too many
        formatter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal > 1000) {
                formatter.setValue(1000);
            }
            if (newVal < 1) {
                formatter.setValue(1);
            }
        });

        // set initial colorSelector color to magenta to match our UI
        colorSelector.setValue(Color.MAGENTA);

        // initializes tl duration spinner
        SpinnerValueFactory<Integer> duration =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 20); //min, max, start
        durationTL.setValueFactory(duration);

        // if no routes exist in .rou files -> cant add vehicles, checked each frame in startrenderer
        startTestButton.setDisable(true);
        addVehicleButton.setDisable(true);
    }

    private void rescale(){
        // scales data field
        dataPane.prefWidthProperty().bind(middlePane.widthProperty().multiply(0.20));
        // scales map based on pane width and height
        map.widthProperty().bind(middlePane.widthProperty().multiply(0.795));
        map.heightProperty().bind(middlePane.heightProperty().multiply(0.985));
        mainButtonBox.prefWidthProperty().bind(middlePane.widthProperty().multiply(0.8));

       // stressTestMenu.translateXProperty().bind(middlePane.widthProperty().multiply(0.15));
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


    private void toggleMenuAtButton(Pane menu, Node button) {
        if (menu.isVisible()) {
            menu.setVisible(false);
            return;
        }
        menu.setVisible(true);
        //menu.applyCss();
        //menu.layout();

        Bounds buttonBounds = button.localToScene(button.getBoundsInLocal()); // position of buttons bound to screen
        double buttonCenterX = buttonBounds.getMinX() + (buttonBounds.getWidth() / 2); // middle position of button
        double menuX = buttonCenterX - (menu.getWidth() / 2);
        double menuY = buttonBounds.getMinY() - menu.getHeight() - 10;
        Point2D localPos = root.sceneToLocal(menuX, menuY);

        menu.setLayoutX(localPos.getX());
        menu.setLayoutY(localPos.getY());
        //menu.toFront();
    }

    public void closeAllMenus() {
        if (filtersMenuSelect != null) filtersMenuSelect.setVisible(false);
        if (mapMenuSelect != null) mapMenuSelect.setVisible(false);
        if (viewMenuSelect != null) viewMenuSelect.setVisible(false);
        if (fileMenuSelect != null) fileMenuSelect.setVisible(false);;

        // still needs fix for small gap between buttons and menus at the top
    }

    @FXML
    protected void amountMinus() {
        String oldVal = amountField.getText();
        int newVal = Integer.parseInt(oldVal)-1;
        amountField.setText(String.valueOf(newVal));
    }

    @FXML
    protected void amountPlus() {
        String oldVal = amountField.getText();
        int newVal = Integer.parseInt(oldVal)+1;
        amountField.setText(String.valueOf(newVal));
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
    protected void onTrafficLight() {
        sr.setShowTrafficLightIDs(!sr.getShowTrafficLightIDs());
        toggleMenuAtButton(trafficLightMenu, trafficLightButton);

    }

    @FXML
    protected void onSelect(){
        if (selectButton.isSelected()) { // toggled
        } else {
            System.out.println("Stopped");
        }
    }

    @FXML
    protected void onAdd(){
        toggleMenuAtButton(addMenu, addButton);
    }

    @FXML
    protected void onFiltersHover(MouseEvent event){
        closeAllMenus();
        filtersMenuSelect.setVisible(true);
    }

    @FXML
    protected void onMapsHover(MouseEvent event){
        // deactivate all menus
        closeAllMenus();
        // activate Map menu
        mapMenuSelect.setVisible(true);
    }

    @FXML
    protected void onViewHover(MouseEvent event){
        closeAllMenus();
        viewMenuSelect.setVisible(true);
    }

    @FXML
    protected void onFileHover(MouseEvent event){
        closeAllMenus();
        fileMenuSelect.setVisible(true);
    }

    @FXML
    protected void onStressTest(){
        toggleMenuAtButton(stressTestMenu, stressTestButton);
    }

    @FXML
    protected void startStressTest(){
        String mode = stressTestMode.getValue();
        // experimental
        if (mode.equals("Light Test")) {
            wrapperController.StressTest(10, Color.GREEN);
        } else if (mode.equals("Medium Test")) {
            wrapperController.StressTest(100, Color.YELLOW);
        } else if (mode.equals("Heavy Test")) {
            wrapperController.StressTest(1000, Color.RED);
        }
    }


    @FXML
    protected void onMiddlePaneHover(){

    }

    @FXML
    protected void applyTLsettings() {
        String id = tlSelector.getValue();
        int duration = durationTL.getValue();
        wrapperController.setTlSettings(id, duration);
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
        // updates UI elements
        updateTime();
        updateDelay();
        updateCountVeh();
        updateTLPhaseText();
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


    @FXML
    protected void onStep() {
        wrapperController.doStepUpdate();
    }

    private void updateCountVeh() {
        int c = wrapperController.updateCountVehicle(); // updates count everytime a new veh is added
        int all = wrapperController.getAllVehicleCount();
        vehicleCount.setText(all+"/"+c);
    }

    private void updateTLPhaseText() {
        if (trafficLightMenu.isVisible()) {
            String[] stateDur = wrapperController.getTlStateDuration(tlSelector.getValue());
            String text ="";
            double nextSwitchAbsolute = Double.parseDouble(stateDur[stateDur.length-1]); // returns time when tl is switched
            double currentTime = wrapperController.getTime(); // current time of sim
            double remaining = nextSwitchAbsolute - currentTime; // remaining time
            for (int i=0; i<stateDur.length-2; i++) {
                text = text + stateDur[i];
            }
            text = text + ", dur: "+ remaining +"/"+ stateDur[stateDur.length-2];
            stateText.setText(text);
        } else {
            stateText.setText("");
        }

        // later: forcing with xml writing and do job get
    }


    // Render

    public void startRenderer() { // maybe with connection as argument? closing connection opened prior
        renderLoop = new AnimationTimer() { // javafx class -> directly runs on javafx thread
            @Override
            public void handle(long timestamp) {
                renderUpdate();

                // other functions that should update every frame
                if(wrapperController.isRouteListEmpty() || routeSelector.getValue().equals("CUSTOM")) {
                    addVehicleButton.setDisable(true);
                    startTestButton.setDisable(true);
                } else {
                    addVehicleButton.setDisable(false);
                    startTestButton.setDisable(false);
                }
            }
        };
        renderLoop.start(); // runs 60 frames per second
    }

    @FXML
    protected void closeApplication() {
        renderLoop.stop(); // terminates Animation Timer
        Platform.exit(); // terminates JavaFX thread
        wrapperController.terminate(); // terminates sumo connection and wrapCon thread
    }

    public void initializeRender(){
        gc = map.getGraphicsContext2D();
        sr = new SimulationRenderer(map,gc,wrapperController.getJunctions(),wrapperController.getStreets(),
                wrapperController.getVehicles(), wrapperController.getTrafficLights());
        renderUpdate();
    }

    public void renderUpdate(){
        sr.initRender();
    }

    @FXML
    public void addVehicle(){
        // parameters from addMenu components
        // static test
        int amount = Integer.parseInt(amountField.getText());
        Color color = colorSelector.getValue();
        String type = typeSelector.getValue();
        String route = routeSelector.getValue();
        if(route == null) {
            route = "r0"; // if route count == 0 -> disable add button, disable stress test start
        }

        wrapperController.addVehicle(amount, type, route, color);
    }

    @FXML
    protected void mapClick(){

    }

    @FXML
    protected void zoomMap(ScrollEvent event){

        if (event.getDeltaY() > 0) { // delta y vertical
            sr.zoomMap(1.2);
            //System.out.println("zoom");
        } else  {
            //System.out.println("zoomout");
            sr.zoomMap(0.8);
        }
    }

    public void mapPan() {
        map.setOnMousePressed(event -> {
            mousePressedXOld = event.getX(); // old
            mousePressedYOld = event.getY();
            //System.out.println("old"+mousePressedXOld + " " + mousePressedYOld);
        });
        // drag start with pressed -> then this follows
        map.setOnMouseDragged(e->{
            mousePressedXNew = e.getX();
            mousePressedYNew = e.getY();
            //System.out.println("NewX"+mousePressedXNew + " NewY " + mousePressedYNew);
            double panX = -1 * (mousePressedXNew - mousePressedXOld) / panSen; // -1 so that panning to the left swipes to the right
            double panY = (mousePressedYNew - mousePressedYOld) / panSen; // already reversed
            sr.padMad(panX, panY);
            mousePressedXOld = e.getX(); // resetting old values
            mousePressedYOld = e.getY();
        });
    }

    public void setPanSens(double s) {
        panSen = s;
    }

}

