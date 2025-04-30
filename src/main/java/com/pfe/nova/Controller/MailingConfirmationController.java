package com.pfe.nova.Controller;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import com.pfe.nova.models.Order;
import com.pfe.nova.models.Medication;

public class MailingConfirmationController {
    
    private static final String EMAIL_FROM = "mohsnimaha1@gmail.com"; // À remplacer par votre email
    private static final String EMAIL_PASSWORD = "olajjsqqmpcfzrcp"; // À remplacer par votre mot de passe d'application

    public static void sendOrderConfirmationEmail(Order order) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(order.getUser().getEmail()));
            message.setSubject("Confirmation de votre commande #" + order.getId());

            // Création du contenu HTML de l'email
            String htmlContent = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<div style='background-color: #34B1DB; color: white; padding: 20px; text-align: center;'>" +
                "<h1>OncoKidsCare</h1>" +
                "<h2>Confirmation de Commande</h2>" +
                "</div>" +
                "<div style='padding: 20px; background-color: #f8f9fa;'>" +
                "<p>Cher(e) %s %s,</p>" +
                "<p>Nous vous confirmons que votre commande #%d a été validée avec succès vous pouvez la ramener.</p>" +
                "<h3>Détails de votre commande :</h3>" +
                "<table style='width: 100%%; border-collapse: collapse; margin-top: 20px;'>" +
                "<tr style='background-color: #34B1DB; color: white;'>" +
                "<th style='padding: 10px; text-align: left;'>Médicament</th>" +
                "<th style='padding: 10px; text-align: center;'>Quantité</th>" +
                "<th style='padding: 10px; text-align: right;'>Prix</th>" +
                "</tr>",
                order.getUser().getNom(),
                order.getUser().getPrenom(),
                order.getId()
            );

            // Ajout des médicaments dans le tableau
            for (Medication med : order.getMedications()) {
                htmlContent += String.format(
                    "<tr style='border-bottom: 1px solid #ddd;'>" +
                    "<td style='padding: 10px;'>%s</td>" +
                    "<td style='padding: 10px; text-align: center;'>%d</td>" +
                    "<td style='padding: 10px; text-align: right;'>%.2f DT</td>" +
                    "</tr>",
                    med.getNom(),
                    med.getQuantiteCommande(),
                    med.getPrix() * med.getQuantiteCommande()
                );
            }

            // Ajout du total et du pied de page
            htmlContent += String.format(
                "</table>" +
                "<div style='text-align: right; margin-top: 20px; font-weight: bold;'>" +
                "Total : %.2f DT" +
                "</div>" +
                "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;'>" +
                "<p>Merci de votre confiance !</p>" +
                "<p>L'équipe OncoKidsCare</p>" +
                "</div>" +
                "</div>" +
                "</div>",
                order.getMontantTotal()
            );

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email de confirmation : " + e.getMessage());
        }
    }
}