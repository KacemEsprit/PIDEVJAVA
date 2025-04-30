package com.pfe.nova.Controller;
import javafx.scene.input.MouseEvent;
import com.pfe.nova.models.User;
import com.pfe.nova.configuration.UserDAO;
import com.pfe.nova.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ProfileController {
    @FXML private ImageView profileImage;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private TextField addressField;
    @FXML private PasswordField passwordField;
    
    private User currentUser;
    private String newImagePath;
    
    public void initData(User user) {
        this.currentUser = user;
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        telField.setText(user.getTel());
        addressField.setText(user.getAdresse());
        
        // Load profile image
        if (user.getPicture() != null && !user.getPicture().isEmpty()) {
            try {
                Image image = new Image(new File(user.getPicture()).toURI().toString());
                profileImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleChangePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(profileImage.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Create directory if it doesn't exist
                Path uploadDir = Paths.get("PIDEVJAVA/src/main/resources/uploads/profiles");
                Files.createDirectories(uploadDir);
                
                // Copy file to uploads directory
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = uploadDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update image view
                Image image = new Image(targetPath.toUri().toString());
                profileImage.setImage(image);
                newImagePath = targetPath.toString();
            } catch (IOException e) {
                showError("Error uploading image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        try {
            currentUser.setNom(nomField.getText());
            currentUser.setPrenom(prenomField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setTel(telField.getText());
            currentUser.setAdresse(addressField.getText());

            if (newImagePath != null) {
                currentUser.setPicture(newImagePath);
            }

            if (!passwordField.getText().isEmpty()) {
                // Hash the new password before saving
                String hashedPassword = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt());
                currentUser.setPassword(hashedPassword);
            }

            if (UserDAO.updateUser(currentUser)) {
                // Reload the user data to refresh the form
                User updatedUser = UserDAO.getUserById(currentUser.getId());
                if (updatedUser != null) {
                    // Update the current user
                    currentUser = updatedUser;
                    
                    // Reload the form with updated data
                    initData(updatedUser);
                    
                    // Update the session user - Make sure this happens BEFORE notifying controllers
                    Session.getInstance().setUtilisateurConnecte(updatedUser);
                    System.out.println("Session updated with user: " + updatedUser.getNom() + " " + updatedUser.getPrenom());
                    
                    // Notify parent controllers about the update
                    notifyParentControllers(updatedUser);
                    
                    // Print debug information
                    System.out.println("Profile updated: " + updatedUser.getNom() + " " + updatedUser.getPrenom());
                    System.out.println("Profile picture: " + updatedUser.getPicture());
                    
                    showInfo("Profile updated successfully!");
                }
            } else {
                showError("Failed to update profile");
            }
        } catch (Exception e) {
            showError("Error updating profile: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Updated notifyParentControllers method with more robust controller finding
    private void notifyParentControllers(User updatedUser) {
        try {
            System.out.println("Starting to notify parent controllers...");
            
            // Get all stages
            for (Stage stage : javafx.stage.Window.getWindows().filtered(window -> window instanceof Stage)
                    .stream()
                    .map(window -> (Stage) window)
                    .toList()) {
                
                System.out.println("Checking stage: " + stage);
                
                // Get the scene and root
                if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                    Parent root = stage.getScene().getRoot();
                    
                    // Try to get controller from user data first
                    Object controller = root.getUserData();
                    System.out.println("Root user data: " + (controller != null ? controller.getClass().getName() : "null"));
                    
                    // Check if the root has an AdminDashboardController
                    if (controller instanceof AdminDashboardController) {
                        AdminDashboardController adminController = (AdminDashboardController) controller;
                        System.out.println("Found AdminDashboardController directly in user data");
                        adminController.initData(updatedUser);
                        return;
                    }
                    
                    // If not found in user data, search in the scene graph
                    AdminDashboardController adminController = findController(root, AdminDashboardController.class);
                    if (adminController != null) {
                        System.out.println("Found AdminDashboardController in scene graph");
                        adminController.initData(updatedUser);
                        System.out.println("AdminDashboardController updated with user: " + updatedUser.getNom());
                        return; // Found and updated, no need to continue
                    }
                    
                    // Check for DashboardController (for other user types)
                    DashboardController dashController = findController(root, DashboardController.class);
                    if (dashController != null) {
                        // Update the dashboard
                        dashController.updateUserInterface(updatedUser);
                        System.out.println("DashboardController found and updated with user: " + updatedUser.getNom());
                        return; // Found and updated, no need to continue
                    }
                }
            }
            System.out.println("No suitable controller found to update");
        } catch (Exception e) {
            System.err.println("Error notifying parent controllers: " + e.getMessage());
            e.printStackTrace();
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
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}