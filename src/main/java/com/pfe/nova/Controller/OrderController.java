package com.pfe.nova.Controller;

import com.pfe.nova.models.Order;
import com.pfe.nova.models.Medication;
import com.pfe.nova.configuration.OrderDAO;
import com.pfe.nova.configuration.MedicationDAO;
import javafx.application.Platform;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.TextAlignment;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Text;

import java.util.List;
import java.time.format.DateTimeFormatter;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class OrderController {
    @FXML private FlowPane medicationFlowPane;
    @FXML private Button cartButton;
    @FXML private Label cartCountLabel;
    @FXML private TextField searchField;
    
    private List<Medication> allMedications;
    
    private CartController cartController;
    private Stage cartStage;
    private Map<Medication, Integer> cartItems = new HashMap<>();
    private int cartItemCount = 0;
    
    @FXML
    private Button sortButton;
    private boolean isAscendingOrder = true;

    @FXML
    public void initialize() {
        setupCartButton();
        setupSearchField();
        setupSortButton();
        medicationFlowPane.setAlignment(Pos.CENTER);
        medicationFlowPane.setHgap(20);
        medicationFlowPane.setVgap(20);
        medicationFlowPane.setPadding(new Insets(20));
        loadMedicationsToFlowPane();
        initializeCartView();
        loadCartFromFile();
        updateCartCount();
    }

    private void setupSortButton() {
        if (sortButton != null) {
            sortButton.setOnAction(e -> sortMedications());
        }
    }

    private void sortMedications() {
        isAscendingOrder = !isAscendingOrder;
        if (isAscendingOrder) {
            allMedications.sort((m1, m2) -> m1.getNom().compareToIgnoreCase(m2.getNom()));
            sortButton.setText("Trier par nom ↑");
        } else {
            allMedications.sort((m1, m2) -> m2.getNom().compareToIgnoreCase(m1.getNom()));
            sortButton.setText("Trier par nom ↓");
        }
        displayMedications(allMedications);
    }
    
    private void setupCartButton() {
        if (cartButton != null) {
            cartButton.setOnAction(e -> showCartView());
        }
    }
    
    private void setupSearchField() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterMedications(newValue);
            });
        }
    }
    
    private void filterMedications(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayMedications(allMedications);
            return;
        }
        
        List<Medication> filteredList = allMedications.stream()
            .filter(med -> med.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                    med.getDescription().toLowerCase().contains(searchText.toLowerCase()))
            .collect(java.util.stream.Collectors.toList());
        
        displayMedications(filteredList);
    }
    
    private void displayMedications(List<Medication> medications) {
        medicationFlowPane.getChildren().clear();
        for (Medication medication : medications) {
            VBox card = createMedicationCard(medication);
            medicationFlowPane.getChildren().add(card);
        }
    }
    
    private void initializeCartView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/cart_view.fxml"));
            Parent cartView = loader.load();
            cartController = loader.getController();
            cartController.setOrderController(this);
            
            cartStage = new Stage();
            cartStage.setTitle("Mon Panier");
            cartStage.setScene(new Scene(cartView));
            cartStage.initModality(Modality.APPLICATION_MODAL);
            cartStage.setOnCloseRequest(e -> cartController.updateCartItems(cartItems));
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'initialisation du panier: " + e.getMessage());
        }
    }
    
    private void showCartView() {
        if (cartStage != null) {
            cartController.updateCartItems(cartItems);
            cartStage.show();
        }
    }
    
    // Add these methods for cart persistence
    public void saveCartToFile() {
        try {
            java.io.File cartFile = new java.io.File("cart_data.ser");
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
                    new java.io.FileOutputStream(cartFile));
            out.writeObject(cartItems);
            out.writeInt(cartItemCount);
            out.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde du panier: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadCartFromFile() {
        try {
            java.io.File cartFile = new java.io.File("cart_data.ser");
            if (cartFile.exists()) {
                java.io.ObjectInputStream in = new java.io.ObjectInputStream(
                        new java.io.FileInputStream(cartFile));
                cartItems = (Map<Medication, Integer>) in.readObject();
                cartItemCount = in.readInt();
                in.close();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du panier: " + e.getMessage());
            e.printStackTrace();
            // If there's an error, start with an empty cart
            cartItems = new HashMap<>();
            cartItemCount = 0;
        }
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
                Image image = new Image("file:" + medication.getImagePath(), 120, 120, true, true);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
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
        
        Spinner<Integer> quantitySpinner = new Spinner<>(1, medication.getQuantiteStock(), 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.setPrefWidth(80);
        quantitySpinner.getValueFactory().setWrapAround(false);
        
        Button addButton = new Button("Ajouter");
        addButton.setStyle("-fx-background-color:rgb(5, 234, 234); -fx-text-fill: white; -fx-background-radius: 5;");
        addButton.setPrefWidth(120);
        addButton.setTextAlignment(TextAlignment.CENTER);
        
        addButton.setOnAction(e -> {
            if (medication.getQuantiteStock() <= 0) {
                showErrorAlert("Stock insuffisant pour " + medication.getNom());
                return;
            }
            
            int quantite = quantitySpinner.getValue();
            if (quantite > medication.getQuantiteStock()) {
                showErrorAlert("Quantité demandée supérieure au stock disponible");
                return;
            }
            
            medication.setQuantiteCommande(quantite);
            cartItems.put(medication, quantite);
            cartItemCount += quantite;
            updateCartCount();
            saveCartToFile();
        });
        
        HBox controlsBox = new HBox(5);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.getChildren().addAll(quantitySpinner, addButton);
        
        card.getChildren().addAll(imageView, nameLabel, descriptionLabel, priceLabel, controlsBox);
        
        return card;
    }
    
    // In your initialize method, add this after loading orders
    private void loadMedicationsToFlowPane() {
        try {
            medicationFlowPane.getChildren().clear();
            allMedications = MedicationDAO.getAllMedications();
            
            for (Medication medication : allMedications) {
                VBox card = createMedicationCard(medication);
                medicationFlowPane.getChildren().add(card);
            }
        } catch (Exception e) {
            showErrorAlert("Erreur lors du chargement des médicaments: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void updateCartCount(int count) {
        cartItemCount = count;
        Platform.runLater(() -> cartCountLabel.setText(String.valueOf(cartItemCount)));
    }

    private void updateCartCount() {
        Platform.runLater(() -> cartCountLabel.setText(String.valueOf(cartItemCount)));
    }
    
    private void updateMedicationStock() {
        try {
            for (Map.Entry<Medication, Integer> entry : cartItems.entrySet()) {
                Medication medication = entry.getKey();
                int quantity = entry.getValue();
                medication.setQuantiteStock(medication.getQuantiteStock() - quantity);
                MedicationDAO.updateMedication(medication);
            }
            clearCart();
        } catch (Exception e) {
            showErrorAlert("Erreur lors de la mise à jour du stock: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Modify clearCart to save the empty cart state
    public void clearCart() {
        cartItems.clear();
        cartItemCount = 0;
        updateCartCount();
        saveCartToFile(); // Save empty cart state
        // Mettre à jour l'affichage du panier
        if (cartController != null) {
            cartController.updateCartItems(cartItems);
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
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}