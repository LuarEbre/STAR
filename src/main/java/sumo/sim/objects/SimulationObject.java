package sumo.sim.objects;

import sumo.sim.util.DrawableObjects;

public abstract class SimulationObject implements DrawableObjects {
    protected final String id;

    // for checking if visible
    protected double minX = Double.MAX_VALUE;
    protected double maxX = Double.MIN_VALUE;
    protected double minY = Double.MAX_VALUE;
    protected double maxY = Double.MIN_VALUE;

    protected double x;
    protected double y;

    public SimulationObject(String id) {
        this.id = id;
    }

    public String getId() { return id; }

    @Override
    public double getX() { return x; }

    @Override
    public double getY() { return y; }

    protected void calculateBounds(double[] xCoords, double[] yCoords) {
        if (xCoords != null && yCoords != null) {

            for (double v : xCoords) {
                if (v < minX) minX = v;
                if (v > maxX) maxX = v;
            }
            for (double v : yCoords) {
                if (v < minY) minY = v;
                if (v > maxY) maxY = v;
            }
            if (xCoords.length > 0) {
                this.x = xCoords[0];
                this.y = yCoords[0];
            }

        }
    }
}
