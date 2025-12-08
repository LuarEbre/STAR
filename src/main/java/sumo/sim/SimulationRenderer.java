package sumo.sim;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.cmd.Simulation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Set;

import static java.lang.Math.abs;

public class SimulationRenderer {
    private GraphicsContext gc;
    private Canvas map;
    private double zoom = 2;

    public SimulationRenderer(Canvas canvas, GraphicsContext gc) {
        this.map = canvas;
        this.gc = gc;
    }

    public void initRender(Junction_List jl){
        // area the size of canvas : frame -> canvas cords.
        // -> network: only do the following rendering with objects inside this restricting area;
        renderJunctions(jl);

    }

    public void renderJunctions(Junction_List jl){
        // getter x y

        double x_offset = abs(jl.getMinPosX());
        double y_offset = abs(jl.getMinPosY());

        for(JunctionWrap jw : jl.getJunctions()){
            Point2D.Double p = jw.getPosition();
            gc.setFill(Color.RED);
            gc.fillOval((p.getX()+x_offset)*zoom, (p.getY()+y_offset)*zoom, 15*zoom, 15*zoom);
            jl.print_adjacency();
            for (String s : jl.getAdjacentVertexes(jw.getID())){
                System.out.println("herawdawkjf"  + s);
                //gc.strokePolyline();//p.X, p.Y s.getPosition.x, s.getPosition.y -> s id of edges connected to jw
                gc.setLineWidth(2);
                gc.strokeLine((p.x+x_offset+7)*zoom, (p.y+y_offset+7)*zoom, (jl.getJunction(s).getPosition().x+x_offset+7)*zoom, (jl.getJunction(s).getPosition().y+y_offset+7)*zoom);
            }

        }

        // transform

        // render
    }

}


