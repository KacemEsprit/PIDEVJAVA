package com.pfe.nova.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.pfe.nova.models.Order;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class RewardPDFGenerator {

    public String generateRewardTicket(Order order, String cadeau) {
        try {
            // Le dossier
            String folderPath = "C:/xampp/htdocs/rewards/";
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            // Le fichier PDF
            String fileName = "reward_ticket_order_" + order.getId() + ".pdf";
            String filePath = folderPath + fileName;

            // Création du document
            Document document = new Document(PageSize.A5.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Fonts
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 28, Font.BOLD, new BaseColor(41, 128, 185));
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(39, 174, 96));
            Font textFont = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.DARK_GRAY);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, new BaseColor(127, 140, 141));

            // 🎨 Bandeau supérieur coloré
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell(new Phrase("🎁 VOTRE RÉCOMPENSE 🎁", titleFont));
            headerCell.setBackgroundColor(new BaseColor(52, 152, 219)); // Bleu clair
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBorder(Rectangle.NO_BORDER);
            headerCell.setPaddingTop(20);
            headerCell.setPaddingBottom(20);
            header.addCell(headerCell);
            document.add(header);

            document.add(new Paragraph("\n"));

            // 🎉 Partie centrale encadrée
            PdfPTable body = new PdfPTable(1);
            body.setWidthPercentage(80);
            PdfPCell bodyCell = new PdfPCell();
            bodyCell.setPadding(20);
            bodyCell.setBackgroundColor(new BaseColor(236, 240, 241)); // Gris clair
            bodyCell.setBorderColor(new BaseColor(149, 165, 166)); // Gris foncé
            bodyCell.setBorderWidth(2f);

            String dateFormatted = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(order.getDate());

            Paragraph content = new Paragraph(
                    "Commande N° " + order.getId() + "\n\n" +
                            "🗓️ Date : " + dateFormatted + "\n\n" +
                            "🎉 Félicitations 🎉\n\n" +
                            "Vous avez gagné :\n\n" +
                            cadeau,
                    textFont
            );
            content.setAlignment(Element.ALIGN_CENTER);

            bodyCell.addElement(content);
            body.addCell(bodyCell);
            document.add(body);

            document.add(new Paragraph("\n\n"));

            // ➡️ Une ligne de séparation décorative
            LineSeparator ls = new LineSeparator();
            ls.setLineColor(new BaseColor(127, 140, 141));
            document.add(new Chunk(ls));

            document.add(new Paragraph("\n"));

            Paragraph thanks = new Paragraph("Merci pour votre fidélité ❤️", sectionFont);
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            document.add(new Paragraph("\n"));

            // Petit texte en bas
            Paragraph footer = new Paragraph("Présentez ce ticket à votre pharmacie pour récupérer votre récompense.", smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            return filePath;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
