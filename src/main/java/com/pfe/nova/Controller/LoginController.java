package com.pfe.nova.Controller;

import com.pfe.nova.configuration.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        setupButtonHoverEffects();
    }

    private void setupButtonHoverEffects() {
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle("-fx-background-color: #2980b9; -fx-pref-width: 300px; -fx-pref-height: 40px; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 3;")
        );
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle("-fx-background-color: #3498db; -fx-pref-width: 300px; -fx-pref-height: 40px; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 3;")
        );
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        System.out.println("Attempting to log in with email: " + email);

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        boolean loginSuccessful = authenticate(email, password);

        if (loginSuccessful) {
            System.out.println("Login successful");
            if (rememberMeCheckbox.isSelected()) {
                // Save login credentials (implement securely)
            }
            // Pass the username to navigateToDashboard
            navigateToDashboard(authenticatedUsername);
        } else {
            showError("Invalid email or password");
        }
    }

    private void navigateToDashboard(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController dashboardController = loader.getController();
            dashboardController.setUsername(username);

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("Dashboard");
        } catch (IOException e) {
            showError("Unable to load dashboard");
            e.printStackTrace();
        }
    }

    private String authenticatedUsername;

    private boolean authenticate(String email, String password) {
        String query = "SELECT username, password FROM users WHERE email = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("password");
                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    authenticatedUsername = resultSet.getString("username");
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleSignUp() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/signup.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sign Up");
        } catch (IOException e) {
            showError("Unable to load signup page");
            e.printStackTrace();
        }
    }

//    private void navigateToDashboard() {
//        try {
//            System.out.println("Navigating to dashboard...");
//            Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/dashboard.fxml"));
//            Stage stage = (Stage) emailField.getScene().getWindow();
//            stage.setScene(new Scene(root));
//            stage.setTitle("Dashboard");
//            System.out.println("Dashboard loaded successfully");
//        } catch (IOException e) {
//            showError("Unable to load dashboard");
//            e.printStackTrace();
//        }
//    }

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