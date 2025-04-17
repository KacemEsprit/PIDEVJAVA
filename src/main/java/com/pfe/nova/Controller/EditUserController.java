package com.pfe.nova.Controller;

import com.pfe.nova.models.*;
import com.pfe.nova.configuration.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EditUserController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private VBox roleSpecificFields;

    private User currentUser; // Declare the currentUser variable

    // Role-specific fields
    private TextField specialityField;
    private TextField experienceField;
    private TextField diplomaField;
    private TextField ageField;
    private ComboBox<String> genderComboBox;
    private TextField bloodTypeField;
    private TextField donateurTypeField;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("ADMIN", "MEDECIN", "PATIENT", "DONATEUR");

        // Initialize role-specific fields
        specialityField = new TextField();
        specialityField.setPromptText("Speciality");

        experienceField = new TextField();
        experienceField.setPromptText("Experience");

        diplomaField = new TextField();
        diplomaField.setPromptText("Diploma");

        ageField = new TextField();
        ageField.setPromptText("Age");

        genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("Male", "Female");
        genderComboBox.setPromptText("Gender");

        bloodTypeField = new TextField();
        bloodTypeField.setPromptText("Blood Type");

        donateurTypeField = new TextField();
        donateurTypeField.setPromptText("Donateur Type");
    }

    public void initData(User user) {
        this.currentUser = user; // Initialize currentUser
        nameField.setText(user.getNom() + " " + user.getPrenom());
        emailField.setText(user.getEmail());
        telField.setText(user.getTel());
        addressField.setText(user.getAdresse());
        roleComboBox.setValue(user.getRole());

        // Clear previous role-specific fields
        roleSpecificFields.getChildren().clear();

        // Add role-specific fields based on user type
        switch (user.getRole()) {
            case "MEDECIN":
                Medecin medecin = (Medecin) user;
                specialityField.setText(medecin.getSpecialite());
                experienceField.setText(medecin.getExperience());
                diplomaField.setText(medecin.getDiplome());
                roleSpecificFields.getChildren().addAll(specialityField, experienceField, diplomaField);
                break;

            case "PATIENT":
                Patient patient = (Patient) user;
                ageField.setText(String.valueOf(patient.getAge()));
                genderComboBox.setValue(patient.getGender());
                bloodTypeField.setText(patient.getBloodType());
                roleSpecificFields.getChildren().addAll(ageField, genderComboBox, bloodTypeField);
                break;

            case "DONATEUR":
                Donateur donateur = (Donateur) user;
                donateurTypeField.setText(donateur.getDonateurType());
                roleSpecificFields.getChildren().add(donateurTypeField);
                break;
        }
    }

    @FXML
    private void handleSave() {
        try {
            // Validate required fields
            if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty() || telField.getText().trim().isEmpty() || addressField.getText().trim().isEmpty()) {
                showError("All fields are required.");
                return;
            }

            // Split name into first and last name
            String[] nameParts = nameField.getText().trim().split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            // Create a User object based on the role
            User user;
            String role = roleComboBox.getValue();
            switch (role) {
                case "MEDECIN":
                    user = new Medecin();
                    ((Medecin) user).setSpecialite(specialityField.getText().trim());
                    ((Medecin) user).setExperience(experienceField.getText().trim());
                    ((Medecin) user).setDiplome(diplomaField.getText().trim());
                    break;
                case "PATIENT":
                    user = new Patient();
                    ((Patient) user).setAge(Integer.parseInt(ageField.getText().trim()));
                    ((Patient) user).setGender(genderComboBox.getValue());
                    ((Patient) user).setBloodType(bloodTypeField.getText().trim());
                    break;
                case "DONATEUR":
                    user = new Donateur();
                    ((Donateur) user).setDonateurType(donateurTypeField.getText().trim());
                    break;
                default:
                    user = new User();
            }

            // Set common fields
            user.setId(this.currentUser.getId()); // Preserve the ID
            user.setNom(firstName);
            user.setPrenom(lastName);
            user.setEmail(emailField.getText().trim());
            user.setTel(telField.getText().trim());
            user.setAdresse(addressField.getText().trim());
            user.setRole(role);

            // Update the user in the database
            if (UserDAO.updateUser(user)) {
                showSuccess("User updated successfully.");
                closeWindow();
            } else {
                showError("Failed to update user.");
            }
        } catch (NumberFormatException e) {
            showError("Invalid input for numeric fields.");
        } catch (Exception e) {
            showError("An error occurred: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}