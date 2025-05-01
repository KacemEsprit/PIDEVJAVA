package com.pfe.nova.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailPostService {
    private final String username;
    private final String password;
    private final Properties props;

    public EmailPostService(String username, String password) {
        this.username = username;
        this.password = password;

        // Configure email properties for Gmail SMTP
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        // Disable debugging to reduce noise
        props.put("mail.debug", "false");
        
        // Add these properties to fix SSL/TLS issues
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        // Add these properties to fix threading issues
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
    }

    /**
     * Envoie un email en texte brut
     */
    public void sendEmail(String recipient, String subject, String body) throws MessagingException {
        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Create a new email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(body);

        // Send the email
        Transport.send(message);
        System.out.println("Email envoyé avec succès à " + recipient);
    }

    /**
     * Envoie un email au format HTML
     */
    public void sendHtmlEmail(String recipient, String subject, String htmlContent) throws MessagingException {
        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Create a new email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);

        // Set the content as HTML
        message.setContent(htmlContent, "text/html; charset=utf-8");

        // Send the email
        Transport.send(message);
        System.out.println("Email HTML envoyé avec succès à " + recipient);
    }
}