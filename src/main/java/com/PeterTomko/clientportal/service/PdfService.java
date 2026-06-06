package com.PeterTomko.clientportal.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.entity.Project;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;

@Service
public class PdfService {

    public byte[] generateInvoicePdf(Invoice invoice, Project project, String clientName) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 60, 60);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(44, 62, 80));
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY);

        Paragraph title = new Paragraph("CLIENT PORTAL", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("INVOICE #" + invoice.getId(), labelFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(4);
        document.add(subtitle);

        document.add(new Paragraph(" "));

        LineSeparator separator = new LineSeparator();
        separator.setLineColor(new Color(189, 195, 199));
        document.add(new Chunk(separator));

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        addRow(table, "Project", project.getName(), labelFont, valueFont);
        addRow(table, "Client", clientName, labelFont, valueFont);
        addRow(table, "Invoice Date", invoice.getCreatedAt().toLocalDate().toString(), labelFont, valueFont);
        addRow(table, "Due Date", invoice.getDueDate().toString(), labelFont, valueFont);
        addRow(table, "Amount", "$" + invoice.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(), labelFont, valueFont);
        addRow(table, "Status", invoice.getStatus().name(), labelFont, valueFont);

        document.add(table);

        document.add(new Chunk(separator));

        Paragraph footer = new Paragraph("Thank you for your business.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(16);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(new Color(245, 246, 250));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
