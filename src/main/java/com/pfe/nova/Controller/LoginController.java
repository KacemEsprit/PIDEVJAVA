package com.pfe.nova.Controller;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.*;
import com.pfe.nova.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        setupButtonHoverEffects();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        User user = authenticateUser(email, password);
        if (user != null) {
            // Store the connected user in the session
            Session.setUtilisateurConnecte(user);
            System.out.println("User session set for: " + user.getEmail());

            // Navigate to the dashboard
            navigateToDashboard(user);
        } else {
            showError("Invalid email or password");
        }
    }
//    private void handleLogin() {
//        String email = emailField.getText();
//        String password = passwordField.getText();
//
//        if (email.isEmpty() || password.isEmpty()) {
//            showError("Please fill in all fields");
//            return;
//        }
//
//        User user = authenticateUser(email, password);
//        if (user != null) {
//            navigateToDashboard(user);
//        } else {
//            showError("Invalid email or password");
//        }
//    }

    @FXML
    private void navigateToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/signup.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find signup.fxml");
            }
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Sign Up");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load signup page: " + e.getMessage());
            System.err.println("Error loading signup page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(User user) {
        try {
            System.out.println("Loading dashboard for user role: " + user.getRole());
            String dashboardPath = "/com/pfe/novaview/dashboard.fxml";  // Fixed path to match project structure
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(dashboardPath));
            if (loader.getLocation() == null) {
                System.err.println("Dashboard FXML not found at: " + dashboardPath);
                throw new IOException("Cannot find dashboard.fxml at " + dashboardPath);
            }
            
            Parent root = loader.load();
            DashboardController dashboardController = loader.getController();
            
            if (dashboardController == null) {
                throw new IOException("Failed to get DashboardController instance");
            }
            
            dashboardController.initData(user);
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(user.getRole() + " Dashboard");
            stage.setResizable(true);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            showError("Unable to load dashboard: " + e.getMessage());
            System.err.println("Dashboard loading error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private User authenticateUser(String email, String password) {
        String query = "SELECT * FROM user WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPasswordFromDB = rs.getString("password");
                    System.out.println("Attempting login for email: " + email);
                    
                    try {
                        if (BCrypt.checkpw(password, hashedPasswordFromDB)) {
                            String role = rs.getString("role");
                            System.out.println("Password verified successfully for role: " + role);
                            
                            switch (role) {
                                case "PATIENT":
                                    return new Patient(
                                        rs.getInt("id"),
                                        rs.getString("nom"),
                                        rs.getString("prenom"),
                                        email,
                                        rs.getString("tel"),
                                        rs.getString("adresse"),
                                        hashedPasswordFromDB,
                                        rs.getString("picture"),
                                        rs.getInt("age"),
                                        rs.getString("gender"),
                                        rs.getString("blood_type")
                                    );
                                    
                                case "MEDECIN":
                                    return new Medecin(
                                        rs.getInt("id"),
                                        rs.getString("nom"),
                                        rs.getString("prenom"),
                                        email,
                                        rs.getString("tel"),
                                        rs.getString("adresse"),
                                        hashedPasswordFromDB,
                                        rs.getString("picture"),
                                        rs.getString("specialite"),
                                        rs.getString("experience"),
                                        rs.getString("diplome")
                                    );
                                    
                                case "DONATEUR":
                                    return new Donateur(
                                        rs.getInt("id"),
                                        rs.getString("nom"),
                                        rs.getString("prenom"),
                                        email,
                                        rs.getString("tel"),
                                        rs.getString("adresse"),
                                        hashedPasswordFromDB,
                                        rs.getString("picture"),
                                        rs.getString("donateur_type")
                                    );
                            }
                        } else {
                            System.out.println("Password verification failed");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("BCrypt verification error: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("No user found with email: " + email);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void setupButtonHoverEffects() {
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle("-fx-background-color: #2980b9; -fx-pref-width: 300px; -fx-pref-height: 40px; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 3;")
        );
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle("-fx-background-color: #3498db; -fx-pref-width: 300px; -fx-pref-height: 40px; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 3;")
        );
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> errorLabel.setVisible(false));
                    }
                },
                5000
        );
    }
}