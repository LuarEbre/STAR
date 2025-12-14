package sumo.sim;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Paint;

public class SimulationRenderer {
    private final GraphicsContext gc;
    private final Canvas map;
    boolean showTrafficLightIDs;
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
        //scale = 1;
    }

    public void initRender() {
        // area the size of canvas : frame -> canvas cords.
        // -> network: only do the following rendering with objects inside this restricting area;
        gc.setTransform(new Affine()); // transformation matrix

        gc.setFill(Paint.valueOf("#86858E")); // background color
        gc.fillRect(0, 0, map.getWidth(), map.getHeight()); // covers whole screen (edge detection)
        transform();
        renderMap();
    }

    // [ mxx , mxy , tx ]
    // [ myx , myy , ty ]
    // [  0  ,  0  ,  1 ]
    // m matrix , t translate, first letter: target; second letter: source ( which to mult)

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

    private void renderMap() {

        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(scale);

        for (Street s : sl.getStreets()) { // streets
            // stroke Polyline for lanes
            for (LaneWrap l : s.getLanes()) { // lanes of streets

                double[] rawX = l.getShapeX();
                double[] rawY = l.getShapeY();

                // needs checking -> error preventing
                if (rawX == null || rawY == null || rawX.length == 0) continue;
                // continue -> if true -> skip everything and move to the next object of the loop
                // if arrays are null or empty skip

                if (rawX.length >= 2) {
                    // if there are at least 2 values in pointCount -> it's a line e.g. : [54.7, 38.75]
                    gc.setLineWidth(5); // should be adjustable
                    gc.strokePolyline(rawX, rawY, rawX.length);
                }
            }
        }

        for (JunctionWrap jw : jl.getJunctions()) { // every junction in junction list
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

    protected void setShowTrafficLightIDs(boolean showTrafficLightIDs) {
        this.showTrafficLightIDs = showTrafficLightIDs;
    }

    protected boolean getShowTrafficLightIDs() {
        return showTrafficLightIDs;
    }

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

    private void renderVehicle() {
        for (VehicleWrap v : vl.getVehicles()) {
            if (!v.exists() && v.getPosition() == null) continue;
            // no need to translate coordinates since translation is already applied to graphics context
            this.drawTriangleCar(v, 1.5, 3); // ? set length / width in vehicle class -> internal
        }
    }

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

    private void renderTrafficLight() {
        for (TrafficLightWrap tl : tls.getTrafficlights()) {
            //tl.setCurrentState();
            String[] state = tl.getCurrentState(); // [R, edge_R ,y , edge_y , r, edge_r ] format
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

                    double endX = rawX[rawX.length - 1];
                    double endY = rawY[rawX.length - 1];

                    double prevX = rawX[rawX.length - 2];
                    double prevY = rawY[rawX.length - 2];

                    //(P_prev -> P_end)
                    double dx = endX - prevX;
                    double dy = endY - prevY;

                    // normalize length
                    double length = Math.sqrt(dx * dx + dy * dy);

                    double ndx = dx / length;
                    double ndy = dy / length;

                    double perpX = -ndy;
                    double perpY = ndx;

                    double halfWidth = 3.0 / 2.0;

                    double lineX1 = endX + (perpX * halfWidth);
                    double lineY1 = endY + (perpY * halfWidth);

                    double lineX2 = endX - (perpX * halfWidth);
                    double lineY2 = endY - (perpY * halfWidth);

                    gc.strokeLine(lineX1, lineY1, lineX2, lineY2);
                }
            }

        }
    }

    public void padMad(double x, double y) {
        camX += x / (zoom / 2); // zoom / 2 -> if zoomed out -> x gets bigger
        camY += y / (zoom / 2);
    }

    public void zoomMap(double z) {
        zoom *= z; // zoom with values > 1 , // unzoom with val < 1
    }
}


