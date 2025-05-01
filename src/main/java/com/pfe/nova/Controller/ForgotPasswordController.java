package com.pfe.nova.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class ForgotPasswordController {
    @FXML private TextField emailField;
    @FXML private Button sendButton;
    @FXML private Label infoLabel;

    @FXML
    private void handleSend() {
        String email = emailField.getText().trim(); // Add trim to remove any whitespace
        if (email.isEmpty()) {
            showInfo("Please enter your email.");
            return;
        }
        String code = generateResetCode();
        resetCodes.put(email, code);
        System.out.println("Generated code for " + email + ": " + code); // Debug line
        boolean sent = sendResetEmail(email, code);
        if (sent) {
            showInfo("A reset code has been sent to your email.");
            PauseTransition pause = new PauseTransition(Duration.seconds(2)); // 2 seconds delay
            pause.setOnFinished(event -> openCodeEntryForm(email));
            pause.play();
        } else {
            showInfo("Failed to send email. Please try again.");
        }
    }

    private String generateResetCode() {
        Random rand = new Random();
        int code = 100000 + rand.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }

    private boolean sendResetEmail(String toEmail, String code) {
        final String fromEmail = "kacem.benbrahim07@gmail.com";
        final String password = "wgfecqucjbjopepi";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Password Reset Code");
           // message.setText("Your password reset code is: " + code);

            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "  <style>" +
                    "    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                    "    .container { background-color: #ffffff; padding: 20px; border-radius: 8px; max-width: 500px; margin: auto; box-shadow: 0 0 10px rgba(0,0,0,0.1); }" +
                    "    h2 { color: #2c3e50; }" +
                    "    p { color: #34495e; }" +
                    "    .code { font-size: 24px; font-weight: bold; color: #e74c3c; background-color: #f9ecec; padding: 10px; border-radius: 5px; text-align: center; }" +
                    "    .footer { margin-top: 20px; font-size: 12px; color: #95a5a6; }" +
                    "  </style>" +
                    "</head>" +
                    "<body>" +
                    "  <div class='container'>" +
                    "    <h2>Password Reset Request</h2>" +
                    "    <p>Dear user,</p>" +
                    "    <p>We received a request to reset your password. Use the code below to proceed:</p>" +
                    "    <div class='code'>" + code + "</div>" +
                    "    <p>If you didn't request a password reset, please ignore this email.</p>" +
                    "    <p class='footer'>This is an automated message. Please do not reply.</p>" +
                    "  </div>" +
                    "</body>" +
                    "</html>";

            message.setContent(htmlContent, "text/html; charset=utf-8");


            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Add a method to verify the code (to be called from the code entry form)
    public static boolean verifyResetCode(String email, String code) {
        if (email == null || code == null) {
            System.out.println("Email or code is null");
            return false;
        }
        
        String storedCode = resetCodes.get(email);
        System.out.println("Verifying code for " + email + ": entered=" + code + ", stored=" + storedCode);
        
        if (storedCode == null) {
            System.out.println("No code found for email: " + email);
            return false;
        }
        
        return code.equals(storedCode);
    }

    @FXML
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) sendButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showInfo("Unable to load login page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showInfo(String message) {
        infoLabel.setText(message);
        infoLabel.setVisible(true);
    }

    // Example using JavaMail API (add javax.mail dependency to your project)
    private boolean sendResetEmail(String toEmail) {
        final String fromEmail = "kacem.benbrahim07@gmail.com";
        final String password = "wgfecqucjbjopepi";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // for Gmail
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Password Reset Request");
            message.setText("Click the link to reset your password: [reset link here]");
            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // For demo: store codes in memory (replace with DB in production)
    private static final ConcurrentHashMap<String, String> resetCodes = new ConcurrentHashMap<>();

    private void openCodeEntryForm(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/code-entry.fxml"));
            Parent root = loader.load();
            CodeEntryController controller = loader.getController();
            controller.setEmail(email);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Enter Verification Code");
            stage.show();
            // Optionally close the current window
            ((Stage) sendButton.getScene().getWindow()).close();
        } catch (IOException e) {
            showInfo("Unable to open code entry form: " + e.getMessage());
            e.printStackTrace();
        }
    }
}