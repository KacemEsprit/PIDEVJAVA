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
        // TODO: Implement save functionality
    }
}