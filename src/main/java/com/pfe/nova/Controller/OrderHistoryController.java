package com.pfe.nova.Controller;

import com.pfe.nova.models.Order;
import com.pfe.nova.configuration.OrderDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.shape.Circle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.sql.SQLException;

// Add missing imports at the top
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import java.io.IOException;
import java.util.Optional;
import com.pfe.nova.models.Medication;
import com.pfe.nova.configuration.MedicationDAO;

// Add missing import
import javafx.stage.Modality;
import javafx.scene.Scene;

public class OrderHistoryController {
    @FXML private ScrollPane scrollPane;
    @FXML private VBox ordersContainer;
    @FXML private Button closeButton;
    @FXML private Button refreshButton;
    
    private final OrderDAO orderDAO = new OrderDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    @FXML
    public void initialize() {
        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);
            
            if (ordersContainer == null) {
                ordersContainer = new VBox(10);
                ordersContainer.setPadding(new Insets(10));
                ordersContainer.getStyleClass().add("orders-container");
            }
            
            scrollPane.setContent(ordersContainer);
            loadOrders();
        }
    }
    
    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("order-card");
        
        // En-tête avec Date et ID
        HBox header = new HBox();
        header.getStyleClass().add("order-header");
        header.setSpacing(15);
        Text dateText = new Text(dateFormatter.format(order.getDate()));
        Text idText = new Text("Commande #" + order.getId());
        header.getChildren().addAll(dateText, new Separator(Orientation.VERTICAL), idText);
        
        // Articles
        VBox itemsContainer = new VBox(5);
        itemsContainer.getStyleClass().add("order-items");
        Text itemsTitle = new Text("Articles:");
        Text itemsText = new Text(order.getItemsSummary());
        itemsContainer.getChildren().addAll(itemsTitle, itemsText);
        
        // Informations détaillées
        VBox detailsContainer = new VBox(5);
        detailsContainer.getStyleClass().add("order-footer");
        
        // Total et Status dans des conteneurs séparés
        HBox statusContainer = new HBox(10);
        Circle statusIndicator = new Circle(5);
        statusIndicator.setStyle(getStatusColor(order.getStatus()));
        Text statusText = new Text("Status: " + order.getStatus());
        statusContainer.getChildren().addAll(statusIndicator, statusText);
        
        Text totalText = new Text(String.format("Total: %.2f dt", order.getMontantTotal()));
        
        detailsContainer.getChildren().addAll(statusContainer, totalText);
        
        // Assemblage final de la carte
        card.getChildren().addAll(
            header,
            new Separator(),
            itemsContainer,
            new Separator(),
            detailsContainer
        );
        
        // Add modify and delete buttons for orders in progress
        if (order.getStatus().equals("En cours")) {
            HBox buttonsBox = new HBox(10);
            buttonsBox.setAlignment(Pos.CENTER);
            
            Button modifyButton = new Button("Modifier 📝");
            modifyButton.setStyle("-fx-background-color:rgb(4, 216, 216); -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-size: 15px;");

            Button deleteButton = new Button("Annuler ❌");
            deleteButton.setStyle("-fx-background-color:rgb(202, 24, 24); -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-size: 15px;");

            modifyButton.setOnAction(e -> modifyOrder(order));
            deleteButton.setOnAction(e -> deleteOrder(order));
            
            buttonsBox.getChildren().addAll(modifyButton, deleteButton);
            card.getChildren().add(buttonsBox);
        }
        
        return card;
    }
    
    private String getStatusColor(String status) {
        return switch (status.toLowerCase()) {
            case "en cours" -> "-fx-fill: #f39c12";
            case "terminée" -> "-fx-fill: #2ecc71";
            case "annulée" -> "-fx-fill: #e74c3c";
            default -> "-fx-fill: #95a5a6";
        };
    }
    
    @FXML
    private void loadOrders() {
        if (ordersContainer == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'initialisation de l'interface");
            return;
        }
        
        ordersContainer.getChildren().clear();
        showLoadingIndicator();
        
        try {
            List<Order> orders = orderDAO.getAllOrders();
            if (orders == null) {
                handleLoadError(new Exception("Erreur de récupération des commandes"));
                return;
            }
            
            if (orders.isEmpty()) {
                showEmptyState();
            } else {
                displayOrders(orders);
            }
        } catch (SQLException e) {
            handleLoadError(new Exception("Erreur de connexion à la base de données: " + e.getMessage()));
        } catch (Exception e) {
            handleLoadError(new Exception("Erreur inattendue: " + e.getMessage()));
        }
    }
    
    private void showLoadingIndicator() {
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);
        VBox loadingContainer = new VBox(loadingIndicator);
        loadingContainer.setAlignment(javafx.geometry.Pos.CENTER);
        ordersContainer.getChildren().add(loadingContainer);
    }
    
    private void showEmptyState() {
        VBox placeholder = new VBox();
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setSpacing(15);
        
        Label noOrdersLabel = new Label("Aucune commande trouvée");
        noOrdersLabel.getStyleClass().add("empty-state-label");
        
        Button refreshButton = new Button("Actualiser");
        refreshButton.setOnAction(e -> handleRefresh());
        refreshButton.getStyleClass().add("refresh-button");
        
        placeholder.getChildren().addAll(noOrdersLabel, refreshButton);
        ordersContainer.getChildren().setAll(placeholder);
    }
    
    private void displayOrders(List<Order> orders) {
        ordersContainer.getChildren().clear();
        orders.stream()
              .map(this::createOrderCard)
              .forEach(card -> ordersContainer.getChildren().add(card));
    }
    
    private void handleLoadError(Exception e) {
        ordersContainer.getChildren().clear();
        
        VBox errorContainer = new VBox();
        errorContainer.setAlignment(javafx.geometry.Pos.CENTER);
        errorContainer.setSpacing(15);
        
        Label errorLabel = new Label("Erreur de chargement des commandes");
        errorLabel.getStyleClass().add("error-label");
        
        Label errorDetails = new Label(e.getMessage());
        errorDetails.getStyleClass().add("error-details");
        
        Button retryButton = new Button("Réessayer");
        retryButton.setOnAction(event -> handleRefresh());
        retryButton.getStyleClass().add("retry-button");
        
        errorContainer.getChildren().addAll(errorLabel, errorDetails, retryButton);
        ordersContainer.getChildren().add(errorContainer);
        
        e.printStackTrace();
    }
    
    @FXML
    private void handleRefresh() {
        refreshButton.setDisable(true);
        loadOrders();
        refreshButton.setDisable(false);
    }
    
    @FXML
    private void handleClose() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void modifyOrder(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/modify_order.fxml"));
            Parent root = loader.load();
            
            ModifyOrderController controller = loader.getController();
            controller.setOrder(order);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier la commande");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadOrders();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la fenêtre de modification.");
        }
    }

    private void deleteOrder(Order order) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer la commande");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette commande ?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (Medication medication : order.getMedications()) {
                    medication.setQuantiteStock(medication.getQuantiteStock() + medication.getQuantiteCommande());
                    medicationDAO.updateMedication(medication);
                }
                
                orderDAO.deleteOrder(order.getId());
                loadOrders();
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", "La commande a été supprimée avec succès.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la commande: " + e.getMessage());
            }
        }
    }
}