package com.pfe.nova.Controller;

import com.pfe.nova.configuration.MedicationDAO;
import com.pfe.nova.configuration.OrderDAO;
import com.pfe.nova.models.Medication;
import com.pfe.nova.models.Order;
import com.pfe.nova.models.Patient;
import com.pfe.nova.models.User;
import com.pfe.nova.utils.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CartController {
    @FXML private VBox cartItemsContainer;
    @FXML private Label totalLabel;
    @FXML private Button closeButton;
    
    private OrderController orderController;
    
    private Map<Medication, Integer> cartItems = new HashMap<>();
    private double total = 0.0;
    
    @FXML
    public void initialize() {
        updateCartView();
    }
    
    @FXML
    public void handleCancelOrder() {
        cartItems.clear();
        updateCartView();
        orderController.clearCart();
    }
    
    public void addToCart(Medication medication, int quantity) {
        cartItems.merge(medication, quantity, Integer::sum);
        updateCartView();
    }
    
    private void updateCartView() {
        cartItemsContainer.getChildren().clear();
        total = 0.0;
        
        for (Map.Entry<Medication, Integer> entry : cartItems.entrySet()) {
            Medication medication = entry.getKey();
            Integer quantity = entry.getValue();
            
            HBox itemContainer = createCartItemView(medication, quantity);
            cartItemsContainer.getChildren().add(itemContainer);
            
            total += medication.getPrix() * quantity;
        }
        
        totalLabel.setText(String.format("%.2f dt", total));
    }
    
    private HBox createCartItemView(Medication medication, int quantity) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        
        // Image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);
        
        if (medication.getImagePath() != null && !medication.getImagePath().isEmpty()) {
            try {
                // Utiliser l'URL complète pour charger l'image
                String imageUrl = medication.getImagePath();
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    imageUrl = "http://localhost/" + imageUrl.replace("\\", "/");
                }
                Image image = new Image(imageUrl, 50, 50, true, true);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Erreur de chargement d'image: " + e.getMessage());
                // Charger une image par défaut en cas d'erreur
                try {
                    Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-medication.png"));
                    imageView.setImage(defaultImage);
                } catch (Exception ex) {
                    System.err.println("Impossible de charger l'image par défaut: " + ex.getMessage());
                }
            }
        } else {
            // Charger une image par défaut si aucune image n'est spécifiée
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-medication.png"));
                imageView.setImage(defaultImage);
            } catch (Exception e) {
                System.err.println("Impossible de charger l'image par défaut: " + e.getMessage());
            }
        }

        VBox infoContainer = new VBox(5);
        Label nameLabel = new Label(medication.getNom());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label priceLabel = new Label(String.format("%.2f dt", medication.getPrix()));
        infoContainer.getChildren().addAll(nameLabel, priceLabel);
        
        Spinner<Integer> quantitySpinner = new Spinner<>(1, medication.getQuantiteStock(), quantity);
        quantitySpinner.setEditable(true);
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            cartItems.put(medication, newVal);
            updateCartView();
        });
        
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteButton.setOnAction(e -> {
            cartItems.remove(medication);
            updateCartView();
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        container.getChildren().addAll(imageView, infoContainer, spacer, quantitySpinner, deleteButton);
        return container;
    }
    
    @FXML
    private void handleClose() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
    
    public void setOrderController(OrderController controller) {
        this.orderController = controller;
        
        User currentUser = Session.getUtilisateurConnecte();
        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            if (orderController != null) {
                orderController.initializeForPatient(patient.getId());
            }
        } else {
            showErrorAlert("Accès non autorisé. Seuls les patients peuvent accéder au panier.");
            handleClose();
        }
    }
    
    public void updateCartItems(Map<Medication, Integer> items) {
        this.cartItems = new HashMap<>(items);
        updateCartView();
    }
    
    @FXML
    private void handleViewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/order_history.fxml"));
            Parent orderHistoryView = loader.load();
            Stage orderHistoryStage = new Stage();
            orderHistoryStage.setTitle("Historique des Commandes");
            orderHistoryStage.setScene(new Scene(orderHistoryView));
            orderHistoryStage.initModality(Modality.APPLICATION_MODAL);
            orderHistoryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'affichage de l'historique des commandes: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showErrorAlert("Votre panier est vide");
            return;
        }
        
        try {
            MedicationDAO medicationDAO = new MedicationDAO();
            for (Map.Entry<Medication, Integer> entry : cartItems.entrySet()) {
                Medication med = entry.getKey();
                int requestedQuantity = entry.getValue();
                int availableStock = med.getQuantiteStock();
                
                if (requestedQuantity > availableStock) {
                    showErrorAlert("Stock insuffisant pour " + med.getNom() + ". Stock disponible: " + availableStock);
                    return;
                }
            }
            
            Order newOrder = new Order("En cours");
            newOrder.setUser(Session.getUtilisateurConnecte());
            double totalAmount = 0.0;
            int totalQuantity = 0;
            
            for (Map.Entry<Medication, Integer> entry : cartItems.entrySet()) {
                Medication med = entry.getKey();
                int quantity = entry.getValue();
                
                med.setQuantiteStock(med.getQuantiteStock() - quantity);
                medicationDAO.updateMedication(med);
                
                newOrder.addMedication(med, quantity);
                
                totalAmount += med.getPrix() * quantity;
                totalQuantity += quantity;
            }
            
            newOrder.setMontantTotal(totalAmount);
            newOrder.setQuantiteTotal(totalQuantity);
            
            int orderId = OrderDAO.createOrder(newOrder);
            
            orderController.clearCart();
            cartItems.clear();
            updateCartView();
            
            handleClose();
            
            if (orderId > 0) {
                cartItems.clear();
                updateCartView();
                if (orderController != null) {
                    orderController.updateCartCount(0);
                    orderController.saveCartToFile(); 
                }
                
                showInfoAlert("Commande n°" + orderId + " effectuée avec succès!");
                handleClose();
            } else {
                throw new Exception("Erreur lors de la création de la commande");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de la création de la commande: " + e.getMessage());
        }
    }
    
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}