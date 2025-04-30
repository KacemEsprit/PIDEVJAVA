package com.pfe.nova.Controller;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.pfe.nova.configuration.OrderDAO;
import com.pfe.nova.models.Order;
import com.pfe.nova.models.Medication;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderConfirmationController {
    @FXML
    private VBox ordersContainer;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        loadPendingOrders();
    }

    private void loadPendingOrders() {
        try {
            List<Order> pendingOrders = OrderDAO.getPendingOrders();
            ordersContainer.getChildren().clear();
            
            for (Order order : pendingOrders) {
                ordersContainer.getChildren().add(createOrderCard(order));
                ordersContainer.getChildren().add(new Separator());
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des commandes", e.getMessage());
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(15);  // Augmentation de l'espacement
        card.setStyle(
            "-fx-background-color:rgb(183, 230, 252);" +
            "-fx-border-color:rgb(78, 135, 250);" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);"
        );
        
        
        // En-tête de la commande avec style amélioré
        Label dateLabel = new Label(dateFormatter.format(order.getDate()));
        Label orderIdLabel = new Label("Commande N°" + order.getId());
        Label clientLabel = new Label("Client: " + order.getUser().getNom() + " " + order.getUser().getPrenom());
        
        // Style des labels amélioré
        orderIdLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2f2f2f;");
clientLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");

        
        // Liste des médicaments avec style amélioré
        VBox medicationsBox = new VBox(8);
        medicationsBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-padding: 15; -fx-background-radius: 5;");
        
        for (Medication med : order.getMedications()) {
            HBox medRow = new HBox(15);
            medRow.setAlignment(Pos.CENTER_LEFT);
            
            Label medName = new Label(med.getNom());
            Label quantity = new Label("×" + med.getQuantiteCommande());
            Label price = new Label(String.format("%.2f DT", med.getPrix() * med.getQuantiteCommande()));
            
            // Style amélioré pour les informations des médicaments
            medName.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
quantity.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
price.setStyle("-fx-font-size: 14px; -fx-text-fill: #000; -fx-font-weight: bold;");

            
            medRow.getChildren().addAll(medName, quantity, price);
            medicationsBox.getChildren().add(medRow);
        }
        
        // Séparateurs stylisés
        Separator topSeparator = new Separator();
        Separator bottomSeparator = new Separator();
        topSeparator.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
        bottomSeparator.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
        
        // Total avec style amélioré
        Label totalLabel = new Label(String.format("Total: %.2f DT", order.getMontantTotal()));
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; " +
                          "-fx-font-family: 'Segoe UI'; -fx-padding: 10 0 10 0;");
        
        // Boutons d'action avec style amélioré
        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button validateButton = new Button("Valider ✓");
        validateButton.setStyle("-fx-background-color:rgb(39, 171, 197); -fx-text-fill: white; " +
        "-fx-font-size: 13px; -fx-padding: 8 18; -fx-background-radius: 8;" +
        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;");
        
        Button rejectButton = new Button("Rejeter ✗");
        rejectButton.setStyle("-fx-background-color:rgb(244, 130, 118); -fx-text-fill: white; " +
        "-fx-font-size: 13px; -fx-padding: 8 18; -fx-background-radius: 8;" +
        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;");
        
        // Ajout des effets hover sur les boutons
        validateButton.setOnMouseEntered(e -> validateButton.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; " +
                "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 3);"));
        
        validateButton.setOnMouseExited(e -> validateButton.setStyle("-fx-background-color: #2962FF; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; " +
                "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 2);"));
        
        rejectButton.setOnMouseEntered(e -> rejectButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; " +
                "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 3);"));
        
        rejectButton.setOnMouseExited(e -> rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; " +
                "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 2);"));
        
        validateButton.setOnAction(e -> handleValidateOrder(order));
        rejectButton.setOnAction(e -> handleRejectOrder(order));
        
        buttonsBox.getChildren().addAll(validateButton, rejectButton);
        
        card.getChildren().addAll(
            dateLabel, 
            orderIdLabel,
            clientLabel,
            topSeparator,
            medicationsBox,
            bottomSeparator,
            totalLabel,
            buttonsBox
        );
        
        return card;
    }

    private void handleValidateOrder(Order order) {
        try {
            OrderDAO.updateOrderStatus(order.getId(), "Validée");
            MailingConfirmationController.sendOrderConfirmationEmail(order);
            showSuccess("Commande validée avec succès");
            loadPendingOrders();
        } catch (SQLException e) {
            showError("Erreur lors de la validation", e.getMessage());
        }
    }

    private void handleRejectOrder(Order order) {
        try {
            OrderDAO.updateOrderStatus(order.getId(), "Rejetée");
    
            // Numéro fixe
            String fixedPhone = "+21620490440"; // Numéro toujours utilisé
            String message = "Votre commande #" + order.getId() + " a été annulée d'après la pharmacie. Contactez-nous pour plus d'informations.";
            
            sendSMS(fixedPhone, message);
    
            showSuccess("Commande rejetée avec succès");
            loadPendingOrders();
        } catch (SQLException e) {
            showError("Erreur lors du rejet", e.getMessage());
        }
    }
    

    private void sendSMS(String phoneNumber, String message) {
        try {
            // Configuration Twilio
            String ACCOUNT_SID = "ACeb37fffb1d2b08142887a0b27c513fcc";
            String AUTH_TOKEN = "6fc709ba6dfb32ec0433bc718ceda617";
            String TWILIO_PHONE = "+15076237294";
            
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(TWILIO_PHONE),
                message
            ).create();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du SMS : " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}