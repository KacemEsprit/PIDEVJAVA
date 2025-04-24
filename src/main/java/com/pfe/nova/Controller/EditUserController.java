package com.pfe.nova.Controller;

import com.pfe.nova.models.*;
import com.pfe.nova.configuration.UserDAO;
import com.pfe.nova.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

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

        String fieldStyle = "-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #e0e0e0; " +
                           "-fx-border-radius: 5; -fx-background-color: #f8f9fa;";

        // Initialize role-specific fields with styling
        specialityField = new TextField();
        specialityField.setPromptText("Speciality");
        specialityField.setStyle(fieldStyle);

        experienceField = new TextField();
        experienceField.setPromptText("Experience");
        experienceField.setStyle(fieldStyle);

        diplomaField = new TextField();
        diplomaField.setPromptText("Diploma");
        diplomaField.setStyle(fieldStyle);

        ageField = new TextField();
        ageField.setPromptText("Age");
        ageField.setStyle(fieldStyle);

        genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("Male", "Female");
        genderComboBox.setPromptText("Gender");
        genderComboBox.setStyle(fieldStyle);

        bloodTypeField = new TextField();
        bloodTypeField.setPromptText("Blood Type");
        bloodTypeField.setStyle(fieldStyle);

        donateurTypeField = new TextField();
        donateurTypeField.setPromptText("Donateur Type");
        donateurTypeField.setStyle(fieldStyle);
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
            
            // Preserve the picture if it exists
            if (currentUser.getPicture() != null && !currentUser.getPicture().isEmpty()) {
                user.setPicture(currentUser.getPicture());
            }

            // Update the user in the database
            if (UserDAO.updateUser(user)) {
                // Update session if the edited user is the current logged-in user
                User sessionUser = Session.getInstance().getUtilisateurConnecte();
                if (sessionUser != null && sessionUser.getId() == user.getId()) {
                    Session.getInstance().setUtilisateurConnecte(user);
                }
                
                // Notify parent controllers about the update
                notifyParentControllers(user);
                
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
    
    // Add this helper method to notify parent controllers
    private void notifyParentControllers(User updatedUser) {
        try {
            // Get all stages
            for (Stage stage : javafx.stage.Window.getWindows().filtered(window -> window instanceof Stage)
                    .stream()
                    .map(window -> (Stage) window)
                    .toList()) {
                
                // Get the scene and root
                if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                    Parent root = stage.getScene().getRoot();
                    
                    // Check if the root has an AdminDashboardController
                    AdminDashboardController adminController = findController(root, AdminDashboardController.class);
                    if (adminController != null) {
                        // Update the admin dashboard
                        adminController.updateSidebarInfo(updatedUser);
                        return; // Found and updated, no need to continue
                    }
                    
                    // Check for DashboardController (for other user types)
                    DashboardController dashController = findController(root, DashboardController.class);
                    if (dashController != null) {
                        // Update the dashboard
                        dashController.updateUserInterface(updatedUser);
                        return; // Found and updated, no need to continue
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error notifying parent controllers: " + e.getMessage());
        }
    }
    
    // Helper method to find a controller in the scene graph
    private <T> T findController(Parent root, Class<T> controllerClass) {
        if (root.getUserData() != null && controllerClass.isInstance(root.getUserData())) {
            return controllerClass.cast(root.getUserData());
        }
        
        // Try to find the controller in children
        for (javafx.scene.Node node : root.getChildrenUnmodifiable()) {
            if (node instanceof Parent) {
                T controller = findController((Parent) node, controllerClass);
                if (controller != null) {
                    return controller;
                }
            }
        }
        
        return null;
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

    @FXML
    private void handleButtonHover(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle(button.getStyle() + "-fx-opacity: 0.9;");
    }

    @FXML
    private void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle(button.getStyle().replace("-fx-opacity: 0.9;", ""));
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }
}