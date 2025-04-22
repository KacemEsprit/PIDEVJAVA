package com.pfe.nova.Controller.Compagnie;

import com.pfe.nova.models.Compagnie;
import com.pfe.nova.services.CompagnieService;
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

    private CompagnieService compagnieService;
    private ObservableList<Compagnie> observableList;

    @FXML
    public void initialize() {
        try {
            compagnieService = new CompagnieService();
            chargerDonnees();


            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    try {
                        if (newValue == null || newValue.isEmpty()) {
                            chargerDonnees(); // Recharger toutes les données si le champ est vide
                        } else {
                            List<Compagnie> resultats = compagnieService.rechercherParNom(newValue);
                            observableList.setAll(resultats);
                        }
                    } catch (SQLException e) {
                        afficherErreur("Erreur lors de la recherche", e.getMessage());
                    }
                });
            }
        } catch (SQLException e) {
            afficherErreur("Erreur de connexion à la base de données", e.getMessage());
        }
    }

    private void chargerDonnees() throws SQLException {

        List<Compagnie> compagniesList = compagnieService.recuperer();
        observableList = FXCollections.observableArrayList(compagniesList);


        cardContainer.getChildren().clear();


        for (Compagnie compagnie : compagniesList) {
            VBox card = createCompagnieCard(compagnie);
            cardContainer.getChildren().add(card);
        }


        lblTotalCompagnies.setText(String.valueOf(compagniesList.size()));

        lblTotalCompagnies.setText(String.valueOf(compagniesList.size()));
    }

    private VBox createCompagnieCard(Compagnie compagnie) {
        VBox card = new VBox(10);
        card.getStyleClass().add("company-card");
        card.setPadding(new Insets(15));


        card.setOnMouseClicked(event -> {
            selectedCompagnie = compagnie;

            cardContainer.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    node.getStyleClass().remove("selected-card");
                }
            });
            card.getStyleClass().add("selected-card");
        });


        ImageView logoView = new ImageView();
        if (compagnie.getLogo() != null && !compagnie.getLogo().isEmpty()) {
            try {
                Image logo = new Image(compagnie.getLogo());
                logoView.setImage(logo);
            } catch (Exception e) {
                // Utiliser une image par défaut en cas d'erreur
                logoView.setImage(new Image(getClass().getResourceAsStream("/images/default-company.png")));
            }
        } else {
            logoView.setImage(new Image(getClass().getResourceAsStream("/images/default-company.png")));
        }
        logoView.setFitWidth(100);
        logoView.setFitHeight(100);
        logoView.setPreserveRatio(true);


        Label nomLabel = new Label(compagnie.getNom());
        nomLabel.getStyleClass().add("company-name");


        VBox contactInfo = new VBox(5);
        contactInfo.getChildren().addAll(
                createInfoLabel("Adresse: " + compagnie.getAdresse()),
                createInfoLabel("Tél: " + compagnie.getTelephone()),
                createInfoLabel("Email: " + compagnie.getEmail())
        );


        Label descriptionLabel = new Label(compagnie.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("company-description");

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
;


        editButton.setOnAction(e -> {
            selectedCompagnie = compagnie;
            modifierCompagnie(new ActionEvent());
        });
        deleteButton.setOnAction(e -> supprimerCompagnie(compagnie));
        deleteButton.setStyle("-fx-background-color: #FF0000; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");

        actions.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(logoView, nomLabel, contactInfo, descriptionLabel, actions);
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
                List<Compagnie> resultats = compagnieService.rechercherParNom(searchQuery);
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
}