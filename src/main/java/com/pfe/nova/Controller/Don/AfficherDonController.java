package com.pfe.nova.Controller.Don;

import com.pfe.nova.models.Don;
import com.pfe.nova.services.DonService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherDonController {

    @FXML
    private TableView<Don> tableview;

    @FXML
    private javafx.scene.control.TextField searchField;

    @FXML
    private TableColumn<Don, Integer> idCol;

    @FXML
    private TableColumn<Don, String> typeDonCol;

    @FXML
    private TableColumn<Don, Double> montantCol;

    @FXML
    private TableColumn<Don, String> descriptionCol;

    @FXML
    private TableColumn<Don, String> dateDonCol;

    @FXML
    private TableColumn<Don, Integer> campagneIdCol;

    @FXML
    private TableColumn<Don, String> modePaiementCol;

    @FXML
    private TableColumn<Don, String> preuveDonCol;

    private DonService donService = new DonService();
    private ObservableList<Don> observableList;

    @FXML
    void initialize() {
        try {
            chargerDonnees();

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (newValue == null || newValue.isEmpty()) {
                        chargerDonnees();
                    } else {
                        List<Don> donsFiltered = donService.rechercherParType(newValue);
                        observableList = FXCollections.observableArrayList(donsFiltered);
                        tableview.setItems(observableList);
                    }
                } catch (SQLException e) {
                    afficherErreur("Erreur lors de la recherche", e.getMessage());
                }
            });
        } catch (SQLException e) {
            afficherErreur("Erreur de connexion à la base de données", e.getMessage());
        }
    }

    private void chargerDonnees() throws SQLException {

        List<Don> donsList = donService.recuperer();
        observableList = FXCollections.observableArrayList(donsList);


        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeDonCol.setCellValueFactory(new PropertyValueFactory<>("typeDon"));
        montantCol.setCellValueFactory(new PropertyValueFactory<>("montant"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("descriptionMateriel"));
        dateDonCol.setCellValueFactory(new PropertyValueFactory<>("dateDon"));
        campagneIdCol.setCellValueFactory(new PropertyValueFactory<>("campagneId"));
        modePaiementCol.setCellValueFactory(new PropertyValueFactory<>("modePaiement"));
        preuveDonCol.setCellValueFactory(new PropertyValueFactory<>("preuveDon"));


        tableview.getStyleClass().add("don-table");
        tableview.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        tableview.setItems(observableList);
    }

    @FXML
    void ajouterDon(ActionEvent event) {
        try {
            // Charger le fichier FXML pour AjouterDonController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/Don/AjouterDon.fxml"));
            Parent root = loader.load();

            // Ouvrir une nouvelle fenêtre pour ajouter un don
            Stage stage = new Stage();
            stage.setTitle("Ajouter Don");
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
    void modifierDon(ActionEvent event) {
        Don selectedDon = tableview.getSelectionModel().getSelectedItem();
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
                // Rafraîchir la TableView après la modification
                rafraichirTable();
            } catch (IOException e) {
                afficherErreur("Erreur", "Erreur lors du chargement de la fenêtre de modification.");
            }
        } else {
            afficherErreur("Aucun don sélectionné", "Veuillez sélectionner un don à modifier.");
        }
    }

    @FXML
    void supprimerDon(ActionEvent event) {
        Don selectedDon = tableview.getSelectionModel().getSelectedItem();
        if (selectedDon != null) {
            SupprimerDonController supprimerDonController = new SupprimerDonController();
            boolean success = supprimerDonController.supprimerDon(selectedDon);

            if (success) {
                observableList.remove(selectedDon);
                afficherMessage("Succès", "Don supprimé avec succès.");
            } else {
                afficherErreur("Erreur", "Erreur lors de la suppression du don.");
            }
        } else {
            afficherErreur("Aucun don sélectionné", "Veuillez sélectionner un don à supprimer.");
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

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText("Une erreur est survenue");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

