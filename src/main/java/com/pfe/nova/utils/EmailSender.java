package com.pfe.nova.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.Session;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    public static void sendValidationEmail(String recipientEmail, String companyName) {
        String fromEmail = "amalmansri52@gmail.com"; // Email directement défini
        String password = "womhcqjiuimtjpxx"; // Mot de passe d'application directement défini
    
        if (fromEmail.isEmpty() || password.isEmpty()) {
            System.err.println("[ERREUR] Les credentials email ne sont pas définis. L'envoi d'email est annulé.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Validation de votre Compagnie");

            String content = "Bonjour,\n\nVotre compagnie '" + companyName + "' a été validée avec succès.\n\nMerci pour votre confiance.";

            message.setText(content);

            Transport.send(message);
            System.out.println("Email envoyé !");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
