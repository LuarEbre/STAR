package sumo.sim;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.awt.geom.Point2D;

import static java.lang.Math.abs;

public class SimulationRenderer {
    private final GraphicsContext gc;
    private final Canvas map;
    private double zoom;
    private double camX;
    private double camY;
    private double scale; // should depend on how big the map is -> difference between max and min?
    private final Junction_List jl;
    private final Street_List sl;
    private final Vehicle_List vl;
    private final TrafficLights_List tls;

    public SimulationRenderer(Canvas canvas, GraphicsContext gc, Junction_List jl, Street_List sl, Vehicle_List vl, TrafficLights_List tls) {
        this.map = canvas;
        this.gc = gc; // for drawing on canvas
        this.sl = sl;
        this.jl = jl;
        this.vl = vl;
        this.tls = tls;
        this.camX = jl.getCenterPosX() ; // center Position is max + min / 2
        this.camY = jl.getCenterPosY() ;
        double scaleX = (jl.getMaxPosX() - jl.getMinPosX()); // e.g : max 3, min -3 -> 3 -- 3 = 6 -> difference
        double scaleY = (jl.getMaxPosY() - jl.getMinPosY());
        scale = 1+(scaleX / scaleY);
        System.out.println("scale: " + scale);
        zoom = scale+1;
        //scale = 1;
    }

    public void initRender(){
        // area the size of canvas : frame -> canvas cords.
        // -> network: only do the following rendering with objects inside this restricting area;
        gc.setTransform(new Affine()); // transformation matrix

        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, map.getWidth(), map.getHeight()); // covers whole screen (edge detection)
        transform();
        renderMap(jl,sl);
    }


    // [ mxx , mxy , tx ]
    // [ myx , myy , ty ]
    // [  0  ,  0  ,  1 ]
    // m matrix , t translate, first letter: target; second letter: source ( which to mult)


    public void transform(){
        Affine transform = new Affine();
        transform.appendTranslation(map.getWidth()/2, map.getHeight()/2); // moves 0,0 to map middle : add/sub
        // [ 1 , 0 , width ]        [ x + w ] <-- this is our point x -> + is to the right on x
        // [ 0 , 1 , height ]  *    [ y+h ]  <-- this is our point y
        // [ 0 , 0 , 1 ]            [ 1 ] <-- homogeneuos (added 1 row )

        transform.appendScale(zoom,-zoom); // - y because sumo y coords are reversed : mul / div
        // [ xSc , 0 , 0 ]        [ x * xSc ]  Scales our point with xSc and ySc
        // [ 0 , ySC , 0 ]  *    [ y * ySc ]
        // [ 0 , 0 , 1 ]            [ 1 ]
        transform.appendTranslation(-camX, -camY); // centralizes our view
        gc.setTransform(transform); // applies new matrix to gc matrix

    }


    public void renderMap(Junction_List jl, Street_List sl){

        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(scale);
        for (Street s : sl.getStreets()) { // streets
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

        for(JunctionWrap jw : jl.getJunctions()) { // every junction in junction list
            gc.setFill(Color.RED);
            gc.setStroke(Color.RED);
            gc.setLineWidth(scale);
            double[] rawX = jw.getShapeX();
            double[] rawY = jw.getShapeY();

            if (rawX == null || rawY == null || rawX.length == 0) continue;

            // draw
            // 1 element in array: dot -> oval
            // [54.7, 38.75] 2 -> line
            // > 3 elements in array : polygon
            if (rawX.length >= 3) {
                gc.fillPolygon(rawX, rawY, rawX.length ); // fills polygon
                //gc.strokePolygon(rawX, rawY, rawX.length); // border
            } else if (rawX.length == 2) {
                //gc.strokeLine(screenX[0], screenY[0], screenX[1], screenY[1]);
            } else {
               gc.fillOval(rawX[0] - 2, rawY[0] - 2, 4, 4);
            }

        }
        renderVehicle();
    }

    public void drawTriangleCar(VehicleWrap v, double width, double length) {
        double angle = v.getAngle();
        double posX = v.getPosition().getX();
        double posY = v.getPosition().getY();
        gc.setFill(v.getColor());

        double radTip   = Math.toRadians(angle - 90);
        double radLeft  = Math.toRadians(angle - 90 - 90);
        double radRight = Math.toRadians(angle - 90 + 90);

        double x1 = posX + length * Math.cos(radTip);
        double y1 = posY + length * Math.sin(radTip);

        double x2 = posX + width * Math.cos(radLeft);
        double y2 = posY + width * Math.sin(radLeft);

        double x3 = posX + width * Math.cos(radRight);
        double y3 = posY + width * Math.sin(radRight);

        double[] xPoints = { x1, x2, x3, x1 };
        double[] yPoints = { y1, y2, y3, y1 };

        gc.fillPolygon(xPoints, yPoints, 4);
    }

    public void renderVehicle(){
        double angle = 0;
        double posX;
        double posY;

        for (VehicleWrap v : vl.getVehicles()) {
            if(!v.exists()) continue;
            gc.setFill(v.getColor());
            angle = v.getAngle();
            posX = v.getPosition().getX();
            posY = v.getPosition().getY();
            // no need to translate coordinates since translation is already applied to graphics context
            gc.fillOval(posX-2, posY-2, 4, 4); // for now drawing an oval, could be either a svg or other polygon in the future
            // drawTriangleCar is still experimental as the angles are not accurate when taking turns etc.
            // this.drawTriangleCar(v,2, 8);

        }
    }

    public void padMad(double x, double y) {
        camX += x/(zoom/2); // zoom / 2 -> if zoomed out -> x gets bigger
        camY += y/(zoom/2);
    }

    public void zoomMap(double z) {
        zoom *= z; // zoom with values > 1 , // unzoom with val < 1
    }



}


