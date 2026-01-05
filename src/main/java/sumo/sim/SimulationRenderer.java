package sumo.sim;

import de.tudresden.sumo.cmd.Junction;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Paint;

/**
 * Handles the graphical rendering of the SUMO simulation onto a JavaFX {@link Canvas}.
 * <p>
 * This class manages the 2D camera transformation (panning, zooming, coordinate flipping),
 * and draws the static network (streets, junctions) as well as dynamic objects
 * (vehicles, traffic lights) frame by frame.
 * </p>
 */
public class SimulationRenderer {
    private final GraphicsContext gc;
    private final Canvas map;
    boolean showTrafficLightIDs;
    boolean showSelectablePoints;
    private double zoom;
    private double camX;
    private double camY;
    private double scale; // should depend on how big the map is -> difference between max and min?
    private double rotation;
    private final JunctionList jl;
    private final StreetList sl;
    private final VehicleList vl;
    private final TrafficLightList tls;
    private final Font tlFont;

    private double viewMinX;
    private double viewMaxX;
    private double viewMinY;
    private double viewMaxY;

    /**
     * Constructs a new SimulationRenderer called by {@link GuiController}
     * <p>
     * Initializes the camera position to the center of the network and calculates
     * an initial scale factor to fit the network bounds within the canvas.
     * </p>
     *
     * Arguments passed on by {@link GuiController}: <br>
     *
     * @param canvas The JavaFX Canvas to draw on
     * @param gc     The GraphicsContext associated with the canvas. Which controls its content
     * @param jl     The list of junctions to render.
     * @param sl     The list of streets to render -> to get its associated lanes
     * @param vl     The list of vehicles to render.
     * @param tls    The list of traffic lights to render.
     * <br>
     * <p>
     *      Initializes scale, zoom and rotation as well as CamX/Y which is centered by {@link JunctionList#getCenterPosX()}
     *
     * </p>
     */
    public SimulationRenderer(Canvas canvas, GraphicsContext gc, JunctionList jl, StreetList sl, VehicleList vl, TrafficLightList tls) {
        this.showTrafficLightIDs = false;
        this.tlFont = new Font("Arial", 7);
        this.map = canvas;
        this.gc = gc; // for drawing on canvas
        this.sl = sl;
        this.jl = jl;
        this.vl = vl;
        this.tls = tls;
        this.camX = jl.getCenterPosX(); // center Position is max + min / 2
        this.camY = jl.getCenterPosY();
        double scaleX = (jl.getMaxPosX() - jl.getMinPosX()); // e.g : max 3, min -3 -> 3 -- 3 = 6 -> difference
        double scaleY = (jl.getMaxPosY() - jl.getMinPosY());
        this.scale = 1 + (scaleX / scaleY); // should calculate the rough scale of the map
        this.zoom = scale + 1;
        this.rotation = 0;
        for (Street s: sl.getStreets()) {
            s.calculateBounds();
        }
        for (JunctionWrap j: jl.getJunctions()) {
            j.calculateBounds();
        }
        //scale = 1;
    }

    /**
     * Main rendering method. Clears the canvas, applies transformations with {@link #transform()}, and triggers
     * the drawing of map layers {@link #renderTrafficLight()} {@link #renderMap()} {@link #renderVehicle()}  }
     * <p>
     * This method is called by {@link GuiController#renderUpdate()} method ~60 times per second
     * </p>
     */
    public void initRender() {
        updateViewportBounds();
        // area the size of canvas : frame -> canvas cords.
        // -> network: only do the following rendering with objects inside this restricting area;
        gc.setTransform(new Affine()); // transformation matrix, clears canvas

        gc.setFill(Paint.valueOf("#86858E")); // background color
        gc.fillRect(0, 0, map.getWidth(), map.getHeight()); // covers whole screen (edge detection)

        transform();
        renderMap();
    }

    // [ mxx , mxy , tx ]
    // [ myx , myy , ty ]
    // [  0  ,  0  ,  1 ]
    // m matrix , t translate, first letter: target; second letter: source ( which to mult)

