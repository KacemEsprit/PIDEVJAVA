package com.pfe.nova.Controller;

import com.pfe.nova.configuration.MedicationDAO;
import com.pfe.nova.configuration.OrderDAO;
import com.pfe.nova.models.Medication;
import com.pfe.nova.models.Order;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        if (medication.getImagePath() != null && !medication.getImagePath().isEmpty()) {
            try {
                // Assurez-vous que l'URL est complète
                String imageUrl = medication.getImagePath();
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    imageUrl = "http://localhost/" + imageUrl.replace("\\", "/");
                }
                Image image = new Image(imageUrl, 100, 100, true, true);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                // Charger une image par défaut en cas d'erreur
                try {
                    Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-medication.png"));
                    imageView.setImage(defaultImage);
                } catch (Exception ex) {
                    System.err.println("Impossible de charger l'image par défaut: " + ex.getMessage());
                }
            }
        }
        
        Label nameLabel = new Label(medication.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        Label descriptionLabel = new Label(medication.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setTextAlignment(TextAlignment.CENTER);
        descriptionLabel.setStyle("-fx-font-size: 14px;");
        
        Label priceLabel = new Label(String.format("%.2f dt", medication.getPrix()));
        priceLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");
                
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
        
        card.getChildren().addAll(imageView, nameLabel, descriptionLabel, priceLabel, controlsBox);
        
        return card;
    }
    
    private void loadOrderItems() {
        if (order != null) {
            List<Medication> medications = order.getMedications();
            medicationFlowPane.getChildren().clear();
            
            try {
                List<Medication> allMedications = MedicationDAO.getAllMedications();
                
                Map<Integer, Medication> orderMedications = new HashMap<>();
                for (Medication medication : medications) {
                    orderMedications.put(medication.getId(), medication);
                    originalQuantities.put(medication, medication.getQuantiteCommande());
                }
                
                for (Medication medication : allMedications) {
                    boolean isInOrder = orderMedications.containsKey(medication.getId());
                    if (isInOrder) {
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
        loadOrderItems(); 
    }
    
    private void saveChanges() {
        try {
            for (Medication medication : order.getMedications()) {
                int originalQuantity = originalQuantities.getOrDefault(medication, 0);
                int newQuantity = medication.getQuantiteCommande();
                
                if (originalQuantity != newQuantity) {
                    int stockAdjustment = originalQuantity - newQuantity;
                    medication.setQuantiteStock(medication.getQuantiteStock() + stockAdjustment);
                    MedicationDAO.updateMedication(medication);
                }
            }
            
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