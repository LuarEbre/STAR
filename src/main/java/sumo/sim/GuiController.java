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

import java.util.Locale;
import java.util.function.UnaryOperator;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Main JavaFX controller for the simulation GUI and gui.fxml.
 *
 * <p>
 * This class handles all GUI elements such as buttons, panes and labels,
 * as well as all user interactions and communication with the
 * {@link WrapperController}.
 * </p>
 *
 * <p>
 * The controller runs on the JavaFX Application Thread.
 * Rendering is performed by the {@link SimulationRenderer} and
 * initialized via {@link #initializeRender()}.
 * </p>
 *
 * @author Leandro Liuzzo
 */
public class GuiController {
    // all FXML objects
    @FXML
    private AnchorPane dataPane, root, middlePane, addMenu, filtersMenuSelect, mapMenuSelect, viewMenuSelect, stressTestMenu, trafficLightMenu;
    @FXML
    private ColorPicker colorSelector;
    @FXML
    private VBox fileMenuSelect;
    @FXML
    private ToggleButton playButton, selectButton, addButton, stressTestButton, trafficLightButton;
    @FXML
    private Button stepButton, addVehicleButton, amountMinus, amountPlus, startTestButton, map1select, map2select;

    private ButtonBase[] allButtons;

    @FXML
    private Spinner <Integer> delaySelect, durationTL;
    @FXML
    private Canvas staticMap, dynamicMap;
    @FXML
    private Label timeLabel, vehicleCount;
    @FXML
    private Slider playSlider;
    @FXML
    private ListView<String> listData; // list displaying data as a string
    @FXML
    private ChoiceBox<String> typeSelector, routeSelector, stressTestMode, tlSelector;
    @FXML
    private CheckBox buttonView, dataView;
    @FXML
    private TextField amountField, stateText, activeVehicles, VehiclesNotOnScreen, DepartedVehicles, VehiclesCurrentlyStopped, TotalTimeSpentStopped, MeanSpeed, SpeedSD;
    @FXML
    private TabPane tabPane;
    @FXML
    private HBox mainButtonBox;
    @FXML
    private CheckBox showDensityAnchor, showDataOutput, showButtons, showRouteHighlighting, showTrafficLightIDs;

    private GraphicsContext gc;
    private SimulationRenderer sr;
    private AnimationTimer renderLoop;

    // dragging window
    private double xOffset, yOffset;

    // panning
    private double mousePressedXOld;
    private double mousePressedYOld;
    private double mousePressedXNew;
    private double mousePressedYNew;
    private double panSen; // sensitivity

    // sim
    private WrapperController wrapperController;
    private final int defaultDelay;
    private final int maxDelay;

    /**
     * <p>
     * Is created by FXML loader in {@link GuiApplication}
     * after launching Application in {@link Main} class
     *
     * </p>
     * @author Leandro Liuzzo
     */
    public GuiController() {
        this.defaultDelay = 50;
        this.maxDelay = 999;
        panSen = 2;
    }

    /**
     * Initializes the GUI controller using the given {@link WrapperController}.
     *
     * <p>
     * This method establishes the connection to the simulation backend and
     * performs all initialization steps that require an active
     * {@link WrapperController}, including:
     * </p>
     *
     * <ul>
     *   <li>Populating UI components (types, routes, traffic lights)</li>
     *   <li>Initializing the renderer {@link #initializeRender()} </li>
     *   <li>Enabling map interaction {@link #mapPan()} </li>
     *   <li>Starting the render loop {@link #startRenderer()} </li>
     *   <li>Initializes all drop down GUI menus (e.g. StressTestMode, routeSelector)</li>
     * </ul>
     *
     * @param wrapperController the simulation wrapper used for backend communication with the simulation
     */
    public void initializeCon(WrapperController wrapperController) {
        this.wrapperController = wrapperController;

        // allows map switching
        map1select.setDisable(false);
        map2select.setDisable(false);

        // initializing which is only possible after wrapper con was created
        initializeDropDowns();

        // rendering
        stopRenderer(); // stops animation timer if already active
        initializeRender();

        // initializes map pan
        mapPan();

        // starts renderer loop
        startRenderer();
    }

    private void initializeDropDowns() {
        if (wrapperController==null) return;

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

        allButtons = new ButtonBase[]{
                playButton,
                selectButton,
                addButton,
                stressTestButton,
                trafficLightButton,
                stepButton
        };

        String[] modes = { "Light Test" , "Medium Test" , "Heavy Test" };
        stressTestMode.setItems(FXCollections.observableArrayList(modes));
        stressTestMode.setValue(modes[0]);

        String[] routes = wrapperController.getRouteList();
        routeSelector.setItems(FXCollections.observableArrayList(wrapperController.getRouteList()));
        routeSelector.setValue(routes[0]);

        tlSelector.setItems(FXCollections.observableArrayList(wrapperController.getTLids()));
        tlSelector.setValue(wrapperController.getTLids()[0]);

    }

    /**
     * Is called automatically after FXML loader has created {@link GuiController}.
     *
     * <p>
     *  This method initializes everything that does not require a connection to {@link WrapperController}.
     *
     * </p>
     *
     * <p>
     *     Methods called:
     *  <ul>
     *      <li> {@link #rescale()} rescales GUI elements based on height and width of the frame. </li>
     *      <li> {@link #updateDataPane()} updates data Listview object displayed on the left Pane. </li>
     *      <li> {@link #validateInput(TextField)} checks weather the given input is correct or not. </li>
     *  </ul>
     * </p>
     */
    @FXML
    public void initialize() {
        rescale(); // rescales menu based on width and height
        setUpInputs(); // Spinner factory etc. initializing
        // set initial colorSelector color to magenta to match our UI
        colorSelector.setValue(Color.MAGENTA);

        // if no routes exist in .rou files -> cant add vehicles, checked each frame in startrenderer
        startTestButton.setDisable(true);
        addVehicleButton.setDisable(true);
    }

    private void setUpInputs() {
        SpinnerValueFactory<Integer> valueFactory = // manages spinner
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, defaultDelay); //min, max, start
        delaySelect.setValueFactory(valueFactory);
        delaySelect.setEditable(true); // no longer read only

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



        // initializes tl duration spinner
        SpinnerValueFactory<Integer> duration =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 20); //min, max, start
        durationTL.setValueFactory(duration);
    }

    @FXML
    private void mouseClicked(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void dragWindow(MouseEvent event) {
        Stage stage = (Stage) root.getScene().getWindow();
        if (stage.isFullScreen()) return;
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    /**
     * This method scales GUI elements based on height and width:
     *
     * <p>
     *    <ul>
     *           <li> dataPane , displays data  </li>
     *            <li> map , where map is rendered </li>
     *           <li> mainButtonBox, adjusts width of main buttons </li>
     *      </ul>
     * </p>
     */
    private void rescale(){
        // scales data field
        dataPane.prefWidthProperty().bind(middlePane.widthProperty().multiply(0.20)); // 20 percent of the width
        // scales map based on pane width and height
        staticMap.widthProperty().bind(middlePane.widthProperty().multiply(0.795));
        staticMap.heightProperty().bind(middlePane.heightProperty().multiply(0.985));
       // dynamicMap.widthProperty().bind(middlePane.widthProperty().multiply(0.795));
       // dynamicMap.heightProperty().bind(middlePane.heightProperty().multiply(0.985));
        mainButtonBox.prefWidthProperty().bind(middlePane.widthProperty().multiply(0.8));

       // stressTestMenu.translateXProperty().bind(middlePane.widthProperty().multiply(0.15));
    }

    /**
     * Validates the input of a {@link TextField} associated with the delay spinner.
     *
     * <p>
     * This method ensures that the value entered by the user is a valid integer
     * within the allowed range (1 to {@link #maxDelay}). If the value is higher
     * than {@link #maxDelay}, it is set to {@link #maxDelay}. If it is zero or negative,
     * it is set to 1. In case the input cannot be parsed as an integer,
     * the value is reset to the default delay ({@link #defaultDelay}).
     * </p>
     *
     * <p>
     * After validation, both the spinner's value and the text field are updated
     * to reflect the corrected value.
     * </p>
     *
     * @param editor the {@link TextField} to validate
     */
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

    /**
     * Toggles the visibility of the given menu and positions it relative to the given button.
     *
     * <p>
     * If the menu is already visible, it will be hidden. Otherwise, it is shown and
     * positioned above the button with a small vertical offset.
     * </p>
     *
     * @param menu   the menu pane to show or hide
     * @param button the button relative to which the menu is positioned
     */
    private void toggleMenuAtButton(Pane menu, Node button) {
        if (menu.isVisible()) {
            menu.setVisible(false);
            return;
        }
        menu.setVisible(true);

        Bounds buttonBounds = button.localToScene(button.getBoundsInLocal()); // position of buttons bound to screen
        double buttonCenterX = buttonBounds.getMinX() + (buttonBounds.getWidth() / 2); // middle position of button
        double menuX = buttonCenterX - (menu.getWidth() / 2);
        double menuY = buttonBounds.getMinY() - menu.getHeight() - 10;
        Point2D localPos = root.sceneToLocal(menuX, menuY);

        menu.setLayoutX(localPos.getX());
        menu.setLayoutY(localPos.getY());
    }

    /**
     * This method closes all menus (invisible).
     */
    public void closeAllMenus() {
        if (filtersMenuSelect != null) filtersMenuSelect.setVisible(false);
        if (mapMenuSelect != null) mapMenuSelect.setVisible(false);
        if (viewMenuSelect != null) viewMenuSelect.setVisible(false);
        if (fileMenuSelect != null) fileMenuSelect.setVisible(false);;

        // still needs fix for small gap between buttons and menus at the top
    }

    /**
     * <p>
     *     Is triggered when pressing "-" on amountButton
     *     <li> Reads and decrements value inside amount text field </li>
     * </p>
     */
    @FXML
    protected void amountMinus() {
        String oldVal = amountField.getText();
        int newVal = Integer.parseInt(oldVal)-1;
        amountField.setText(String.valueOf(newVal));
    }

    /**
     * Equivalent to {@link #amountMinus()}.
     */
    @FXML
    protected void amountPlus() {
        String oldVal = amountField.getText();
        int newVal = Integer.parseInt(oldVal)+1;
        amountField.setText(String.valueOf(newVal));
    }

    // main buttons pressed

    /**
     * <p>
     *     Called when "play button" is pressed:
     *     <li> disables "step button." </li>
     *     <li> calls {@link WrapperController} methode {@link WrapperController#startSim()} to start
     *     and {@link WrapperController#stopSim()} to stop the simulation.</li>
     * </p>
     */
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

    /**
     * <p>
     *     Called when "traffic light button" is pressed.
     *     <li> Calls {@link SimulationRenderer#setSeeTrafficLightIDs(boolean)}.</li>
     *     <li> Displays TL menu via {@link #toggleMenuAtButton(Pane, Node)}.</li>
     * </p>
     *
     */
    @FXML
    protected void onTrafficLight() {
        sr.setSeeTrafficLightIDs(!sr.getSeeTrafficLightIDs());
        toggleMenuAtButton(trafficLightMenu, trafficLightButton);
    }

    /**
     * Called when "select button" is pressed.
     */
    @FXML
    protected void onSelect(){
        if (selectButton.isSelected()) {

        } else {

        }
    }

    /**
     * Called when "add button" is pressed
     * and displays add menu.
     */
    @FXML
    protected void onAdd(){
        sr.setPickedARoute(!sr.getPickedARoute());
        toggleMenuAtButton(addMenu, addButton);
        String Route = routeSelector.getValue();
        sr.setPickedRouteID(Route);
    }

    /**
     * Called when "stress test button" is pressed
     * and displays stress test menu.
     */
    @FXML
    protected void onStressTest(){
        toggleMenuAtButton(stressTestMenu, stressTestButton);
    }

    /**
     * <p>
     *     Called when "step button" is pressed.
     *     <li> Runs {@link WrapperController#doStepUpdate()} method with every click.</li>
     * </p>
     */
    @FXML
    protected void onStep() {
        wrapperController.doStepUpdate();
    }

    // top right menu buttons hovered

    /**
     * Is triggered when user hovers over "filter" button
     *
     * <p>
     *     Calls {@link #closeAllMenus()} method to ensure that no menus are stacked on top of each other and
     *     sets menu to "visible"
     * </p>
     */
    @FXML
    protected void onFiltersHover(){
        closeAllMenus();
        filtersMenuSelect.setVisible(true);
    }

    /**
     * Equivalent to {@link #onFiltersHover()}
     */
    @FXML
    protected void onMapsHover(){
        // deactivate all menus
        closeAllMenus();
        // activate Map menu
        mapMenuSelect.setVisible(true);
    }

    /**
     * Equivalent to {@link #onFiltersHover()}
     */
    @FXML
    protected void onViewHover(){
        closeAllMenus();
        viewMenuSelect.setVisible(true);
    }

    @FXML void onDataOutputToggle() {

    }

    @FXML void onButtonToggle() {
        for(ButtonBase button: allButtons) {
            button.setDisable(!showButtons.isSelected());
            button.setVisible(showButtons.isSelected());
        }
    }

    @FXML
    protected void onDensityAnchorToggle() {
        sr.setShowDensityAnchor(showDensityAnchor.isSelected());
    }

    @FXML
    protected void onRouteHighlightingToggle() { sr.setShowRouteHighlighting(showRouteHighlighting.isSelected()); }

    @FXML
    protected void onTrafficLightIDToggle() { sr.setShowTrafficLightIDs(showTrafficLightIDs.isSelected()); }

    @FXML
    protected void onMiddlePaneHover(){

    }


    /**
     * Equivalent to {@link #onFiltersHover()}
     */
    @FXML
    protected void onFileHover(){
        closeAllMenus();
        fileMenuSelect.setVisible(true);
    }

    // main buttons menu methods

    @FXML
    protected void startStressTest(){
        String mode = stressTestMode.getValue();
        if (mode.equals("Light Test")) {
            wrapperController.StressTest(1000, Color.GREEN, null);
        } else if (mode.equals("Medium Test")) {
            wrapperController.StressTest(2500, Color.YELLOW, null);
        } else if (mode.equals("Heavy Test")) {
            wrapperController.StressTest(5000, Color.RED, null);
        }
    }


    @FXML
    protected void applyTLsettings() {
        String id = tlSelector.getValue();
        int duration = durationTL.getValue();
        wrapperController.setTlSettings(id, duration);
    }


    // functionality

    /**
     * Called by {@link WrapperController} thread repeatedly to update GUI elements connected to the simulation
     *
     * <p>
     *     <li> {@link #updateTime()} updates time based on sim time. </li>
     *     <li> {@link #updateDelay()} updates delay. </li>
     *     <li> {@link #updateCountVeh()} updates how many cars are spawned. </li>
     *     <li> {@link #updateTLPhaseText()} updates TL menu phase text. </li>
     * </p>
     */
    public void doSimStep() {
        // updates UI elements
        updateTime();
        updateDelay();
        updateCountVeh();
        updateTLPhaseText();
        this.updateDataPane();
    }

    /**
     * Updates selected value, if changed, and calls {@link WrapperController#changeDelay(int)} method to set value.
     */
    public void updateDelay() {
        if (delaySelect.getValue() != wrapperController.getDelay()) {
            wrapperController.changeDelay(delaySelect.getValue());
        }
    }

    private String rawSecondsToHMS(int seconds) {
        StringBuilder sb = new StringBuilder();
        int h = seconds / 3600; // every 3600 ms is one hour
        int m = seconds % 3600 / 60; // minutes 0 to 3599 / 60
        int s =  seconds % 60; // seconds 0 - 59
        sb.append(String.format("%02d:%02d:%02d", h, m, s));
        return sb.toString();
    }
    /**
     * Calculates time provided by {@link WrapperController#getTime()}
     * formats it to: "HH:MM:SS"  and displays it in GUI
     */
    public void updateTime() {
        int time = (int) wrapperController.getTime();
        timeLabel.setText(this.rawSecondsToHMS(time));
    }

    /**
     * Refreshes the data list view (currently placeholder structure).
     */
    public void updateDataPane() {
        Locale.setDefault(Locale.US);
        VehicleList vehicles = wrapperController.getVehicles();
        String currentTab = tabPane.getSelectionModel().getSelectedItem().getText();
        if (currentTab.equals("Overall")) {
            int overallVehicleCount = wrapperController.getAllVehicleCount();
            int activeCount = vehicles.getActiveCount();
            int queuedCount = vehicles.getQueuedCount();
            int exitedCount = overallVehicleCount - activeCount - queuedCount;
            int currentlyStopped = vehicles.getStoppedCount();
            int stoppedTime = vehicles.getStoppedTime();
            float stoppedPercentage = 0f;
            if (activeCount > 0) {
                stoppedPercentage = (currentlyStopped / (float) activeCount) * 100;
            }

            this.activeVehicles.setText(Integer.toString(activeCount));
            this.VehiclesNotOnScreen.setText(Integer.toString(queuedCount));
            this.DepartedVehicles.setText(Integer.toString(exitedCount));
            this.VehiclesCurrentlyStopped.setText(String.format("%d (%.2f%%)", currentlyStopped, stoppedPercentage));
            this.TotalTimeSpentStopped.setText(String.format("%s", this.rawSecondsToHMS(stoppedTime)));
            this.MeanSpeed.setText(String.format("%.2f m/s", vehicles.getMeanSpeed()));
            this.SpeedSD.setText(String.format("%.2f m/s", vehicles.getSpeedStdDev()));

        } else if (currentTab.equals("Selected")) {
            return;
            // if no Object is selected display "Please select a Vehicle using Select Mode" and highlight Select Mode Button
            // Vehicles:
            // ID, Type, Route ID, Color (displayed in color, if possible), max Speed (maximum speed reached), current Speed, average Speed
            // Angle, Acceleration, Deceleration, Total Lifetime, Overall Stop Time, number of Stops
        } else {
            return;
            // Same as Overall, but only taking filtered Vehicles into account, which requires a separate VehicleList...
        }
    }

    /**
     * Updates the vehicle count label.
     * <p>
     * Displays "TotalVehicles / CurrentVehicles".
     * </p>
     */
    private void updateCountVeh() {
        int c = wrapperController.updateCountVehicle(); // updates count everytime a new veh is added
        int all = wrapperController.getAllVehicleCount();
        vehicleCount.setText(all+"/"+c);
    }

    /**
     * Updates the text display in the Traffic Light menu.
     * <p>
     * Shows the current phase string and the remaining time until the next switch
     * for the selected Traffic Light in this format: <br>
     * "Grr, dur: 67/82" <br>
     * State, remaining dur and absolute duration of this state
     *
     * </p>
     */
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
    /**
     * Starts the animation loop called by {@link #initializeCon(WrapperController)}
     * <p>
     * Creates an {@link AnimationTimer} that calls {@link #renderUpdate()} roughly 60 times per second.
     * It also checks every frame if the route list is empty to enable/disable vehicle addition buttons if so.
     * </p>
     */
    public void startRenderer() { // maybe with connection as argument? closing connection opened prior
        renderLoop = new AnimationTimer() { // javafx class -> directly runs on javafx thread
            @Override
            public void handle(long timestamp) {
                renderUpdate();
                checkPerFrame();
            }
        };
        renderLoop.start(); // runs 60 frames per second
    }

    private void checkPerFrame(){
        // Only allow injection if there are routes
        if(wrapperController.isRouteListEmpty()) {
            addVehicleButton.setDisable(true);
            startTestButton.setDisable(true);
        } else {
            addVehicleButton.setDisable(false);
            startTestButton.setDisable(false);
        }
        
        if(addMenu.isVisible() && !(routeSelector.getValue().isEmpty())){
                    String Route = routeSelector.getValue();
                    sr.setPickedRouteID(Route);
        }
        
        if (!dataView.isSelected()) {
            staticMap.widthProperty().bind(middlePane.widthProperty());
            staticMap.heightProperty().bind(middlePane.heightProperty());
            dataPane.setVisible(false);
        } else {
            dataPane.setVisible(true);
            rescale();
        }

    }

    private void stopRenderer() {
        if (renderLoop != null) {
            renderLoop.stop();
            renderLoop = null;
        }
    }

    /**
     * Correctly terminates JavaFX thread and {@link AnimationTimer}
     * and runs {@link WrapperController#terminate()}
     */
    @FXML
    protected void closeApplication() {
        renderLoop.stop(); // terminates Animation Timer
        Platform.exit(); // terminates JavaFX thread, runs "stop" method in GuiAppl
        //wrapperController.terminate(); // terminates sumo connection and wrapCon thread
    }

    /**
     * Initializes the {@link SimulationRenderer}.
     * <p>
     * Obtains the 2D GraphicsContext from the canvas and injects the (necessary) simulation object lists
     * (Junctions, Streets, Vehicles, TLs) into the renderer.
     * </p>
     */
    public void initializeRender(){
        gc = staticMap.getGraphicsContext2D();

        sr = new SimulationRenderer(staticMap,gc,wrapperController.getJunctions(),wrapperController.getStreets(),
                wrapperController.getVehicles(), wrapperController.getTrafficLights(), wrapperController.getRoutes());
        renderUpdate();
    }

    /**
     * Called by {@link #startRenderer()} to update {@link SimulationRenderer#initRender()} ~60 times per frame
     */
    public void renderUpdate(){
        sr.initRender();
    }

    /**
     * Collects data from the "Add Vehicle" menu inputs and calls
     * {@link WrapperController#addVehicle(int, String, String, Color)}
     */
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

    /**
     * Handles scrolling events on the map to zoom in or out and calls
     * {@link SimulationRenderer#zoomMap(double)}
     *
     * @param event the scroll event generated by the mouse wheel.
     */
    @FXML
    protected void zoomMap(ScrollEvent event){

        if (event.getDeltaY() > 0) { // delta y vertical
            sr.zoomMap(1.25);
            //System.out.println("zoom");
        } else  {
            //System.out.println("zoomout");
            sr.zoomMap(0.75);
        }
    }

    /**
     * Sets up mouse listeners for panning the map.
     * <p>
     * Calculates the difference between mouse press and drag coordinates
     * and sends the offset to the renderer via {@link SimulationRenderer#padMad(double, double)}.
     * </p>
     */
    public void mapPan() {
        staticMap.setOnMousePressed(event -> {
            mousePressedXOld = event.getX(); // old
            mousePressedYOld = event.getY();
            //System.out.println("old"+mousePressedXOld + " " + mousePressedYOld);
        });
        // drag start with pressed -> then this follows
        staticMap.setOnMouseDragged(e->{
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

    @FXML
    protected void changeToMap1() {
        changeMap("Frankfurt");
    }

    @FXML
    protected void changeToMap2() {
        changeMap("TestMap");
    }

    private void changeMap(String mapName) {
        // disable buttons -> prevents spamming of switches
        map1select.setDisable(true);
        map2select.setDisable(true);
        wrapperController.mapSwitch(mapName);
    }

    private void importMap() {

    }

}

