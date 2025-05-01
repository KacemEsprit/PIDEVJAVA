package com.pfe.nova.Controller.Don;

import com.pfe.nova.models.Don;
import com.pfe.nova.services.DonService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Date;
import java.sql.SQLException;

public class ModifierDonController {

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtTypeDon;

    @FXML
    private TextField txtMontant;

    @FXML
    private TextField txtDescriptionMateriel;

    @FXML
    private TextField txtDateDon;

    @FXML
    private TextField txtModePaiement;

    @FXML
    private TextField txtPreuveDon;

    @FXML
    private javafx.scene.control.Label lblPreuveDon;

    private Don donToModify;

    public void setDonToModify(Don don) {
        this.donToModify = don;
        txtId.setText(String.valueOf(don.getId()));
        txtTypeDon.setText(don.getTypeDon());
        txtMontant.setText(String.valueOf(don.getMontant()));
        txtDescriptionMateriel.setText(don.getDescriptionMateriel());
        txtDateDon.setText(don.getDateDon().toString());
        txtModePaiement.setText(don.getModePaiement());
        txtPreuveDon.setText(don.getPreuveDon());
    }

    @FXML
    void validerModification(javafx.event.ActionEvent event) {
        try {
            if (donToModify == null) {
                afficherErreur("Erreur", "Aucun don à modifier.");
                return;
            }

            Don don = new Don(
                    Integer.parseInt(txtId.getText()),
                    txtTypeDon.getText(),
                    Double.parseDouble(txtMontant.getText()),
                    txtDescriptionMateriel.getText(),
                    Date.valueOf(txtDateDon.getText()),
                    donToModify.getDonateurId(),
                    donToModify.getCampagneId(), //
                    txtModePaiement.getText(),
                    txtPreuveDon.getText()
            );

            DonService donService = new DonService();
            donService.modifier(don);
            afficherMessage("Succès", "Don modifié avec succès.");

            // Fermer la fenêtre après la modification
            ((javafx.stage.Stage) txtId.getScene().getWindow()).close();

        } catch (SQLException e) {
            afficherErreur("Erreur", "Erreur lors de la modification du don: " + e.getMessage());
        } catch (NumberFormatException e) {
            afficherErreur("Erreur de format", "Veuillez vérifier les champs numériques.");
        } catch (IllegalArgumentException e) {
            afficherErreur("Erreur de date", "Veuillez vérifier le format de la date (yyyy-mm-dd).");
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

    @FXML
    private void choisirFichier() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir une preuve de don");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new javafx.stage.FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        java.io.File selectedFile = fileChooser.showOpenDialog(lblPreuveDon.getScene().getWindow());
        if (selectedFile != null) {
            lblPreuveDon.setText(selectedFile.getName());
            txtPreuveDon.setText(selectedFile.getAbsolutePath());
        }
    }
}
