package sumo.sim;

public abstract class SelectableObject {
    protected boolean isSelected;
    protected int selectRadius;

    public SelectableObject() {
        this.isSelected = false;
        this.selectRadius = 3;
    }

    public void select() {
        isSelected = true;
    }

    public void deselect() {
        isSelected = false;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public int getSelectRadius() {
        return selectRadius;
    }
}