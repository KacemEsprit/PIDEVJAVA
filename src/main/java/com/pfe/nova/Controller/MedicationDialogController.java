package com.pfe.nova.Controller;

import com.pfe.nova.models.Medication;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class MedicationDialogController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private ImageView medicationImageView;
    
    private Stage dialogStage;
    private Medication medication;
    private boolean saveClicked = false;
    private String selectedImagePath;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setMedication(Medication medication) {
        this.medication = medication;
        
        if (medication != null) {
            nameField.setText(medication.getNom());
            descriptionField.setText(medication.getDescription());
            quantityField.setText(String.valueOf(medication.getQuantiteStock()));
            priceField.setText(String.valueOf(medication.getPrix()));
            
            if (medication.getImagePath() != null) {
                selectedImagePath = medication.getImagePath();
                try {
                    Image image = new Image("file:" + medication.getImagePath());
                    medicationImageView.setImage(image);
                } catch (Exception e) {
                    medicationImageView.setImage(null);
                }
            }
        }
    }
    
    public boolean isSaveClicked() {
        return saveClicked;
    }
    
    public Medication getMedication() {
        return medication;
    }
    
    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                // Créer le répertoire de destination s'il n'existe pas
                File destDir = new File("C:\\xampp\\htdocs\\img");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String newFileName = UUID.randomUUID().toString() + extension;
                File destFile = new File(destDir, newFileName);
                
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // Construire l'URL au lieu du chemin local
                selectedImagePath = "http://localhost/img/" + newFileName;
                Image image = new Image(selectedImagePath);
                medicationImageView.setImage(image);
                
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur lors de la copie de l'image");
                alert.setContentText("Une erreur est survenue lors de la copie de l'image : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            if (medication == null) {
                medication = new Medication();
            }
            
            medication.setNom(nameField.getText());
            medication.setDescription(descriptionField.getText());
            medication.setQuantiteStock(Integer.parseInt(quantityField.getText()));
            medication.setPrix(Double.parseDouble(priceField.getText()));
            medication.setImagePath(selectedImagePath);
            
            saveClicked = true;
            dialogStage.close();
        }
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private boolean isInputValid() {
        String errorMessage = "";
        
        if (nameField.getText() == null || nameField.getText().trim().isEmpty() || nameField.getText().trim().length() < 4) {
            errorMessage += "Le nom doit contenir au moins 4 caractères\n";
        }
        
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty() || descriptionField.getText().trim().length() < 6) {
            errorMessage += "La description doit contenir au moins 6 caractères\n";
        }
        
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                errorMessage += "La quantité doit être supérieure à 0\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "La quantité doit être un nombre entier valide\n";
        }
        
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                errorMessage += "Le prix doit être supérieur à 0\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "Le prix doit être un nombre valide\n";
        }
        
        if (selectedImagePath == null || selectedImagePath.trim().isEmpty()) {
            errorMessage += "Une image est requise\n";
        }
        
        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Champs invalides");
            alert.setHeaderText("Veuillez corriger les champs invalides");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}