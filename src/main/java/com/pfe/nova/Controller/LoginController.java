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
import com.pfe.nova.configuration.UserDAO;
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
            System.out.println("Authenticated user: " + user.getEmail() + ", Role: " + user.getRole());
            Session.setUtilisateurConnecte(user);
            navigateToDashboard(user);
        } else {
            showError("Invalid email or password");
            System.out.println("Authentication failed for email: " + email);
        }
    }


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

    @FXML
    private void navigateToForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/forgot-password.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Forgot Password");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load forgot password page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(User user) {
        try {
            System.out.println("Navigating to dashboard for role: " + user.getRole());
            String dashboardPath;

            if ("ADMIN".equals(user.getRole())) {
                dashboardPath = "/com/pfe/novaview/admin-dashboard.fxml";
            } else {
                dashboardPath = "/com/pfe/novaview/dashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(dashboardPath));
            Parent root = loader.load();

            if ("ADMIN".equals(user.getRole())) {
                AdminDashboardController adminController = loader.getController();
                adminController.initData(user);
            } else {
                DashboardController dashboardController = loader.getController();
                dashboardController.initData(user);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root,1200,800));
            stage.setTitle(user.getRole() + " Dashboard");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private User authenticateUser(String email, String password) {
        return UserDAO.authenticateUser(email, password);
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