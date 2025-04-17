package com.pfe.nova.Controller;

import com.pfe.nova.models.User;
import com.pfe.nova.configuration.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.sql.SQLException;

public class EditUserController {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private TextField adresseField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button saveButton;

    private User user;

    public void initData(User user) {
        this.user = user;
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        telField.setText(user.getTel());
        adresseField.setText(user.getAdresse());
        roleComboBox.getItems().addAll("ADMIN", "MEDECIN", "PATIENT", "DONATEUR");
        roleComboBox.setValue(user.getRole());
    }

    @FXML
    private void handleSave() {
        // Update user object with form values
        user.setNom(nomField.getText());
        user.setPrenom(prenomField.getText());
        user.setEmail(emailField.getText());
        user.setTel(telField.getText());
        user.setAdresse(adresseField.getText());
        user.setRole(roleComboBox.getValue());
        
        try {
            // Save to database
            boolean success = UserDAO.updateUser(user);
            
            if (success) {
                // Close the form
                nomField.getScene().getWindow().hide();
            } else {
                showError("Failed to update user. No changes were made.");
            }
        } catch (SQLException e) {
            showError("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}