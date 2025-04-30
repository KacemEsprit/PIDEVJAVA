package com.pfe.nova.Controller;
import java.awt.Desktop;
import com.pfe.nova.configuration.MedicationDAO;
import com.pfe.nova.models.Medication;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MedicationController {
    @FXML private FlowPane medicationCardsPane;
    @FXML private TextField searchField;
    @FXML private Button sortButton;
    private boolean isAscendingOrder = true;

    private ObservableList<Medication> medicationList = FXCollections.observableArrayList();
    private ObservableList<Medication> filteredList = FXCollections.observableArrayList();
    private Medication selectedMedication;

    @FXML
    public void initialize() {
        loadMedications();
        setupSortButton();
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.clear();
            String searchTerm = newValue.toLowerCase();
            
            for (Medication med : medicationList) {
                if (med.getNom().toLowerCase().contains(searchTerm) ||
                    med.getDescription().toLowerCase().contains(searchTerm)) {
                    filteredList.add(med);
                }
            }
            
            updateMedicationCards();
        });
    }

    private void setupSortButton() {
        if (sortButton != null) {
            sortButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand;");
            sortButton.setText("Trier par nom ↑");
            sortButton.setOnAction(e -> sortMedications());
        }
    }

    private void sortMedications() {
        isAscendingOrder = !isAscendingOrder;
        if (isAscendingOrder) {
            List<Medication> sortedList = filteredList.stream()
                .sorted(Comparator.comparing(Medication::getNom))
                .collect(Collectors.toList());
            filteredList.setAll(sortedList);
            sortButton.setText("Trier par nom ↑");
        } else {
            List<Medication> sortedList = filteredList.stream()
                .sorted(Comparator.comparing(Medication::getNom).reversed())
                .collect(Collectors.toList());
            filteredList.setAll(sortedList);
            sortButton.setText("Trier par nom ↓");
        }
        updateMedicationCards();
    }
    private VBox createMedicationCard(Medication medication) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 10);");
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);
        
        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);
        
        if (medication.getImagePath() != null) {
            try {
                Image image = new Image(medication.getImagePath(), 120, 120, true, true);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
        
        Label nameLabel = new Label(medication.getNom());
        nameLabel.setStyle("-fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        
        Text descriptionText = new Text(medication.getDescription());
        descriptionText.setWrappingWidth(180);
        descriptionText.setStyle("-fx-font-size: 12px;");
        descriptionText.setTextAlignment(TextAlignment.CENTER);

        
        Label priceLabel = new Label(String.format("Prix: %.2f dt", medication.getPrix()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ecc71;");
        
        Label quantityLabel = new Label(String.format("Quantité en stock: %d", medication.getQuantiteStock()));
        quantityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3498db;");
        
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        
        Button editButton = new Button("📝");
        editButton.setStyle("-fx-background-color:rgb(4, 216, 216); -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-size: 15px;");
        editButton.setOnAction(e -> selectMedication(medication));
        
        Button deleteButton = new Button("🗑️");
        deleteButton.setStyle("-fx-background-color:rgb(237, 235, 125); -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-size: 15px;");
        deleteButton.setOnAction(e -> handleDelete(medication));
        
        buttonsBox.getChildren().addAll(editButton, deleteButton);
        
        card.getChildren().addAll(imageView, nameLabel, descriptionText, priceLabel, quantityLabel, buttonsBox);
        
        card.setOnMouseClicked(e -> selectMedication(medication));

        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#aaaaaa"));
        card.setOnMouseEntered(e -> {
            card.setEffect(shadow);
        });
        card.setOnMouseExited(e -> {
            card.setEffect(null);
        });
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
            List<Medication> medications = MedicationDAO.getAllMedications();
            System.out.println("Nombre de médicaments chargés : " + medications.size());
            medicationList.addAll(medications);
            filteredList.setAll(medicationList);
            updateMedicationCards();
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
    
    private void updateMedicationCards() {
        medicationCardsPane.getChildren().clear();
        for (Medication med : filteredList) {
            VBox card = createMedicationCard(med);
            medicationCardsPane.getChildren().add(card);
        }
    }

    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier Excel");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
        );
        
        File selectedFile = fileChooser.showOpenDialog(medicationCardsPane.getScene().getWindow());
        if (selectedFile != null) {
            try {
                ExcelController excelController = new ExcelController();
                List<Medication> importedMedications = excelController.importMedicationsFromExcel(selectedFile.getAbsolutePath());
                
                for (Medication med : importedMedications) {
                    try {
                        MedicationDAO.createMedication(med);
                    } catch (SQLException e) {
                        showErrorAlert("Erreur lors de l'import du médicament " + med.getNom() + ": " + e.getMessage());
                    }
                }
                
                loadMedications();
                showSuccessAlert("Import réussi !");
            } catch (Exception e) {
                showErrorAlert("Erreur lors de l'import: " + e.getMessage());
            }
        }
    }
    @FXML
    private void handleExport() {
        try {
            ExcelController excelController = new ExcelController();
            
            // Modification ici : récupérer le nom du fichier généré
            String fileName = excelController.exportMedicationsToExcel(new ArrayList<>(medicationList));
            
            // Ouvre automatiquement le fichier Excel
            if (fileName != null) {
                File file = new File(fileName);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                }
            }
        } catch (Exception e) {
            showErrorAlert("Erreur lors de l'export : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
