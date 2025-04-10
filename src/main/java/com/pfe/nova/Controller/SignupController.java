package com.pfe.nova.Controller;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.Role;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import javafx.fxml.FXML;

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
    private ComboBox<Role> roleComboBox;

    @FXML
    private Button signupButton;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        setupButtonHoverEffects();
        roleComboBox.setItems(javafx.collections.FXCollections.observableArrayList(
                Role.PATIENT, Role.DONATEUR
        ));

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
        Role selectedRole = roleComboBox.getValue();
        if (selectedRole == null) {
            showError("Please select a role");
            return;
        }


        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        boolean signupSuccessful = registerUser(username, email, hashedPassword,selectedRole);

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
    private boolean registerUser(String username, String email, String hashedPassword, Role role) {
        String query = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, hashedPassword);
            preparedStatement.setString(4, role.name()); // Convert enum to string like "PATIENT"
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