    /**
     * <p>
     *     Applies a JavaFX transformation matrix (Affine) to the GraphicsContext via setTransform.
     *  <li> Translation: Moves by x and y value (only possible with homogenous matrix) </li>
     *  <li> Rotation: Rotates with sin and cos </li>
     *  <li> Scale: Scales bigger or smaller </li>
     * </p>
     * <br>
     * <p>
     * Steps:
     * <li> Translate to center of canvas based on map (width,height) / 2 .</li>
     * <li>Apply rotation based on value (unused). </li>
     * <li>Apply zoom/scale (Note: Y-axis is flipped with {@code -zoom} because SUMO
     * and JavaFX y-coordinates are reversed. </li>
     * <li> Translate back by camera position (camX, camY). </li>
     * </p>
     */
    private void transform() {
        Affine transform = new Affine();
        transform.appendTranslation(map.getWidth() / 2, map.getHeight() / 2); // moves 0,0 to map middle : add/sub
        // [ 1 , 0 , width ]        [ x + w ] <-- this is our point x -> + is to the right on x
        // [ 0 , 1 , height ]  *    [ y+h ]  <-- this is our point y
        // [ 0 , 0 , 1 ]            [ 1 ] <-- homogeneuos (added 1 row )
        transform.appendRotation(rotation);
        transform.appendScale(zoom, -zoom); // - y because sumo y coords are reversed : mul / div
        // [ xSc , 0 , 0 ]        [ x * xSc ]  Scales our point with xSc and ySc
        // [ 0 , ySC , 0 ]  *    [ y * ySc ]
        // [ 0 , 0 , 1 ]            [ 1 ]
        transform.appendTranslation(-camX, -camY); // centralizes our view
        gc.setTransform(transform); // applies new matrix to gc matrix

    }

    /**
     * Renders the static map elements (streets and junctions) and triggers rendering
     * of dynamic elements (Vehicle / TL)
     *
     * <p>
     *     Performs rendering by parsing raw shapes of objects onto gc via the given lists
     * </p>
     */
    private void renderMap() {
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(scale);

        for (Street s : sl.getStreets()) { // streets
            // stroke Polyline for lanes
            if (s.getMaxX() < viewMinX || s.getMinX() > viewMaxX
                    || s.getMaxY() < viewMinY || s.getMinY() > viewMaxY) continue;
            int countLanes = s.getLanes().size();
            int laneIndex = 0;
            double[] meanLaneX = null;
            double[] meanLaneY = null;

            for (LaneWrap l : s.getLanes()) { // lanes of streets

                double[] rawX = l.getShapeX();
                double[] rawY = l.getShapeY();
                // needs checking -> error preventing
                if (rawX == null || rawY == null || rawX.length == 0) continue;

                if (meanLaneX == null) {
                    // only the first time if there are no lanes for this street saved yet
                    meanLaneX = new double[rawX.length];
                    meanLaneY = new double[rawY.length];
                }

                int limit = Math.min(meanLaneX.length, rawX.length); // out of bounce check -> if not the same size

                for (int i = 0; i < limit; i++) {
                    meanLaneX[i] += rawX[i];
                    meanLaneY[i] += rawY[i];
                    // sums up all point for mean calculation later
                }

                // Draws Lanes
                if (rawX.length >= 2) {
                    // if there are at least 2 values in pointCount -> it's a line e.g. : [54.7, 38.75]
                    gc.setFill(Color.BLACK);
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(5); // should be adjustable
                    gc.setLineDashes(null);
                    gc.strokePolyline(rawX, rawY, rawX.length);
                }
            }

            // test, only works if lanesCount == 2 , if odd -> cant place line in the middle, if even: needs offset
            // Draws lane lines
            if (meanLaneX != null) {
                for (int i = 0; i < meanLaneX.length; i++) {
                    // mean calculation / countLanes -> gets a line exactly in the middle of the road
                    meanLaneX[i] /= countLanes;
                    meanLaneY[i] /= countLanes;
                }

                gc.setStroke(Color.WHITE);
                gc.setLineWidth(0.25);
                gc.setLineDashes(10d, 10d);

                // experimental , needs fixing
                if (countLanes == 2) {
                    gc.strokePolyline(meanLaneX, meanLaneY, meanLaneX.length); // middle line
                } else if (countLanes % 2 == 0) {

                } else if (countLanes % 2 == 1) {

                }
            }
        }

        for (JunctionWrap jw : jl.getJunctions()) { // every junction in junction list
            if (jw.getMaxX() < viewMinX || jw.getMinX() > viewMaxX
                    || jw.getMaxY() < viewMinY || jw.getMinY() > viewMaxY) continue;
            gc.setFill(Color.BLACK);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(scale);
            double[] rawX = jw.getShapeX();
            double[] rawY = jw.getShapeY();

            if (rawX == null || rawY == null || rawX.length == 0) continue;

            // draw
            // 1 element in array: dot -> oval
            // [54.7, 38.75] 2 -> line
            // > 3 elements in array : polygon
            if (rawX.length >= 3) {
                gc.fillPolygon(rawX, rawY, rawX.length); // fills polygon
                //gc.strokePolygon(rawX, rawY, rawX.length); // border
            } else if (rawX.length == 2) {
                //gc.strokeLine(screenX[0], screenY[0], screenX[1], screenY[1]);
            } else {
                gc.fillOval(rawX[0] - 2, rawY[0] - 2, 4, 4);
            }

        }
        renderVehicle();
        renderTrafficLight();
        if (showTrafficLightIDs) displayTrafficLights();
    }

