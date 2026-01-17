package sumo.sim.util;

public interface ExportableData {
    String getExportCategory();
    String[] getColumnHeaders();
    String[] getRowData();
}
