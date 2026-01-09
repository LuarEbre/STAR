package sumo.sim;

import javafx.scene.paint.Color;

/**
 * A helper class storing information about a {@link de.tudresden.sumo.objects.SumoTLSPhase}.
 *
 * <p>
 *     Created in {@link TrafficLightWrap} this class stores index, state and duration of one TrafficLight phase.
 *     All phases of one TrafficLight are collected in a List.
 * </p>
 */
public class TrafficLightPhase {

    private final int index;
    private final String state;
    private double duration;

    public TrafficLightPhase(int index, String state, double duration) {
        this.index = index;
        this.state = state;
        this.duration = duration;
    }

    public char getStateCode(int signalIndex) {
        if (signalIndex >= 0 && signalIndex < state.length()) {
            return state.charAt(signalIndex);
        }
        return 'o'; // sumo error
    }

    public Color getColor(int signalIndex) {
        char code = getStateCode(signalIndex);
        switch (code) {
            case 'r': return Color.RED;
            case 'y': return Color.YELLOW;
            case 'G': return Color.LIMEGREEN;
            case 'g': return Color.GREEN;
            // case 'u': return Color.ORANGE;
            // case 'o': return Color.BLACK;
            default: return Color.GREY;
        }
    }

    public int getIndex() { return index; }
    public String getState() { return state; }
    public double getDuration() { return duration; }

    public void setDuration(double duration) { this.duration = duration; }
}