    private void updateViewportBounds() {
        double viewWidthWorld = map.getWidth() / zoom;
        double viewHeightWorld = map.getHeight() / zoom;

        this.viewMinX = camX - (viewWidthWorld / 2);
        this.viewMaxX = camX + (viewWidthWorld / 2);
        this.viewMinY = camY - (viewHeightWorld / 2);
        this.viewMaxY = camY + (viewHeightWorld / 2);
    }

    protected void setShowTrafficLightIDs(boolean showTrafficLightIDs) {
        this.showTrafficLightIDs = showTrafficLightIDs;
    }

    protected boolean getShowTrafficLightIDs() {
        return showTrafficLightIDs;
    }

    protected void setSelectablePoints(boolean p) {
        this.showSelectablePoints = p;
    }

    protected boolean getSelectablePoints() {
        return showSelectablePoints;
    }

    /**
     * Renders text labels for Traffic Light IDs at their respective positions.
     * <p>
     * This method handles the coordinate flip locally to ensure text renders
     * "right-side up" despite the global flip in {@link #transform()}.
     * </p>
     */
    protected void displayTrafficLights() {
        // text adjustments (color, alignment, font)
        gc.setFill(Color.rgb(241, 241, 241));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(tlFont);

        for (TrafficLightWrap tl : tls.getTrafficlights()) {
            // save and restore context, as each traffic light gets a unique translation
            gc.save();

            // using TL position as "offset" so we can later render at translated (0 | 0)
            double newX = tl.getPosition().getX();
            double newY = tl.getPosition().getY();
            String id = tl.getId();

            // translate using position
            gc.translate(newX, newY);
            // flip Y-axis so text renders right side up
            gc.scale(1, -1);

            gc.fillText(id, 0, 0);
            // restore previously saved GraphicsContext
            gc.restore();
        }
    }

    private void displaySelectablePoints() {

    }

    /**
     * Iterates {@link VehicleList} and calls {@link #drawTriangleCar(VehicleWrap, double, double)} for every vehicle (still on the map)
     */
    private void renderVehicle() {
        for (VehicleWrap v : vl.getVehicles()) {
            if (!v.exists() && v.getPosition() == null) continue;
            // no need to translate coordinates since translation is already applied to graphics context
            if (v.getPosition().getX() <= viewMinX || v.getPosition().getX() >= viewMaxX
             || v.getPosition().getY() <= viewMinY || v.getPosition().getY() >= viewMaxY) continue;
            this.drawTriangleCar(v, 1.5, 3); // ? set length / width in vehicle class -> internal
        }
    }

