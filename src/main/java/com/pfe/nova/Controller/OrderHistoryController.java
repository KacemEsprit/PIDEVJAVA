package com.pfe.nova.Controller;

import com.pfe.nova.configuration.MedicationDAO;
import com.pfe.nova.configuration.OrderDAO;
import com.pfe.nova.models.Medication;
import com.pfe.nova.models.Order;
import com.pfe.nova.utils.QRCodeGenerator;
import com.pfe.nova.utils.RewardPDFGenerator;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderHistoryController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox ordersContainer;
    @FXML private Button closeButton;
    @FXML private Button refreshButton;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;


    private final OrderDAO orderDAO = new OrderDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private List<Order> allOrders;

    @FXML
    public void initialize() {
        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);

            if (ordersContainer == null) {
                ordersContainer = new VBox(10);
                ordersContainer.setPadding(new Insets(10));
            }

            // ComboBox de filtre
            statusFilter.getItems().addAll("Tous", "En cours", "Validée", "Rejetée");
            statusFilter.setValue("Tous");
            statusFilter.setOnAction(e -> filterOrdersByStatus());
            searchField.setOnKeyReleased(e -> filterOrdersBySearch());


            VBox mainContainer = new VBox(10);
            mainContainer.getChildren().addAll(statusFilter, ordersContainer);
            scrollPane.setContent(mainContainer);

            loadOrders();
        }
    }

    private void filterOrdersByStatus() {
        filterOrdersBySearch();
        if (allOrders == null) return;

        String selectedStatus = statusFilter.getValue();
        List<Order> filteredOrders = "Tous".equals(selectedStatus)
                ? allOrders
                : allOrders.stream().filter(order -> order.getStatus().equals(selectedStatus)).collect(Collectors.toList());

        displayOrders(filteredOrders);
    }

    @FXML
    private void loadOrders() {
        if (ordersContainer == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'initialisation de l'interface");
            return;
        }

        ordersContainer.getChildren().clear();
        try {
            allOrders = orderDAO.getAllOrders();
            if (allOrders == null || allOrders.isEmpty()) {
                showEmptyState();
            } else {
                filterOrdersByStatus();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion à la base: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur inattendue: " + e.getMessage());
        }
    }

    private StackPane createOrderFlipCard(Order order) {
        StackPane flipCard = new StackPane();
        flipCard.setPrefWidth(700);

        // FRONT
        VBox frontCard = new VBox(10);
        frontCard.setAlignment(Pos.CENTER_LEFT);
        frontCard.setPadding(new Insets(15));
        frontCard.getStyleClass().add("order-card");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Text dateText = new Text(dateFormatter.format(order.getDate()));
        dateText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");

        Text idText = new Text("Commande #" + order.getId());
        idText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");

        header.getChildren().addAll(dateText, new Separator(), idText);

        VBox itemsContainer = new VBox(new Text("Articles:"), new Text(order.getItemsSummary()));
        Circle statusIndicator = new Circle(5);
        statusIndicator.setStyle(getStatusColor(order.getStatus()));

        HBox statusContainer = new HBox(10, statusIndicator, new Text("Status: " + order.getStatus()));
        statusContainer.setAlignment(Pos.CENTER_LEFT);

        Text totalText = new Text(String.format("Total: %.2f DT", order.getMontantTotal()));
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        if ("En cours".equals(order.getStatus())) {
            Button modifyButton = new Button("Modifier 📝");
            modifyButton.setOnAction(e -> modifyOrder(order));
            modifyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");

            Button cancelButton = new Button("Annuler ❌");
            cancelButton.setOnAction(e -> deleteOrder(order));
            cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");

            actionButtons.getChildren().addAll(modifyButton, cancelButton);
        }

        frontCard.getChildren().addAll(header, new Separator(), itemsContainer, new Separator(), statusContainer, totalText, actionButtons);

        // BACK
        VBox backCard = new VBox(10);
        backCard.setAlignment(Pos.CENTER);
        backCard.setPadding(new Insets(15));
        backCard.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12;");

        if ("Validée".equalsIgnoreCase(order.getStatus())) {
            Button invoiceButton = new Button("Facture 📄");
            invoiceButton.setOnAction(e -> generateInvoice(order));
            invoiceButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5;");

            HBox ratingBox = new HBox(5);
            ratingBox.setAlignment(Pos.CENTER);

            for (int i = 1; i <= 5; i++) {
                final int rating = i;
                Label star = new Label("★");
                star.setStyle("-fx-font-size: 24px; -fx-cursor: hand;" + (i <= order.getRate() ? "-fx-text-fill: gold;" : "-fx-text-fill: gray;"));
                star.setOnMouseClicked(e -> {
                    try {
                        orderDAO.updateOrderRating(order.getId(), rating);
                        order.setRate(rating);
                        refreshStars(ratingBox, rating);
                        showAlert(Alert.AlertType.INFORMATION, "Merci", "Votre Avis est pris en compte !");
                    } catch (SQLException ex) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur mise à jour note : " + ex.getMessage());
                    }
                });
                ratingBox.getChildren().add(star);
            }

            VBox qrContainer = new VBox(10);
            qrContainer.setAlignment(Pos.CENTER);

            // ➡️ 1. Choisir un cadeau aléatoire
            List<String> cadeaux = List.of(
                    "10% de réduction sur votre prochaine commande",
                    "Vitamine C offerte 🎁",
                    "Masque médical enfant gratuit 😷",
                    "Gel hydroalcoolique enfant offert 🧴",
                    "Thermomètre digital en promotion 🌡️",
                    "Crème hydratante offerte pour peaux sensibles 🧴",
                    "Petit doudou offert pour accompagner votre enfant 🧸",
                    "Boîte de pansements à motifs enfantins offerte 🩹",
                    "Bonnet doux offert pour protéger du froid 🎩",
                    "Spray nasal doux offert 👃",
                    "Sachet de vitamines adaptées aux enfants 🍬",
                    "Trousse de premiers soins colorée offerte 🚑",
                    "Protège-masque en tissu enfant offert 😷",
                    "Bracelet de courage offert 🎗️",
                    "Pack découverte soins naturels pour enfants 🌿",
                    "Livre de coloriage offert 🎨",
                    "Puzzle éducatif pour enfant offert 🧩",
                    "Boisson vitaminée offerte 🧃",
                    "Bouteille d’eau réutilisable enfant offerte 🥤"
            );
            String cadeau = cadeaux.get((int)(Math.random() * cadeaux.size()));

            // ➡️ 2. Générer un mini PDF Ticket de récompense
            RewardPDFGenerator rewardPDF = new RewardPDFGenerator();
            String rewardPdfPath = rewardPDF.generateRewardTicket(order, cadeau); // <-- CORRECT

            // ➡️ 3. Générer le QR code qui pointe vers ce fichier
            try {
                // Récupérer automatiquement l'IP locale
                String serverIp = java.net.InetAddress.getLocalHost().getHostAddress();

                // Construire l'URL du fichier PDF
                String pdfHttpUrl = "http://" + serverIp + "/rewards/" + new File(rewardPdfPath).getName();

                // Vérifier l'existence du fichier PDF en ligne (facultatif, mais recommandé)
                java.net.URL url = new java.net.URL(pdfHttpUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                connection.disconnect();

                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    // Si fichier disponible, générer le QR
                    ImageView qrImage = new ImageView(QRCodeGenerator.generateQRCodeImage(pdfHttpUrl, 150, 150));
                    qrContainer.getChildren().addAll(new Label("Scannez pour découvrir votre cadeau 🎁"), qrImage);
                } else {
                    // Si fichier pas disponible
                    Label errorLabel = new Label("Cadeau indisponible pour le moment ❌");
                    errorLabel.setStyle("-fx-text-fill: red;");
                    qrContainer.getChildren().add(errorLabel);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Label errorLabel = new Label("Erreur QR Code 🎁");
                errorLabel.setStyle("-fx-text-fill: red;");
                qrContainer.getChildren().add(errorLabel);
            }



            backCard.getChildren().addAll(invoiceButton, ratingBox, qrContainer);
        }
        else {
            Label infoLabel = new Label("Facture et avis disponibles après validation.");
            infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            backCard.getChildren().add(infoLabel);
        }

        backCard.setVisible(false);
        flipCard.getChildren().addAll(frontCard, backCard);

        // Flip animation
        flipCard.setOnMouseClicked(event -> {
            RotateTransition rotateOut = new RotateTransition(Duration.millis(300), flipCard);
            rotateOut.setAxis(new Point3D(0, 1, 0));
            rotateOut.setFromAngle(0);
            rotateOut.setToAngle(90);

            RotateTransition rotateIn = new RotateTransition(Duration.millis(300), flipCard);
            rotateIn.setAxis(new Point3D(0, 1, 0));
            rotateIn.setFromAngle(-90);
            rotateIn.setToAngle(0);

            rotateOut.setOnFinished(e -> {
                frontCard.setVisible(!frontCard.isVisible());
                backCard.setVisible(!backCard.isVisible());
                rotateIn.play();
            });

            rotateOut.play();
        });

        return flipCard;
    }


    private void refreshStars(HBox ratingBox, int rating) {
        for (int i = 0; i < ratingBox.getChildren().size(); i++) {
            Label s = (Label) ratingBox.getChildren().get(i);
            s.setStyle("-fx-font-size: 24px; -fx-cursor: hand;" + (i < rating ? "-fx-text-fill: gold;" : "-fx-text-fill: gray;"));
        }
    }


    private void showLoadingIndicator() {
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        VBox loadingContainer = new VBox(loadingIndicator);
        loadingContainer.setAlignment(Pos.CENTER);
        ordersContainer.getChildren().add(loadingContainer);
    }

    private void showEmptyState() {
        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setSpacing(15);

        Label noOrdersLabel = new Label("Aucune commande trouvée");
        Button refreshButton = new Button("Actualiser");
        refreshButton.setOnAction(e -> handleRefresh());

        placeholder.getChildren().addAll(noOrdersLabel, refreshButton);
        ordersContainer.getChildren().setAll(placeholder);
    }

    private void displayOrders(List<Order> orders) {
        ordersContainer.getChildren().clear();
        orders.stream()
                .map(this::createOrderFlipCard)
                .forEach(card -> ordersContainer.getChildren().add(card));
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
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la modification.");
        }
    }

    private void deleteOrder(Order order) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer la commande");
        confirmAlert.setContentText("Êtes-vous sûr ?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (Medication medication : order.getMedications()) {
                    medication.setQuantiteStock(medication.getQuantiteStock() + medication.getQuantiteCommande());
                    medicationDAO.updateMedication(medication);
                }
                orderDAO.deleteOrder(order.getId());
                loadOrders();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande supprimée !");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur suppression: " + e.getMessage());
            }
        }
    }

    private void generateInvoice(Order order) {
        PDFController pdfController = new PDFController();

        try {
            String filePath = pdfController.generateInvoicePDF(order); // ➡️ Le chemin du PDF est retourné
            File pdfFile = new File(filePath);
            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile); // ➡️ Ouvrir automatiquement si fichier existe
            }
            showAlert(Alert.AlertType.INFORMATION, "Succès", "La facture a été générée et ouverte avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur génération de la facture : " + e.getMessage());
        }
    }

    private String getStatusColor(String status) {
        return switch (status.toLowerCase()) {
            case "en cours" -> "-fx-fill: #f39c12;";  // Orange
            case "validée" -> "-fx-fill: #2ecc71;";   // Vert
            case "rejetée", "annulée" -> "-fx-fill: #e74c3c;"; // Rouge
            default -> "-fx-fill: #95a5a6;";          // Gris si inconnu
        };
    }
    private void filterOrdersBySearch() {
        if (allOrders == null) return;

        String searchText = searchField.getText().trim().toLowerCase();

        // On applique d’abord le filtre de statut
        String selectedStatus = statusFilter.getValue();
        List<Order> filteredOrders = "Tous".equals(selectedStatus)
                ? allOrders
                : allOrders.stream()
                .filter(order -> order.getStatus().equalsIgnoreCase(selectedStatus))
                .collect(Collectors.toList());

        // Ensuite on applique la recherche par ID ou date
        if (!searchText.isEmpty()) {
            filteredOrders = filteredOrders.stream()
                    .filter(order ->
                            String.valueOf(order.getId()).contains(searchText) ||
                                    dateFormatter.format(order.getDate()).toLowerCase().contains(searchText)
                    )
                    .collect(Collectors.toList());
        }

        displayOrders(filteredOrders);
    }


}
