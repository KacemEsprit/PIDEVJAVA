package com.pfe.nova.Controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.pfe.nova.models.Order;
import com.pfe.nova.models.Medication;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class PDFController {

    public String generateInvoicePDF(Order order) {
        try {
            // ➡️ Enregistrer dans ton chemin spécifique
            String saveDirectory = "C:/Users/DELL/OneDrive/Bureau/copieintegration/";
            new File(saveDirectory).mkdirs(); // Créer dossier si pas existant

            Document document = new Document(PageSize.A4);
            String fileName = saveDirectory + "Facture_" + order.getId() + "_" + System.currentTimeMillis() + ".pdf";
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Canvas pour arrière-plan
            PdfContentByte backgroundCanvas = writer.getDirectContentUnder();
            drawBackgroundCurves(backgroundCanvas, document.getPageSize());

            // Titre OncoKidsCare
            Font brandFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, new BaseColor(255, 140, 0));
            Paragraph brand = new Paragraph("OncoKidsCare", brandFont);
            brand.setAlignment(Element.ALIGN_RIGHT);
            brand.setSpacingBefore(20);
            brand.setIndentationRight(40);
            document.add(brand);

            // Titre FACTURE
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 32, Font.BOLD, new BaseColor(0, 51, 102));
            Paragraph title = new Paragraph("FACTURE COMMANDE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(30);
            title.setSpacingAfter(30);
            document.add(title);

            // Infos Status et Date
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, new BaseColor(0, 51, 102));
            infoTable.addCell(createInfoCell("Status: " + order.getStatus(), infoFont));
            infoTable.addCell(createInfoCellRight("Date: " + order.getDateCommande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), infoFont));
            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Infos Client
            PdfPTable clientInfoTable = new PdfPTable(1);
            clientInfoTable.setWidthPercentage(100);
            PdfPCell clientCell = new PdfPCell();
            clientCell.setBackgroundColor(new BaseColor(245, 247, 250));
            clientCell.setPadding(15);
            clientCell.setBorder(Rectangle.BOX); // Bordure visible
            clientCell.setPhrase(new Phrase(
                "INFORMATIONS CLIENT\n\n" +
                "Nom: " + order.getUser().getNom() + "\n" +
                "Prénom: " + order.getUser().getPrenom() + "\n" +
                "Email: " + order.getUser().getEmail(),
                new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, new BaseColor(0, 51, 102))
            ));
            clientInfoTable.addCell(clientCell);
            document.add(clientInfoTable);

            // Tableau Médicaments avec lignes
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(30);
            table.setSpacingAfter(30);

            BaseColor headerColor = new BaseColor(52, 152, 219);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);

            // Headers avec bordures
            Stream.of("Médicament", "Prix Unitaire (DT)", "Quantité")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell(new Phrase(columnTitle, headerFont));
                    header.setBackgroundColor(headerColor);
                    header.setBorderWidth(1); // ➡️ Bordure visible
                    header.setPadding(10);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });

            Font contentFont = new Font(Font.FontFamily.HELVETICA, 11);
            BaseColor altColor = new BaseColor(249, 249, 249);

            for (int i = 0; i < order.getMedications().size(); i++) {
                Medication med = order.getMedications().get(i);
                BaseColor rowColor = (i % 2 == 0) ? BaseColor.WHITE : altColor;

                table.addCell(createStyledTableCell(med.getNom(), contentFont, rowColor));
                table.addCell(createStyledTableCell(String.format("%.2f", med.getPrix()), contentFont, rowColor));
                table.addCell(createStyledTableCell(String.valueOf(med.getQuantiteCommande()), contentFont, rowColor));
            }
            document.add(table);

            // Total à payer
            PdfPTable totalTable = new PdfPTable(1);
            totalTable.setWidthPercentage(35);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Font totalFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.WHITE);
            PdfPCell totalCell = new PdfPCell(new Phrase(
                    String.format("Total à payer\n%.2f DT", order.getMontantTotal()), totalFont));
            totalCell.setBackgroundColor(new BaseColor(0, 51, 102));
            totalCell.setBorder(Rectangle.BOX);
            totalCell.setPadding(20);
            totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalTable.addCell(totalCell);
            document.add(totalTable);

            // Footer
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Merci pour votre confiance ! | OncoKidsCare © 2025", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(50);
            document.add(footer);

            document.close();

            // ➡️ Ouvrir automatiquement
           return fileName;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }

    private PdfPCell createInfoCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell createInfoCellRight(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private PdfPCell createStyledTableCell(String content, Font font, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setBorderWidth(1); // ➡️ Bordure visible sur chaque cellule
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private void drawBackgroundCurves(PdfContentByte canvas, Rectangle pageSize) {
        float width = pageSize.getWidth();
        float height = pageSize.getHeight();

        canvas.setColorFill(new BaseColor(235, 245, 251));
        canvas.moveTo(width * 0.7f, height);
        canvas.curveTo(width * 0.8f, height * 0.9f, width, height * 0.8f, width, height * 0.7f);
        canvas.lineTo(width, height);
        canvas.lineTo(width * 0.7f, height);
        canvas.fill();

        canvas.moveTo(0, height * 0.3f);
        canvas.curveTo(width * 0.1f, height * 0.2f, width * 0.2f, 0, width * 0.3f, 0);
        canvas.lineTo(0, 0);
        canvas.lineTo(0, height * 0.3f);
        canvas.fill();
    }
}
