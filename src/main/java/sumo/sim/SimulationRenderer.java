package sumo.sim;

import de.tudresden.sumo.cmd.Junction;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;


import static java.lang.Math.abs;

public class SimulationRenderer {
    private final GraphicsContext gc;
    private final Canvas map;
    private double zoom;
    private double camX;
    private double camY;
    private final Junction_List jl;
    private final Street_List sl;

    public SimulationRenderer(Canvas canvas, GraphicsContext gc, Junction_List jl, Street_List sl) {
        this.map = canvas;
        this.gc = gc;
        this.zoom = 0.4;
        this.sl = sl;
        this.jl = jl;
        this.camX = jl.getCenterPosX() ; // center Position is max + min / 2
        this.camY = jl.getCenterPosY() ;
    }

    public void initRender(){
        // area the size of canvas : frame -> canvas cords.
        // -> network: only do the following rendering with objects inside this restricting area;
        gc.setTransform(new Affine()); // transformation matrix

        gc.setFill(Color.LIGHTGREEN);
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
        gc.setLineWidth(1.0);
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
            gc.setLineWidth(1);
            double[] rawX = jw.getShapeX();
            double[] rawY = jw.getShapeY();

            if (rawX == null || rawY == null || rawX.length == 0) continue;

            // draw
            // 1 element in array: dot -> oval
            // [54.7, 38.75] 2 -> line
            // > 3 elements in array : polygon
            if (rawX.length >= 3) {
                gc.fillPolygon(rawX, rawY, rawX.length); // fills polygon
                gc.strokePolygon(rawX, rawY, rawX.length); // border
            } else if (rawX.length == 2) {
                /*
                gc.strokeLine(screenX[0], screenY[0], screenX[1], screenY[1]); */
            } else {
                gc.fillOval(rawX[0] - 2, rawY[0] - 2, 4, 4);
            }

        }
    }

    public void moveX(double pad) {
        camX += pad;
    }

    public void moveY(double pad) {
        camY += pad;
    }

    public void zoomMap(double z) {
        zoom += z;
    }

}


