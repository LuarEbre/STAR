package sumo.sim;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


import static java.lang.Math.abs;

public class SimulationRenderer {
    private final GraphicsContext gc;
    private final Canvas map;
    private double zoom;

    public SimulationRenderer(Canvas canvas, GraphicsContext gc) {
        this.map = canvas;
        this.gc = gc;
        this.zoom = 4;
    }

    public void initRender(Junction_List jl, Street_List sl){
        // area the size of canvas : frame -> canvas cords.
        // -> network: only do the following rendering with objects inside this restricting area;
        renderJunctions(jl,sl);

    }

    public void renderJunctions(Junction_List jl, Street_List sl){

        double offsetX = abs(jl.getMinPosX())+300; // min position like -230 -> +230 so that it start at 0,0
        double offsetY = abs(jl.getMinPosY())+200; // +300 and +200 just for testing -> displaying in the middle
        gc.setFill(Color.DARKGRAY);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        for (Street s : sl.getStreets()) { // streets
            for (LaneWrap l : s.getLanes()) { // lanes of streets

                double[] rawX = l.getShapeX();
                double[] rawY = l.getShapeY();

                int pointCount = rawX.length;
                double[] screenX = new double[pointCount];
                double[] screenY = new double[pointCount];

                // Transformation
                for (int i = 0; i < pointCount; i++) {
                    screenX[i] = (rawX[i] * zoom) + offsetX;
                    screenY[i] = -(rawY[i] * zoom) + offsetY;
                }

                if (pointCount >= 2) {
                    // if there are at least 2 values in pointCount -> it's a line e.g. : [54.7, 38.75]
                    gc.setLineWidth(12); // should be adjustable
                    gc.strokePolyline(screenX, screenY, pointCount);
                }
            }
        }

        for(JunctionWrap jw : jl.getJunctions()) { // every junction in junction list
            gc.setLineWidth(1);
            double[] rawX = jw.getShapeX();
            double[] rawY = jw.getShapeY();

            int pointCount = rawX.length; // how many entries in array [54.7, 38.75] <- 2
            double[] screenX = new double[pointCount];
            double[] screenY = new double[pointCount];

            // transform
            for (int i = 0; i < pointCount; i++) {
                screenX[i] = (rawX[i] * zoom) + offsetX;
                screenY[i] = -(rawY[i] * zoom) + offsetY; // - to invert
            }

            // draw
            // 1 element in array: dot -> oval
            // [54.7, 38.75] 2 -> line
            // > 3 elements in array : polygon
            if (pointCount >= 3) {
                gc.fillPolygon(screenX, screenY, pointCount); // fills polygon
                gc.strokePolygon(screenX, screenY, pointCount); // border
            } else if (pointCount == 2) {
                /*
                gc.strokeLine(screenX[0], screenY[0], screenX[1], screenY[1]); */
            } else {
                gc.fillOval(screenX[0] - 2, screenY[0] - 2, 4, 4);
            }

        }
    }

}


