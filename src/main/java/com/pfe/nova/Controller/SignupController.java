package com.pfe.nova.Controller;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignupController {
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private TextField adresseField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private VBox dynamicFieldsContainer;
    @FXML private Label errorLabel;
    @FXML private Button signupButton;

    // Dynamic fields for different roles
    private TextField specialiteField;
    private TextField experienceField;
    private TextField diplomeField;
    private TextField ageField;
    private ComboBox<String> genderComboBox;
    private TextField bloodTypeField;
    private TextField donateurTypeField;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("MEDECIN", "PATIENT", "DONATEUR");
        roleComboBox.setOnAction(e -> updateDynamicFields());
        setupButtonHoverEffects();
    }

    private void updateDynamicFields() {
        dynamicFieldsContainer.getChildren().clear();
        String selectedRole = roleComboBox.getValue();

        if (selectedRole == null) return;

        switch (selectedRole) {
            case "MEDECIN":
                specialiteField = new TextField();
                experienceField = new TextField();
                diplomeField = new TextField();
                
                specialiteField.setPromptText("Specialité");
                experienceField.setPromptText("Experience");
                diplomeField.setPromptText("Diplome");
                
                dynamicFieldsContainer.getChildren().addAll(
                    specialiteField, experienceField, diplomeField
                );
                break;

            case "PATIENT":
                ageField = new TextField();
                genderComboBox = new ComboBox<>();
                bloodTypeField = new TextField();
                
                ageField.setPromptText("Age");
                genderComboBox.setPromptText("Gender");
                genderComboBox.getItems().addAll("Male", "Female");
                bloodTypeField.setPromptText("Blood Type");
                
                dynamicFieldsContainer.getChildren().addAll(
                    ageField, genderComboBox, bloodTypeField
                );
                break;

            case "DONATEUR":
                donateurTypeField = new TextField();
                donateurTypeField.setPromptText("Donateur Type");
                dynamicFieldsContainer.getChildren().add(donateurTypeField);
                break;
        }

        // Apply consistent styling to all dynamic fields
        dynamicFieldsContainer.getChildren().forEach(node -> {
            if (node instanceof TextField || node instanceof ComboBox) {
                node.setStyle("-fx-pref-width: 300px; -fx-pref-height: 40px");
            }
        });
    }

    @FXML
    private void handleSignup() {
        // Basic validation
        if (!validateFields()) return;

        // Check if email already exists
        if (isEmailExists(emailField.getText())) {
            showError("Email already registered");
            return;
        }

        // Email format validation
        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format");
            return;
        }

        try {
            // Generate a salt and hash the password
            String hashedPassword = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(12));
            User user = createUserBasedOnRole(hashedPassword);

            if (user != null && registerUser(user)) {
                showSuccess("Registration successful!");
                navigateToLogin();
            }
        } catch (IllegalArgumentException e) {
            showError("Error processing password");
            e.printStackTrace();
        }
    }

    private boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showSuccess(String message) {
        errorLabel.setStyle("-fx-text-fill: green;");
        showError(message); // Reusing the showError method for success messages
    }

    private boolean validateFields() {
        if (nomField.getText().isEmpty() || 
            prenomField.getText().isEmpty() || 
            emailField.getText().isEmpty() || 
            telField.getText().isEmpty() || 
            adresseField.getText().isEmpty() || 
            passwordField.getText().isEmpty() || 
            confirmPasswordField.getText().isEmpty() || 
            roleComboBox.getValue() == null) {
            
            showError("Please fill in all required fields");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match");
            return false;
        }

        // Validate role-specific fields
        String role = roleComboBox.getValue();
        switch (role) {
            case "MEDECIN":
                if (specialiteField.getText().isEmpty() || 
                    experienceField.getText().isEmpty() || 
                    diplomeField.getText().isEmpty()) {
                    showError("Please fill in all medical professional fields");
                    return false;
                }
                break;
            case "PATIENT":
                if (ageField.getText().isEmpty() || 
                    genderComboBox.getValue() == null || 
                    bloodTypeField.getText().isEmpty()) {
                    showError("Please fill in all patient fields");
                    return false;
                }
                try {
                    Integer.parseInt(ageField.getText());
                } catch (NumberFormatException e) {
                    showError("Age must be a valid number");
                    return false;
                }
                break;
            case "DONATEUR":
                if (donateurTypeField.getText().isEmpty()) {
                    showError("Please specify donor type");
                    return false;
                }
                break;
        }
        return true;
    }

    private User createUserBasedOnRole(String hashedPassword) {
        String role = roleComboBox.getValue();
        if (role == null) return null;

        switch (role) {
            case "MEDECIN":
                return new Medecin(
                    0, // ID will be set by database
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    telField.getText(),
                    adresseField.getText(),
                    hashedPassword,
                    "", // picture placeholder
                    specialiteField.getText(),
                    experienceField.getText(),
                    diplomeField.getText()
                );

            case "PATIENT":
                return new Patient(
                    0,
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    telField.getText(),
                    adresseField.getText(),
                    hashedPassword,
                    "",
                    Integer.parseInt(ageField.getText()),
                    genderComboBox.getValue(),
                    bloodTypeField.getText()
                );

            case "DONATEUR":
                return new Donateur(
                    0,
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    telField.getText(),
                    adresseField.getText(),
                    hashedPassword,
                    "",
                    donateurTypeField.getText()
                );

            default:
                return null;
        }
    }

    private boolean registerUser(User user) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // First, insert the base user data
            String userQuery = "INSERT INTO user (nom, prenom, email, tel, adresse, password, picture, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement userStmt = connection.prepareStatement(userQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, user.getNom());
                userStmt.setString(2, user.getPrenom());
                userStmt.setString(3, user.getEmail());
                userStmt.setString(4, user.getTel());
                userStmt.setString(5, user.getAdresse());
                userStmt.setString(6, user.getPassword());
                userStmt.setString(7, user.getPicture());
                userStmt.setString(8, user.getRole());
                
                int affectedRows = userStmt.executeUpdate();
                
                if (affectedRows == 0) {
                    showError("Failed to create user");
                    return false;
                }

                // Get the generated user ID
                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        
                        // Handle specific fields based on user type
                        switch (user.getRole()) {
                            case "MEDECIN":
                                Medecin medecin = (Medecin) user;
                                String medecinQuery = "UPDATE user SET specialite = ?, experience = ?, diplome = ? WHERE id = ?";
                                try (PreparedStatement medecinStmt = connection.prepareStatement(medecinQuery)) {
                                    medecinStmt.setString(1, medecin.getSpecialite());
                                    medecinStmt.setString(2, medecin.getExperience());
                                    medecinStmt.setString(3, medecin.getDiplome());
                                    medecinStmt.setInt(4, userId);
                                    medecinStmt.executeUpdate();
                                }
                                break;

                            case "PATIENT":
                                Patient patient = (Patient) user;
                                String patientQuery = "UPDATE user SET age = ?, gender = ?, blood_type = ? WHERE id = ?";
                                try (PreparedStatement patientStmt = connection.prepareStatement(patientQuery)) {
                                    patientStmt.setInt(1, patient.getAge());
                                    patientStmt.setString(2, patient.getGender());
                                    patientStmt.setString(3, patient.getBloodType());
                                    patientStmt.setInt(4, userId);
                                    patientStmt.executeUpdate();
                                }
                                break;

                            case "DONATEUR":
                                Donateur donateur = (Donateur) user;
                                String donateurQuery = "UPDATE user SET donateur_type = ? WHERE id = ?";
                                try (PreparedStatement donateurStmt = connection.prepareStatement(donateurQuery)) {
                                    donateurStmt.setString(1, donateur.getDonateurType());
                                    donateurStmt.setInt(2, userId);
                                    donateurStmt.executeUpdate();
                                }
                                break;
                        }
                        return true;
                    } else {
                        showError("Failed to retrieve user ID");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void setupButtonHoverEffects() {
        signupButton.setOnMouseEntered(e ->
                signupButton.setStyle("-fx-background-color: #2980b9; -fx-pref-width: 300px; -fx-pref-height: 40px; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 3;")
        );
        signupButton.setOnMouseExited(e ->
                signupButton.setStyle("-fx-background-color: #3498db; -fx-pref-width: 300px; -fx-pref-height: 40px; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 3;")
        );
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/login.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find login.fxml");
            }
            Parent root = loader.load();
            Stage stage = (Stage) signupButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load login page: " + e.getMessage());
            System.err.println("Error loading login page: " + e.getMessage());
            e.printStackTrace();
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