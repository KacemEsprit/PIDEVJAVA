package com.pfe.nova.Controller;

import com.pfe.nova.models.Medication;
import com.pfe.nova.configuration.MedicationDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import java.io.IOException;
import java.sql.SQLException;

public class MedicationController {
    // Remplacer le TableView par un FlowPane pour les cartes
    @FXML private FlowPane medicationCardsPane;
    
    private ObservableList<Medication> medicationList = FXCollections.observableArrayList();
    private Medication selectedMedication;

    @FXML
    public void initialize() {
        loadMedications();
    }

    // Méthode pour créer une carte pour un médicament
    private VBox createMedicationCard(Medication medication) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 10);");
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);
        
        // Image du médicament
        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);
        
        if (medication.getImagePath() != null) {
            try {
                Image image = new Image("file:" + medication.getImagePath(), 120, 120, true, true);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
        
        // Nom du médicament
        Label nameLabel = new Label(medication.getNom());
        nameLabel.setStyle("-fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        
        // Description (limitée)
        Text descriptionText = new Text(medication.getDescription());
        descriptionText.setWrappingWidth(180);
        descriptionText.setStyle("-fx-font-size: 12px;");
        descriptionText.setTextAlignment(TextAlignment.CENTER);

        
        // Prix et quantité
        Label priceLabel = new Label(String.format("Prix: %.2f dt", medication.getPrix()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ecc71;");
        
        Label quantityLabel = new Label(String.format("Quantité en stock: %d", medication.getQuantiteStock()));
        quantityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3498db;");
        
        // Boutons d'action
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        
        Button editButton = new Button("📝");
        editButton.setStyle("-fx-background-color:rgb(4, 216, 216); -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-size: 15px;");
        editButton.setOnAction(e -> selectMedication(medication));
        
        Button deleteButton = new Button("🗑️");
        deleteButton.setStyle("-fx-background-color:rgb(237, 235, 125); -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-size: 15px;");
        deleteButton.setOnAction(e -> handleDelete(medication));
        
        buttonsBox.getChildren().addAll(editButton, deleteButton);
        
        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(imageView, nameLabel, descriptionText, priceLabel, quantityLabel, buttonsBox);
        
        // Ajouter un événement de clic pour sélectionner le médicament
        card.setOnMouseClicked(e -> selectMedication(medication));
        
        return card;
    }
    
    private void selectMedication(Medication medication) {
        selectedMedication = medication;
        handleEdit(medication);
    }

    @FXML
    private void handleAddButton() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/pfe/novaview/medication_dialog.fxml"));
            VBox page = (VBox) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un médicament");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(medicationCardsPane.getScene().getWindow());

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            MedicationDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                try {
                    Medication newMedication = controller.getMedication();
                    MedicationDAO.createMedication(newMedication);
                    loadMedications();
                    showSuccessAlert("Médicament ajouté avec succès");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Erreur de base de données: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'ouverture du dialogue");
        }
    }

    private void handleEdit(Medication medication) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/pfe/novaview/medication_dialog.fxml"));
            VBox page = (VBox) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier un médicament");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(medicationCardsPane.getScene().getWindow());

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            MedicationDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMedication(medication);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                try {
                    boolean success = MedicationDAO.updateMedication(medication);
                    if (success) {
                        loadMedications();
                        showSuccessAlert("Médicament mis à jour avec succès");
                    } else {
                        showErrorAlert("Erreur lors de la mise à jour du médicament");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Erreur de base de données: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'ouverture du dialogue");
        }
    }

    @FXML
    private void handleDelete(Medication medication) {
        try {
            if (MedicationDAO.isUsedInOrder(medication.getId())) {
                showErrorAlert("Ce médicament est utilisé dans une commande et ne peut pas être supprimé");
                return;
            }

            MedicationDAO.deleteMedication(medication.getId());
            showSuccessAlert("Médicament supprimé avec succès");
            loadMedications();
        } catch (Exception e) {
            showErrorAlert("Erreur lors de la suppression: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMedications() {
        try {
            medicationList.clear();
            medicationCardsPane.getChildren().clear();
            
            List<Medication> medications = MedicationDAO.getAllMedications();
            System.out.println("Nombre de médicaments chargés : " + medications.size());
            
            medicationList.addAll(medications);
            
            // Créer et ajouter les cartes pour chaque médicament
            for (Medication med : medicationList) {
                VBox card = createMedicationCard(med);
                medicationCardsPane.getChildren().add(card);
            }
        } catch (Exception e) {
            showErrorAlert("Erreur lors du chargement des médicaments: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}