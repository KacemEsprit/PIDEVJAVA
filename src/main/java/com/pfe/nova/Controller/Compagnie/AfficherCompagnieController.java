package com.pfe.nova.Controller.Compagnie;

import com.pfe.nova.models.Compagnie;
import com.pfe.nova.models.User;
import com.pfe.nova.models.Donateur;
import com.pfe.nova.services.CompagnieService;
import com.pfe.nova.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherCompagnieController {

    @FXML
    private FlowPane cardContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Label lblTotalCompagnies;

    @FXML
    private Button btnConfirmer;
    @FXML
    private Button btnRejeter;

    @FXML
    private ComboBox<String> statutFilterComboBox;

    private CompagnieService compagnieService;
    private ObservableList<Compagnie> observableList;

    // Ajout d'un champ pour stocker l'id du donateur connecté
    private int donateurIdConnecte;
    private boolean isAdmin;

    public void setDonateurIdConnecte(int donateurId) {
        this.donateurIdConnecte = donateurId;
        // Vérifier si l'utilisateur est admin
        User currentUser = SessionManager.getCurrentUser();
        this.isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        System.out.println("[DEBUG] Donateur connecté id=" + donateurId + ", isAdmin=" + isAdmin);
        try {
            chargerDonnees();
        } catch (Exception e) {
            afficherErreur("Erreur lors du chargement des compagnies", e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        try {
            compagnieService = new CompagnieService();
            // Initialisation du filtre des statuts
            statutFilterComboBox.getItems().addAll("Tous", "En attente", "Validée", "Rejetée");
            statutFilterComboBox.setValue("Tous");
            statutFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    chargerDonnees();
                } catch (SQLException e) {
                    afficherErreur("Erreur lors du filtrage", e.getMessage());
                }
            });
            // Correction : si admin, charger toutes les compagnies directement
            User currentUser = com.pfe.nova.utils.SessionManager.getCurrentUser();
            this.isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
            if (isAdmin) {
                System.out.println("[DEBUG] Admin détecté dans initialize, chargement de toutes les compagnies");
                chargerDonnees();
            }
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    try {
                        if (newValue == null || newValue.isEmpty()) {
                            chargerDonnees(); // Recharger toutes les données si le champ est vide
                        } else {
                            List<Compagnie> resultats;
                            if (isAdmin) {
                                resultats = compagnieService.recuperer();
                            } else {
                                resultats = compagnieService.recupererParDonateurId(donateurIdConnecte);
                            }
                            resultats.removeIf(c -> !c.getNom().toLowerCase().contains(newValue.toLowerCase()));
                            observableList.setAll(resultats);
                        }
                    } catch (Exception e) {
                        afficherErreur("Erreur lors de la recherche", e.getMessage());
                    }
                });
            }
        } catch (SQLException e) {
            afficherErreur("Erreur de connexion à la base de données", e.getMessage());
        }
    }

    private void chargerDonnees() throws SQLException {

        List<Compagnie> compagniesList;
        if (isAdmin) {
            compagniesList = compagnieService.recuperer();
        } else {
            compagniesList = compagnieService.recupererParDonateurId(donateurIdConnecte);
        }
        System.out.println("[DEBUG] Compagnies récupérées pour affichage :");
        for (Compagnie c : compagniesList) {
            System.out.println("  - " + c.getNom() + " (donateurId=" + c.getDonateurId() + ")");
        }
        String selectedStatut = statutFilterComboBox != null ? statutFilterComboBox.getValue() : "Tous";
        if (!"Tous".equals(selectedStatut)) {
            compagniesList.removeIf(c -> {
                switch (selectedStatut) {
                    case "Validée": return !"CONFIRMEE".equals(c.getStatut_validation());
                    case "En attente": return !"EN_ATTENTE".equals(c.getStatut_validation());
                    case "Rejetée": return !"REJETEE".equals(c.getStatut_validation());
                    default: return false;
                }
            });
        }
        observableList = FXCollections.observableArrayList(compagniesList);

        cardContainer.getChildren().clear();

        for (Compagnie compagnie : compagniesList) {
            VBox card = createCompagnieCard(compagnie);
            cardContainer.getChildren().add(card);
        }

        lblTotalCompagnies.setText(String.valueOf(compagniesList.size()));
    }

    private VBox createCompagnieCard(Compagnie compagnie) {
        VBox card = new VBox(15);
        card.getStyleClass().add("company-card");
        card.setPadding(new Insets(20));
        card.setSpacing(15);

        HBox topSection = new HBox(15);
        topSection.setAlignment(Pos.CENTER_LEFT);

        // Logo
        ImageView logoView = new ImageView();
        if (compagnie.getLogo() != null && !compagnie.getLogo().isEmpty()) {
            try {
                Image logo = new Image(compagnie.getLogo());
                logoView.setImage(logo);
            } catch (Exception e) {
                logoView.setImage(new Image(getClass().getResourceAsStream("/images/default-company.png")));
            }
        } else {
            logoView.setImage(new Image(getClass().getResourceAsStream("/images/default-company.png")));
        }
        logoView.setFitWidth(70);
        logoView.setFitHeight(70);
        logoView.setPreserveRatio(true);
        logoView.getStyleClass().add("company-logo");

        VBox infoSection = new VBox(5);
        infoSection.setAlignment(Pos.TOP_LEFT);
        Label nomLabel = new Label(compagnie.getNom());
        nomLabel.getStyleClass().add("company-name");
        HBox statutBox = new HBox(7);
        statutBox.setAlignment(Pos.CENTER_LEFT);
        Label statutLabel = new Label();
        String statut = compagnie.getStatut_validation();
        statutLabel.setMinHeight(24);
        statutLabel.getStyleClass().add("status-badge");
        switch (statut) {
            case "CONFIRMEE":
                statutLabel.setText("Validée");
                statutLabel.getStyleClass().add("valid");
                break;
            case "REJETEE":
                statutLabel.setText("Rejetée");
                statutLabel.getStyleClass().add("rejected");
                break;
            default:
                statutLabel.setText("En attente");
                statutLabel.getStyleClass().add("pending");
                break;
        }
        statutBox.getChildren().addAll(statutLabel);
        infoSection.getChildren().addAll(nomLabel, statutBox);

        topSection.getChildren().addAll(logoView, infoSection);

        VBox contactInfo = new VBox(3);
        contactInfo.getChildren().addAll(
                createInfoLabel("Adresse: " + compagnie.getAdresse()),
                createInfoLabel("Tél: " + compagnie.getTelephone()),
                createInfoLabel("Email: " + compagnie.getEmail())
        );

        Label descriptionLabel = new Label(compagnie.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("company-description");
        descriptionLabel.setMaxHeight(40);

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");
        Button confirmerButton = new Button("Confirmé");
        Button rejeterButton = new Button("Rejeté");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        confirmerButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        rejeterButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        editButton.setOnAction(e -> {
            selectedCompagnie = compagnie;
            modifierCompagnie(new ActionEvent());
        });
        deleteButton.setOnAction(e -> supprimerCompagnie(compagnie));
        confirmerButton.setOnAction(e -> {
            selectedCompagnie = compagnie;
            confirmerCompagnie(new ActionEvent());
        });
        rejeterButton.setOnAction(e -> {
            selectedCompagnie = compagnie;
            rejeterCompagnie(new ActionEvent());
        });
        if ("EN_ATTENTE".equals(compagnie.getStatut_validation())) {
            actions.getChildren().addAll(editButton, deleteButton, confirmerButton, rejeterButton);
        } else {
            actions.getChildren().addAll(editButton, deleteButton);
        }

        card.getChildren().addAll(topSection, contactInfo, descriptionLabel, actions);
        return card;
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("company-info");
        return label;
    }

    @FXML
    void ajouterCompagnie(ActionEvent event) {
        try {
            // Charger la fenêtre d'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/Compagnie/AjouterCompagnie.fxml"));
            Parent root = loader.load();

            // Ouvrir la fenêtre d'ajout
            Stage stage = new Stage();
            stage.setTitle("Ajouter Compagnie");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir la TableView après l'ajout
            rafraichirTable();
        } catch (IOException e) {
            afficherErreur("Erreur", "Erreur lors du chargement de la fenêtre d'ajout.");
        }
    }

    @FXML
    private Compagnie selectedCompagnie;

    @FXML
    public void modifierCompagnie(ActionEvent event) {
        if (selectedCompagnie == null) {
            afficherErreur("Erreur", "Veuillez sélectionner une compagnie à modifier.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/Compagnie/ModifierCompagnie.fxml"));
            Parent root = loader.load();

            ModifierCompagnieController modifierController = loader.getController();
            modifierController.setCompagnieToModify(selectedCompagnie);

            Stage stage = new Stage();
            stage.setTitle("Modifier Compagnie");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (modifierController.modifierCompagnie()) {
                afficherMessage("Succès", "Compagnie modifiée avec succès.");
                rafraichirTable();
            }
        } catch (IOException e) {
            afficherErreur("Erreur", "Erreur lors du chargement de la fenêtre de modification.");
        }
    }

    @FXML
    public void supprimerCompagnie(Compagnie compagnie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la compagnie");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la compagnie " + compagnie.getNom() + " ?");

        if (alert.showAndWait().get().getButtonData().isDefaultButton()) {
            try {
                compagnieService.supprimer(compagnie.getId());
                afficherMessage("Succès", "Compagnie supprimée avec succès.");
                rafraichirTable();
            } catch (SQLException e) {
                afficherErreur("Erreur", "Erreur lors de la suppression de la compagnie.");
            }
        }
    }

    private void rafraichirTable() {
        try {
            chargerDonnees();
        } catch (SQLException e) {
            afficherErreur("Erreur lors du rafraîchissement", e.getMessage());
        }
    }

    private void afficherMessage(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void rechercherCompagnies(ActionEvent event) {
        String searchQuery = searchField.getText().trim();
        try {
            if (searchQuery.isEmpty()) {
                chargerDonnees();
            } else {
                // Recherche seulement dans les compagnies du donateur connecté
                List<Compagnie> resultats;
                if (isAdmin) {
                    resultats = compagnieService.recuperer();
                } else {
                    resultats = compagnieService.recupererParDonateurId(donateurIdConnecte);
                }
                resultats.removeIf(c -> !c.getNom().toLowerCase().contains(searchQuery.toLowerCase()));
                observableList.setAll(resultats);

                cardContainer.getChildren().clear();
                for (Compagnie compagnie : resultats) {
                    VBox card = createCompagnieCard(compagnie);
                    cardContainer.getChildren().add(card);
                }

                lblTotalCompagnies.setText(String.valueOf(resultats.size()));
            }
        } catch (SQLException e) {
            afficherErreur("Erreur lors de la recherche", e.getMessage());
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText("Une erreur est survenue");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void confirmerCompagnie(ActionEvent event) {
        if (selectedCompagnie == null) {
            afficherErreur("Erreur", "Veuillez sélectionner une compagnie à confirmer.");
            return;
        }
        try {
            compagnieService.confirmerCompagnie(selectedCompagnie.getId());
            selectedCompagnie.setStatut_validation("CONFIRMEE"); // Met à jour le modèle localement
            // Envoi de l'email de validation
            if (selectedCompagnie.getEmail() != null && !selectedCompagnie.getEmail().isEmpty()) {
                com.pfe.nova.utils.EmailSender.sendValidationEmail(selectedCompagnie.getEmail(), selectedCompagnie.getNom());
            } else {
                System.out.println("[AVERTISSEMENT] Aucun email renseigné pour la compagnie validée : " + selectedCompagnie.getNom());
            }
            afficherMessage("Succès", "Compagnie confirmée avec succès.");
            rafraichirTable();
        } catch (SQLException e) {
            afficherErreur("Erreur", "Erreur lors de la confirmation de la compagnie : " + e.getMessage());
        }
    }

    @FXML
    private void rejeterCompagnie(ActionEvent event) {
        if (selectedCompagnie == null) {
            afficherErreur("Erreur", "Veuillez sélectionner une compagnie à rejeter.");
            return;
        }
        try {
            compagnieService.rejeterCompagnie(selectedCompagnie.getId());
            selectedCompagnie.setStatut_validation("REJETEE"); // Met à jour le modèle localement
            chargerDonnees();
            afficherMessage("Succès", "Compagnie rejetée avec succès.");
        } catch (SQLException e) {
            afficherErreur("Erreur", "Erreur lors du rejet de la compagnie : " + e.getMessage());
        }
    }
}