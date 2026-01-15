package sumo.sim;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import sumo.sim.util.ExportableData;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataExport {

    public static void exportSelectionAsPDF(File file, List<ExportableData> selections, long simSteps, double simTime) throws Exception {
        // format
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        // font
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.BLUE);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10); // header
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);        // data

        document.add(new Paragraph("SUMO Simulation - Report", titleFont));
        document.add(new Paragraph("Date: " +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), metaFont));
        document.add(new Paragraph("Simulation Runtime: " + String.format(java.util.Locale.US, "%.2f s", simTime), metaFont));
        document.add(new Paragraph("Steps: " + simSteps, metaFont));
        document.add(new Paragraph(" "));

        Map<String, List<ExportableData>> grouped = selections.stream()
                .collect(Collectors.groupingBy(ExportableData::getExportCategory));

        for (Map.Entry<String, List<ExportableData>> entry : grouped.entrySet()) {
            List<ExportableData> items = entry.getValue();
            // continue if list not empty
            if (items == null || items.isEmpty()) {
                continue;
            }
            document.add(new Paragraph(entry.getKey() + ":", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            document.add(new Paragraph(" "));

            String[] headers = items.get(0).getColumnHeaders();

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            //calculate columns
            float[] widths = new float[headers.length];
            if (headers.length > 0) {
                widths[0] = 0.8f; // ID
                for(int i = 1; i < headers.length; i++) {
                    widths[i] = 2.0f; // remaining columns
                }
                table.setWidths(widths);
            }
            // Header
            for (String h : headers) {
                // check for line breaks header
                PdfPCell cell = new PdfPCell(new Phrase(h.trim().replace("\n", ""), headerFont));
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            // rows for data
            for (ExportableData item : items) {
                for (String data : item.getRowData()) {
                    // check for line breaks data
                    String cleanData = data.trim().replace("\n", "").replace("\r", "");
                    PdfPCell dataCell = new PdfPCell(new Phrase(cleanData, dataFont));
                    dataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(dataCell);
                }
            }
            document.add(table);
            document.add(new Paragraph(" "));
        }
        document.close();
    }
}