    /**
     * Helper method to draw a single vehicle as a triangle.
     * Saves gc state and applies new state for rendering vehicle, restores gc state after wards
     *
     * @param v      The vehicle wrapper object.
     * @param width  Half-width of the vehicle base.
     * @param length Distance from center to front/back.
     */
    private void drawTriangleCar(VehicleWrap v, double width, double length) {
        if (v.exists()) {
            gc.save(); // saves previous gc state
            gc.translate(v.getPosition().getX(), v.getPosition().getY()); // new offset
            gc.rotate(-v.getAngle() + 180); // mirror along x -> rotate 180 degree
            gc.setFill(v.getColor());
            double[] xPoints = {0, -width, width}; // width relative to start point 0 , 0
            double[] yPoints = {-length, length, length}; // set 3 Polygon point relative to car position
            gc.fillPolygon(xPoints, yPoints, 3); // 3 ->  length

            gc.restore(); // restores previous
        }
    }

    // optimization necessary

    /**
     * Renders the status of traffic lights by drawing colored lines at the end of controlled lanes.
     * <p>
     * It matches the current light state string (e.g., "GrGr") to the controlled lanes
     * by checking if the controlled lanes are equivalent to {@link LaneWrap} ids. If they are, rendering is performed
     * and a line is drawn at the end of the lane with the corresponding color using {@link TrafficLightWrap#getCurrentState()}
     * </p>
     */
    private void renderTrafficLight() {
        for (TrafficLightWrap tl : tls.getTrafficlights()) {
            //tl.setCurrentState();
            String[] state = tl.getCurrentState(); // [R, lane_R ,y , lane_y , r, lane_r ] format
            if (state == null) continue; // protection

            Color lightColor;
            gc.setLineWidth(2.0);

            for (Street controlledStreet : tl.getControlledStreets()) {
                for (LaneWrap l : controlledStreet.getLanes()) { // lanes of streets , maybe performance hashmap
                    for (int j = 0; j < state.length; j += 2) {
                        if (state[j + 1] == null) continue;
                        if (state[j + 1].equals(l.getLaneID())) { //  maybe performance hashmap
                            switch (state[j]) { // if state like "g" equals...
                                case "G", "g" -> lightColor = Color.GREEN;
                                case "y" -> lightColor = Color.YELLOW;
                                case "r" -> lightColor = Color.RED;
                                default -> lightColor = Color.GRAY;
                            }
                            gc.setStroke(lightColor);
                            break; // lane found
                        }
                    }

                    double[] rawX = l.getShapeX();
                    double[] rawY = l.getShapeY();

                    double endX = rawX[rawX.length - 1]; // 3
                    double endY = rawY[rawX.length - 1]; // 2

                    double prevX = rawX[rawX.length - 2]; // 2
                    double prevY = rawY[rawX.length - 2]; // 1

                    //(P_prev -> P_end) direction vector (target-start)
                    double dx = endX - prevX; // 1
                    double dy = endY - prevY; // 1

                    // normalize length to 1 , because value can be really high
                    double length = Math.sqrt(dx * dx + dy * dy); // 1+1=2

                    double ndx = dx / length;
                    double ndy = dy / length; // 0.5
                    // length of vector is now exactly 1 , can change size here?

                    double perpX = -ndy; // rotates 90 degree to the left when negating x and switching x and y
                    double perpY = ndx;

                    double halfWidth = 2.0 / 2.0; // pre-determined -> should adjust with lane width

                    double lineX1 = endX + (perpX * halfWidth); // line X2/Y2 <---"-"-*-"+"---> lineX1/Y1 , (* = endpoint)
                    double lineY1 = endY + (perpY * halfWidth);

                    double lineX2 = endX - (perpX * halfWidth);
                    double lineY2 = endY - (perpY * halfWidth);

                    gc.strokeLine(lineX1, lineY1, lineX2, lineY2);
                }
            }

        }
    }

    /**
     * Is called by {@link GuiController#mapPan()}
     * @param x
     * @param y
     */
    public void padMad(double x, double y) {
        camX += x / (zoom / 2); // zoom / 2 -> if zoomed out -> x gets bigger
        camY += y / (zoom / 2);
    }

    /**
     * Is called by {@link GuiController#zoomMap(ScrollEvent)}
     * @param z
     */
    public void zoomMap(double z) {
        // should have a zoom min and max cap based on map scale
        zoom *= z; // zoom with values > 1 , // unzoom with val < 1
    }
}


