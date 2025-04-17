package com.pfe.nova.Controller;

import com.pfe.nova.models.User;
import com.pfe.nova.configuration.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class EditUserController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button saveButton;

    private User user;

    public void initData(User user) {
        this.user = user;
        nameField.setText(user.getNom() + " " + user.getPrenom());
        emailField.setText(user.getEmail());
        roleComboBox.getItems().addAll("ADMIN", "MEDECIN", "PATIENT", "DONATEUR");
        roleComboBox.setValue(user.getRole());
    }

    @FXML
    private void handleSave() {
        user.setNom(nameField.getText().split(" ")[0]);
        user.setPrenom(nameField.getText().split(" ")[1]);
        user.setEmail(emailField.getText());
        user.setRole(roleComboBox.getValue());

        UserDAO.updateUser(user);
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}