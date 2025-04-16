package com.pfe.nova.Controller;

import com.pfe.nova.models.Order;
import com.pfe.nova.models.Medication;
import com.pfe.nova.configuration.OrderDAO;
import com.pfe.nova.configuration.MedicationDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.TextAlignment;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;

public class ModifyOrderController {
    @FXML private FlowPane medicationFlowPane;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private ScrollPane scrollPane;
    
    private Order order;
    private Map<Medication, Integer> originalQuantities = new HashMap<>();
    
    @FXML
    public void initialize() {
        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);
            scrollPane.setContent(medicationFlowPane);
        }
        
        saveButton.setOnAction(e -> saveChanges());
        cancelButton.setOnAction(e -> closeWindow());
        
        try {
            loadAllMedications();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des médicaments", e.getMessage());
        }
    }
    
    public void setOrder(Order order) {
        this.order = order;
        loadOrderItems();
    }
    
    private void loadAllMedications() throws SQLException {
        List<Medication> allMedications = MedicationDAO.getAllMedications();
        medicationFlowPane.getChildren().clear();
        
        for (Medication medication : allMedications) {
            VBox card = createMedicationCard(medication, false);
            medicationFlowPane.getChildren().add(card);
        }
    }
    
    private VBox createMedicationCard(Medication medication, boolean isInOrder) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 10);");
        card.setPrefWidth(180);
        card.setAlignment(Pos.CENTER);
        
        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        
        if (medication.getImagePath() != null) {
            try {
                Image image = new Image("file:" + medication.getImagePath(), 100, 100, true, true);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
        
        Label nameLabel = new Label(medication.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        
        Label priceLabel = new Label(String.format("Prix: %.2f dt", medication.getPrix()));
        
        Label stockLabel = new Label(String.format("En stock: %d", medication.getQuantiteStock()));
        
        Button actionButton;
        if (isInOrder) {
            int currentQuantity = medication.getQuantiteCommande();
            int maxQuantity = medication.getQuantiteStock() + currentQuantity;
            
            Spinner<Integer> quantitySpinner = new Spinner<>(1, maxQuantity, currentQuantity);
            quantitySpinner.setEditable(true);
            quantitySpinner.setPrefWidth(80);
            quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue) && newValue > 0 && newValue <= maxQuantity) {
                    medication.setQuantiteCommande(newValue);
                    order.updateMedicationQuantity(medication, newValue);
                } else if (newValue != null && (newValue <= 0 || newValue > maxQuantity)) {
                    quantitySpinner.getValueFactory().setValue(oldValue);
                }
            });
            
            actionButton = new Button("Retirer ❌");
            actionButton.setStyle("-fx-background-color:rgb(206, 37, 37); -fx-text-fill: white; -fx-background-radius: 5;");
            actionButton.setPrefWidth(120);
            actionButton.setOnAction(e -> removeItemFromOrder(medication));
        } else {
            actionButton = new Button("Ajouter");
            actionButton.setStyle("-fx-background-color:rgb(4, 214, 251); -fx-text-fill: white; -fx-background-radius: 5;");
            actionButton.setPrefWidth(120);
            actionButton.setOnAction(e -> addItemToOrder(medication));
        }
        
        HBox controlsBox = new HBox(5);
        controlsBox.setAlignment(Pos.CENTER);
        if (isInOrder) {
            int currentQuantity = medication.getQuantiteCommande();
            int maxQuantity = medication.getQuantiteStock() + currentQuantity;
            Spinner<Integer> quantitySpinner = new Spinner<>(1, maxQuantity, currentQuantity);
            quantitySpinner.setEditable(true);
            quantitySpinner.setPrefWidth(80);
            quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue) && newValue > 0 && newValue <= maxQuantity) {
                    medication.setQuantiteCommande(newValue);
                    order.updateMedicationQuantity(medication, newValue);
                } else if (newValue != null && (newValue <= 0 || newValue > maxQuantity)) {
                    quantitySpinner.getValueFactory().setValue(oldValue);
                }
            });
            controlsBox.getChildren().addAll(quantitySpinner, actionButton);
        } else {
            controlsBox.getChildren().add(actionButton);
        }
        
        card.getChildren().addAll(imageView, nameLabel, priceLabel, stockLabel, controlsBox);
        
        return card;
    }
    
    private void loadOrderItems() {
        if (order != null) {
            List<Medication> medications = order.getMedications();
            medicationFlowPane.getChildren().clear();
            
            try {
                // Charger d'abord tous les médicaments disponibles
                List<Medication> allMedications = MedicationDAO.getAllMedications();
                
                // Créer une carte pour les médicaments dans la commande
                Map<Integer, Medication> orderMedications = new HashMap<>();
                for (Medication medication : medications) {
                    orderMedications.put(medication.getId(), medication);
                    originalQuantities.put(medication, medication.getQuantiteCommande());
                }
                
                // Afficher tous les médicaments avec leur état approprié
                for (Medication medication : allMedications) {
                    boolean isInOrder = orderMedications.containsKey(medication.getId());
                    if (isInOrder) {
                        // Utiliser le médicament de la commande pour conserver la quantité
                        VBox card = createMedicationCard(orderMedications.get(medication.getId()), true);
                        medicationFlowPane.getChildren().add(card);
                    } else {
                        VBox card = createMedicationCard(medication, false);
                        medicationFlowPane.getChildren().add(card);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur lors du chargement des médicaments", e.getMessage());
            }
        }
    }
    
    private void removeItemFromOrder(Medication medication) {
        order.getMedications().remove(medication);
        loadOrderItems(); // Refresh the display
    }
    
    private void saveChanges() {
        try {
            // Update stock for each medication
            for (Medication medication : order.getMedications()) {
                int originalQuantity = originalQuantities.getOrDefault(medication, 0);
                int newQuantity = medication.getQuantiteCommande();
                
                // Adjust stock based on quantity change
                if (originalQuantity != newQuantity) {
                    int stockAdjustment = originalQuantity - newQuantity;
                    medication.setQuantiteStock(medication.getQuantiteStock() + stockAdjustment);
                    MedicationDAO.updateMedication(medication);
                }
            }
            
            // Update the order in the database
            OrderDAO.updateOrder(order);
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "La commande a été mise à jour avec succès.");
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour de la commande: " + e.getMessage());
        }
    }
    
    private void addItemToOrder(Medication medication) {
        if (medication.getQuantiteStock() > 0) {
            if (!order.getMedications().contains(medication)) {
                medication.setQuantiteCommande(1);
                order.addMedication(medication, 1);
                loadOrderItems();
            } else {
                showError("Médicament déjà présent", "Ce médicament est déjà dans la commande.");
            }
        } else {
            showError("Stock insuffisant", "Ce médicament n'est plus disponible en stock.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}