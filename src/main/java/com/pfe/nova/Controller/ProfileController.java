package com.pfe.nova.Controller;
import javafx.scene.input.MouseEvent;
import com.pfe.nova.models.User;
import com.pfe.nova.configuration.UserDAO;
import javafx.fxml.FXML;
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
                showInfo("Profile updated successfully!");
                Stage stage = (Stage) nomField.getScene().getWindow();
                stage.close();
            } else {
                showError("Failed to update profile");
            }
        } catch (Exception e) {
            showError("Error updating profile: " + e.getMessage());
        }
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