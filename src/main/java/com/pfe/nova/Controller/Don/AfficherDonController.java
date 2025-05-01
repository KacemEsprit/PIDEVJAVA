package com.pfe.nova.Controller.Don;

import com.pfe.nova.models.Don;
import com.pfe.nova.models.Donateur;
import com.pfe.nova.services.CompagnieService;
import com.pfe.nova.services.DonService;
import com.pfe.nova.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherDonController {

    @FXML
    private FlowPane cardContainer;

    @FXML
    private TextField searchField;
    
    @FXML
    private Label lblTotalDons;

    private DonService donService = new DonService();
    private Don selectedDon = null;

    @FXML
    private void initialize() {
        try {
            afficherCards();
            
            // Configurer la recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (newValue == null || newValue.isEmpty()) {
                        afficherCards();
                    } else {
                        List<Don> donsFiltered = donService.rechercherParType(newValue);
                        afficherCards(donsFiltered);
                    }
                } catch (SQLException e) {
                    afficherErreur("Erreur lors de la recherche", e.getMessage());
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur d'initialisation", "Impossible de charger les dons: " + e.getMessage());
        }
    }

    private void afficherCards() throws SQLException {
        List<Don> donsList = donService.recuperer();
        afficherCards(donsList);
    }
    
    private void afficherCards(List<Don> donsList) {
        cardContainer.getChildren().clear();
        
        for (Don don : donsList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/Don/DonCard.fxml"));
                VBox card = loader.load();
                
                // Ajouter une classe CSS pour la sélection
                card.getStyleClass().add("don-card");
                
                // Configurer le contrôleur de la carte
                DonCardController controller = loader.getController();
                controller.setDon(don);
                
                // Gérer la sélection de la carte
                card.setOnMouseClicked(event -> {
                    // Désélectionner toutes les cartes
                    cardContainer.getChildren().forEach(node -> {
                        if (node instanceof VBox) {
                            ((VBox) node).getStyleClass().remove("selected-card");
                        }
                    });
                    
                    // Sélectionner cette carte
                    card.getStyleClass().add("selected-card");
                    selectedDon = don;
                });
                
                cardContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Mettre à jour le total
        if (lblTotalDons != null) {
            lblTotalDons.setText(String.valueOf(donsList.size()));
        }
    }

    @FXML
    void ajouterDon(ActionEvent event) {
        try {
            // Vérifier si le donateur connecté est une entreprise (COMPAGNIE)
            Donateur currentDonateur = null;
            if (SessionManager.getCurrentUser() instanceof Donateur) {
                currentDonateur = (Donateur) SessionManager.getCurrentUser();
            }
            if (currentDonateur != null &&
                "COMPAGNIE".equalsIgnoreCase(currentDonateur.getDonateurType())) {
                // Vérifier si la compagnie existe déjà pour cet utilisateur
                CompagnieService compagnieService = new CompagnieService();
                boolean hasCompany = compagnieService.hasCompagnie(currentDonateur.getEmail());
                if (!hasCompany) {
                    // Rediriger vers la création de compagnie
                    FXMLLoader compLoader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/Compagnie/AjouterCompagnie.fxml"));
                    Parent compRoot = compLoader.load();
                    Stage compStage = new Stage();
                    compStage.setTitle("Créer une Compagnie");
                    compStage.setScene(new Scene(compRoot));
                    compStage.initModality(Modality.APPLICATION_MODAL);
                    compStage.showAndWait();
                    // Après création, vérifier à nouveau
                    hasCompany = compagnieService.hasCompagnie(currentDonateur.getEmail());
                    if (!hasCompany) {
                        afficherErreur("Création requise", "Vous devez créer une compagnie avant de faire un don.");
                        return;
                    }
                }
            }
            // Ouvrir l'interface d'ajout de don normalement
            FXMLLoader donLoader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/Don/AjouterDon.fxml"));
            Parent donRoot = donLoader.load();
            Stage donStage = new Stage();
            donStage.setTitle("Ajouter Don");
            donStage.setScene(new Scene(donRoot));
            donStage.initModality(Modality.APPLICATION_MODAL);
            donStage.showAndWait();
            try {
                afficherCards();
            } catch (SQLException ex) {
                afficherErreur("Erreur", "Erreur lors du rafraîchissement des dons.");
            }
        } catch (IOException | SQLException e) {
            afficherErreur("Erreur", "Erreur lors du chargement de la fenêtre d'ajout ou de la vérification de compagnie.");
        }
    }

    @FXML
    void modifierDon(ActionEvent event) {
        if (selectedDon != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/Don/ModifierDon.fxml"));
                Parent root = loader.load();
                ModifierDonController modifierDonController = loader.getController();
                modifierDonController.setDonToModify(selectedDon);
                Stage stage = new Stage();
                stage.setTitle("Modifier Don");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
                
                try {
                    afficherCards();
                } catch (SQLException ex) {
                    afficherErreur("Erreur", "Erreur lors du rafraîchissement des dons.");
                }
            } catch (IOException e) {
                afficherErreur("Erreur", "Erreur lors du chargement de la fenêtre de modification.");
            }
        } else {
            afficherErreur("Aucun don sélectionné", "Veuillez sélectionner un don à modifier.");
        }
    }

    @FXML
    void supprimerDon(ActionEvent event) {
        if (selectedDon != null) {
            SupprimerDonController supprimerDonController = new SupprimerDonController();
            boolean success = supprimerDonController.supprimerDon(selectedDon);
            if (success) {
                try {
                    afficherCards();
                    afficherMessage("Succès", "Don supprimé avec succès.");
                } catch (SQLException ex) {
                    afficherErreur("Erreur", "Erreur lors du rafraîchissement des dons.");
                }
            } else {
                afficherErreur("Erreur", "Erreur lors de la suppression du don.");
            }
        } else {
            afficherErreur("Aucun don sélectionné", "Veuillez sélectionner un don à supprimer.");
        }
    }

    private void afficherMessage(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
