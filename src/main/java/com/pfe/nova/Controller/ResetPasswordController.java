package com.pfe.nova.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;
import com.pfe.nova.configuration.UserDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.pfe.nova.configuration.DatabaseConnection;

public class ResetPasswordController {
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetButton;
    @FXML private Label infoLabel;

    private String email;

    public void setEmail(String email) {
        this.email = email;
        System.out.println("Email set in ResetPasswordController: " + email);
    }

    @FXML
    private void handleReset() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            infoLabel.setText("Please fill in all fields.");
            infoLabel.setVisible(true);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            infoLabel.setText("Passwords do not match.");
            infoLabel.setVisible(true);
            return;
        }

        // Hash the password using BCrypt (same as in SignupController)
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        
        // Update the password in the database
        boolean updated = updatePassword(email, hashedPassword);
        
        if (updated) {
            infoLabel.setText("Password reset successful! Redirecting to login...");
            infoLabel.setVisible(true);
            
            // Add a short delay before navigating to login
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> navigateToLogin());
            pause.play();
        } else {
            infoLabel.setText("Failed to reset password. Please try again.");
            infoLabel.setVisible(true);
        }
    }
    
    private boolean updatePassword(String email, String hashedPassword) {
        String sql = "UPDATE user SET password = ? WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) resetButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            infoLabel.setText("Unable to load login page: " + e.getMessage());
            infoLabel.setVisible(true);
            e.printStackTrace();
        }
    }
}