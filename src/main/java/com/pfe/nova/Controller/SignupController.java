package com.pfe.nova.Controller;

import com.pfe.nova.configuration.DatabaseConnection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignupController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button signupButton;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        setupButtonHoverEffects();
    }

    private void setupButtonHoverEffects() {
        signupButton.setOnMouseEntered(e ->
                signupButton.setStyle("-fx-background-color: #2980b9; -fx-pref-width: 300px; -fx-pref-height: 40px; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 3;")
        );
        signupButton.setOnMouseExited(e ->
                signupButton.setStyle("-fx-background-color: #3498db; -fx-pref-width: 300px; -fx-pref-height: 40px; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 3;")
        );
    }

    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        boolean signupSuccessful = registerUser(username, email, hashedPassword);

        if (signupSuccessful) {
            System.out.println("Signup successful");
            navigateToLogin();
        } else {
            showError("Signup failed");
        }
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/login.fxml"));
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            showError("Unable to load login page");
            e.printStackTrace();
        }
    }
    private boolean registerUser(String username, String email, String hashedPassword) {
        String query = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, hashedPassword);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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