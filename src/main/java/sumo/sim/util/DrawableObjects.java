package sumo.sim.util;

import javafx.scene.canvas.GraphicsContext;

/**
 * Generic class used for declaring drawable objects and render them
 */
public interface DrawableObjects {

    void draw(GraphicsContext gc, double zoom);
    double getX();
    double getY();
}
