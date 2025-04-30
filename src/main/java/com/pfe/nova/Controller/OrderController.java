package com.pfe.nova.Controller;

import com.pfe.nova.configuration.MedicationDAO;
import com.pfe.nova.models.Medication;
import com.pfe.nova.models.Patient;
import com.pfe.nova.utils.Session;
import com.pfe.nova.models.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderController {
    @FXML private FlowPane medicationFlowPane;
    @FXML private Button cartButton;
    @FXML private Label cartCountLabel;
    @FXML private TextField searchField;
    
    private int patientId; 
    
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
    
    public void saveCartToFile() {
        if (patientId <= 0) {
            System.err.println("ID patient invalide pour la sauvegarde du panier");
            return;
        }
        try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
                new java.io.FileOutputStream("cart_" + patientId + ".ser"))) {
            out.writeObject(cartItems);
            out.writeInt(cartItemCount);
            Platform.runLater(() -> {
                if (cartController != null) {
                    cartController.updateCartItems(cartItems);
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde du panier: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void initializeForPatient(int patientId) {
        User currentUser = Session.getUtilisateurConnecte();
        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            this.patientId = patient.getId();
            loadCartFromFile();
        } else {
            showErrorAlert("Accès non autorisé. Seuls les patients peuvent accéder au panier.");
        }
    }

    private void loadCartFromFile() {
        try {
            User currentUser = Session.getUtilisateurConnecte();
            if (!(currentUser instanceof Patient)) {
                showErrorAlert("Accès non autorisé. Seuls les patients peuvent accéder au panier.");
                return;
            }
            Patient patient = (Patient) currentUser;
            if (patient.getId() != patientId) {
                showErrorAlert("Vous ne pouvez pas accéder au panier d'un autre patient.");
                return;
            }
            java.io.File cartFile = new java.io.File("cart_" + patientId + ".ser");
            if (cartFile.exists()) {
                try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(
                        new java.io.FileInputStream(cartFile))) {
                    Object cartData = in.readObject();
                    if (cartData instanceof Map) {
                        cartItems = (Map<Medication, Integer>) cartData;
                        cartItemCount = in.readInt();
                        updateCartCount();
                        if (cartController != null) {
                            Platform.runLater(() -> cartController.updateCartItems(cartItems));
                        }
                    } else {
                        throw new ClassCastException("Format de données du panier invalide");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du panier: " + e.getMessage());
            e.printStackTrace();
            cartItems = new HashMap<>();
            cartItemCount = 0;
            saveCartToFile(); // Créer un nouveau fichier de panier vide
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
        
        if (medication.getImagePath() != null && !medication.getImagePath().isEmpty()) {
            try {
                // Assurez-vous que l'URL est complète (commence par http:// ou https://)
                String imageUrl = medication.getImagePath();
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    imageUrl = "http://localhost/" + imageUrl;
                }
                Image image = new Image(imageUrl, true); // Le second paramètre active le chargement en arrière-plan
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Erreur de chargement de l'image: " + e.getMessage());
                // Charger une image par défaut en cas d'erreur
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/default-medication.png")));
            }
        } else {
            // Charger une image par défaut si aucune image n'est spécifiée
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default-medication.png")));
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
        addButton.setStyle("-fx-background-color:rgb(5, 142, 234); -fx-text-fill: white; -fx-background-radius: 5;");
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
    
    private void loadMedicationsToFlowPane() {
        try {
            medicationFlowPane.getChildren().clear();
            // Vérifier si l'utilisateur est connecté et est un patient
            User currentUser = Session.getUtilisateurConnecte();
            if (!(currentUser instanceof Patient)) {
                showErrorAlert("Accès non autorisé. Seuls les patients peuvent voir les médicaments.");
                return;
            }
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

    public void clearCart() {
        cartItems.clear();
        cartItemCount = 0;
        updateCartCount();
        saveCartToFile(); 
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
    
    private boolean orderPlaced = false;
    
    public boolean isOrderPlaced() {
        return orderPlaced;
    }
    
    public void setOrderPlaced(boolean orderPlaced) {
        this.orderPlaced = orderPlaced;
    }
    
    public int getPatientId() {
        return this.patientId;
    }
}