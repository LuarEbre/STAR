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
import javafx.scene.layout.*;

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
    private AnchorPane dataPane, root, middlePane, addMenu,
            filtersMenuSelect, mapMenuSelect, viewMenuSelect, stressTestMenu, trafficLightMenu, createMenu;
    @FXML
    private ColorPicker colorSelector;
    @FXML
    private VBox fileMenuSelect;
    @FXML
    private ToggleButton playButton, selectButton, addButton, stressTestButton, trafficLightButton, createButton,
            fileMenuButton, mapsMenuButton, filterMenuButton, viewMenuButton;
    @FXML
    private Button stepButton, addVehicleButton, amountMinus, amountPlus, startTestButton, map1select, map2select, importMapButton;

    private ButtonBase[] allButtons;

    @FXML
    private Spinner <Integer> delaySelect, durationTL;
    @FXML
    private Canvas staticMap, dynamicMap;
    @FXML
    private Label timeLabel, vehicleCount, notSelectedLabel1, notSelectedLabel2;
    @FXML
    private Slider playSlider;
    @FXML
    private ListView<String> listData; // list displaying data as a string
    @FXML
    private ChoiceBox<String> typeSelector, routeSelector, stressTestMode, tlSelector, importMapSelector;
    @FXML
    private CheckBox buttonView, dataView , showDensityAnchor, showButtons, showRouteHighlighting, showTrafficLightIDs, densityHeatmap;
    @FXML
    private TextField amountField, stateText, activeVehicles, VehiclesNotOnScreen, DepartedVehicles, VehiclesCurrentlyStopped, TotalTimeSpentStopped, MeanSpeed, SpeedSD,
                        vehicleID, vehicleType, route, color, currentSpeed, averageSpeed, peakSpeed, acceleration, position, angle, totalLifetime, timeSpentStopped, Stops;
    @FXML
    private TabPane tabPane;
    @FXML
    private GridPane FilteredGrid, SelectedGrid;
    @FXML
    private HBox mainButtonBox;

    private GraphicsContext gc;
    private SimulationRenderer sr;
    private AnimationTimer renderLoop;
    private Stage stage;

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
    private SumoMapManager mapManager;
    private int opacity;

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
        this.opacity = 0;
    }

    public void setStageAndManager(Stage s , SumoMapManager mapManager) {
        stage = s;
        this.mapManager = mapManager;
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
        setupSelectionHandler(); // setup SelectMode MouseEvent
        // set initial colorSelector color to magenta to match our UI
        colorSelector.setValue(Color.MAGENTA);

        // if no routes exist in .rou files -> cant add vehicles, checked each frame in startrenderer
        startTestButton.setDisable(true);
        addVehicleButton.setDisable(true);
        SelectedGrid.setVisible(false);
        SelectedGrid.setManaged(false);
        FilteredGrid.setVisible(false);
        FilteredGrid.setManaged(false);

        importMapSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                changeToImportedMap();
            }
        });
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

    private void disableAllTopMenuButtons() {

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
            sr.setSelectMode(true);
            this.playButton.setDisable(true);
            this.playButton.setSelected(false);
            this.stepButton.setDisable(true);
            wrapperController.stopSim();
            System.out.println("Select Mode enabled");
        } else {
            sr.setSelectMode(false);
            this.playButton.setDisable(false);
            this.stepButton.setDisable(false);
            System.out.println("Select Mode disabled");
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

    @FXML
    private void onCreate() {
        toggleMenuAtButton(createMenu, createButton);
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

    private void topMenuButtonToggle(ToggleButton button, AnchorPane menu) {
        menu.setVisible(button.isSelected());
    }

    private void topMenuButtonToggle(ToggleButton button, VBox menu) {
        menu.setVisible(button.isSelected());
    }
    /**
     * Is triggered when user hovers over "filter" button
     *
     * <p>
     *     Calls {@link #closeAllMenus()} method to ensure that no menus are stacked on top of each other and
     *     sets menu to "visible"
     * </p>
     */
    @FXML
    protected void onFiltersPressed(){
        closeAllMenus();
        topMenuButtonToggle(filterMenuButton, filtersMenuSelect);
    }


    /**
     * Equivalent to {@link #onFiltersPressed()}
     */
    @FXML
    protected void onMapsPressed(){
        // deactivate all menus
        closeAllMenus();
        topMenuButtonToggle(mapsMenuButton, mapMenuSelect);
    }

    /**
     * Equivalent to {@link #onFiltersPressed()}
     */
    @FXML
    protected void onViewPressed(){
        closeAllMenus();
        topMenuButtonToggle(viewMenuButton, viewMenuSelect);
    }

    /**
     * Equivalent to {@link #onFiltersPressed()}
     */
    @FXML
    protected void onFilePressed(){
        closeAllMenus();
        topMenuButtonToggle(fileMenuButton, fileMenuSelect);
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
    protected void onMiddlePaneClicked(){
        closeAllMenus();
    }

    @FXML
    protected void onDensityHeatmapToggle(){
        if(densityHeatmap.isSelected()) {
            sr.setViewDensityOn(true);
        }else{
            sr.setViewDensityOn(false);
        }
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

    private void highlightToggleButton(ToggleButton tb) {
        this.opacity = (this.opacity + 10) % 100;
        Border newBorder = tb.getBorder();
        CornerRadii myRadii = tb.getBackground().getFills().get(0).getRadii();
        Border border = new Border(new BorderStroke(
                Color.hsb(304, 0.89, 1.0, this.opacity / 100.0),
                BorderStrokeStyle.SOLID,
                myRadii,
                new BorderWidths(2)
        ));
        tb.setBorder(border);
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
            this.TotalTimeSpentStopped.setText(this.rawSecondsToHMS(stoppedTime));
            this.MeanSpeed.setText(String.format("%.2f m/s", vehicles.getMeanSpeed()));
            this.SpeedSD.setText(String.format("%.2f m/s", vehicles.getSpeedStdDev()));

        } else if (currentTab.equals("Selected")) {
            SelectableObject selectedObject = wrapperController.getSelectedObject();
            if(selectedObject != null) {
                // if selected object is a Vehicle, the GridPane for Traffic Lights needs to be set !visible & !managed and vice versa (SelectedGridTL does not exist yet)
                if (selectedObject instanceof VehicleWrap v) {

                    SelectedGrid.setVisible(true);
                    SelectedGrid.setManaged(true);
                    // SelectedGridTL.setVisible(false);
                    // SelectedGridTL.setManaged(false);

                    this.vehicleID.setText(v.getID());
                    this.vehicleType.setText(v.getType());
                    this.route.setText(v.getRouteID());
                    String hex = String.format("#%02X%02X%02X",
                            (int)(v.getColor().getRed() * 255),
                            (int)(v.getColor().getGreen() * 255),
                            (int)(v.getColor().getBlue() * 255));
                    this.color.setStyle("-fx-background-color: " + hex + ";");
                    this.currentSpeed.setText(String.format("%.2f m/s",v.getSpeed()));
                    this.averageSpeed.setText(String.format("%.2f m/s",v.getAvgSpeed()));
                    this.peakSpeed.setText(String.format("%.2f m/s",v.getMaxSpeed()));
                    double accel = v.getAccel();
                    if(accel < 0) hex = "#C14E4E";
                    else hex = "#089622";
                    this.acceleration.setStyle("-fx-text-fill: " + hex + ";");
                    this.acceleration.setText(String.format("%.2f m/s²",v.getAccel()));
                    this.position.setText(String.format("%.2f | %.2f",v.getPosition().x, v.getPosition().y));
                    this.angle.setText(String.format("%.2f°",v.getAngle()));
                    this.totalLifetime.setText(this.rawSecondsToHMS(v.getTotalLifetime()));
                    this.timeSpentStopped.setText(this.rawSecondsToHMS(v.getWaitingTime()));
                    this.Stops.setText(Integer.toString(v.getNumberOfStops()));

                } else if (selectedObject instanceof TrafficLightWrap tl) {

                    // SelectedGridTL.setVisible(true);
                    // SelectedGridTL.setManaged(true);
                    SelectedGrid.setVisible(false);
                    SelectedGrid.setManaged(false);

                    // open TrafficLight Menu
                    trafficLightButton.setSelected(true);
                    onTrafficLight();
                    // TO DO: Make Traffic Light Object show up in Traffic Light Menu Dropdown Menu, display Traffic Light Stats in a new GridPane with similar structure
                }
            }
        } else {
            // EXPERIMENTAL - this.highlightToggleButton(filterMenuButton);
            // set visible and managed true, only after checking whether filter has been applied
            // FilteredGrid.setVisible(true);
            // FilteredGrid.setManaged(true);
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

    private SelectableObject findClickableObject(double worldX, double worldY) {
        for(VehicleWrap v : wrapperController.getVehicles().getVehicles()) {
            var pos = v.getPosition();
            int radius = v.getSelectRadius();
            double minX = pos.x - radius;
            double maxX = pos.x + radius;
            double minY = pos.y - radius;
            double maxY = pos.y + radius;
            if(worldX <= maxX &&  worldX >= minX && worldY <= maxY && worldY >= minY) {
                return v;
            }
        }
        for(TrafficLightWrap tl : wrapperController.getTrafficLights().getTrafficlights()) {
            var pos = tl.getPosition();
            int radius = tl.getSelectRadius();
            double minX = pos.x - radius;
            double maxX = pos.x + radius;
            double minY = pos.y - radius;
            double maxY = pos.y + radius;
            if(worldX <= maxX &&  worldX >= minX && worldY <= maxY && worldY >= minY) {
                return tl;
            }
        }
        return null;
    }

    public void setupSelectionHandler() {
        staticMap.setOnMouseClicked(event -> {
            if (sr.getSelectMode()) {
                double mouseX = event.getX();
                double mouseY = event.getY();
                java.awt.geom.Point2D.Double pos = sr.screenToWorld(mouseX, mouseY);
                try {
                    SelectableObject so = this.findClickableObject(pos.x, pos.y);
                    if(so != null) {
                        // deselect all other selectableElements first
                        wrapperController.getVehicles().deselectAll();
                        wrapperController.getTrafficLights().deselectAll();
                        // select Object returned by findClickableObject
                        so.select();
                        notSelectedLabel1.setVisible(false);
                        notSelectedLabel2.setVisible(false);
                        // switch over to "Selected" Tab
                        tabPane.getSelectionModel().select(1);
                        // switch off Select Mode button
                        selectButton.setSelected(false);
                        this.onSelect();
                        this.updateDataPane();
                    }
                } catch (NullPointerException e) {
                   System.err.println(e);
                }
            }
        });
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

    protected void reset(){

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
     * {@link SimulationRenderer
     *
     * @param event the scroll event generated by the mouse wheel.
     */
    @FXML
    protected void zoomMap(ScrollEvent event){

        if (event.getDeltaY() > 0) { // delta y vertical
            sr.zoomMapIn(1.25);
            //System.out.println("zoom");
        } else  {
            //System.out.println("zoomout");
            sr.zoomMapOut(0.75);
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
            closeAllMenus(); // closes all top menus when panning
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
        changeMap("RugMap");
    }

    @FXML
    protected void changeToImportedMap() {
        changeMap(importMapSelector.getValue());
    }

    private void changeMap(String mapName) {
        // disable buttons -> prevents spamming of switches
        map1select.setDisable(true);
        map2select.setDisable(true);
        if (mapName != null) {
            wrapperController.mapSwitch(mapName);
        }
        importMapSelector.getSelectionModel().clearSelection(); // resets previous selection
    }

    private void createType() {
        // all possible choices -> if no entry : empty in xml
    }

    @FXML
    private void importMap() {
        mapManager.chooseFile(stage);
        updateImportedMaps();
    }

    private void updateImportedMaps() {
        importMapSelector.setItems(FXCollections.observableArrayList(mapManager.getAllImportedMaps()));
    }

    @FXML
    private void addRoute() {
        // needs argument: start and end junction id
        // J0, J11
        wrapperController.addRoute("J0", "J1", "testID");
    }

}

