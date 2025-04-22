package com.pfe.nova.Controller.Compagnie;

import com.pfe.nova.models.Compagnie;
import com.pfe.nova.services.CompagnieService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;

public class ModifierCompagnieController {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtAdresse;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtSiteWeb;

    @FXML
    private TextField txtDescription;

    @FXML
    private TextField txtLogo;

    @FXML
    private Label lblLogo;
    @FXML
    private TextField txtSiret;

    @FXML
    private Label statut_juridique;

    private Compagnie compagnieToModify;

    public void setCompagnieToModify(Compagnie compagnie) {
        this.compagnieToModify = compagnie;


        txtNom.setText(compagnie.getNom());
        txtAdresse.setText(compagnie.getAdresse());
        txtTelephone.setText(compagnie.getTelephone());
        txtEmail.setText(compagnie.getEmail());
        txtSiteWeb.setText(compagnie.getSiteWeb());
        txtDescription.setText(compagnie.getDescription());
        txtLogo.setText(compagnie.getLogo());
    }

    @FXML
    private void choisirFichier() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un logo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(txtLogo.getScene().getWindow());
        if (selectedFile != null) {
            txtLogo.setText(selectedFile.getAbsolutePath());
            lblLogo.setText(selectedFile.getName());
        }
    }

    @FXML
    public boolean modifierCompagnie() {
        if (compagnieToModify != null) {
            // Validation des champs obligatoires
            if (txtNom.getText().isEmpty() || txtAdresse.getText().isEmpty() || txtTelephone.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de validation");
                alert.setHeaderText(null);
                alert.setContentText("Les champs Nom, Adresse et Téléphone sont obligatoires.");
                alert.showAndWait();
                return false;
            }


            String nom = txtNom.getText();
            String adresse = txtAdresse.getText();
            String telephone = txtTelephone.getText();
            String email = txtEmail.getText();
            String siteWeb = txtSiteWeb.getText();
            String description = txtDescription.getText();
            String logo = txtLogo.getText();
            String Siret= txtSiret.getText();
            String statut_juridique= txtSiret.getText();


            Compagnie modifiedCompagnie = new Compagnie(
                    compagnieToModify.getId(),
                    nom,
                    adresse,
                    telephone,
                    email,
                    siteWeb,
                    description,
                    logo,
                    Siret,
                    statut_juridique          );

            try {
                CompagnieService service = new CompagnieService();
                boolean success = service.modifier(modifiedCompagnie);
                
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText(null);
                    alert.setContentText("Compagnie modifiée avec succès.");
                    alert.showAndWait();
                    
                    // Fermer la fenêtre après la modification
                    txtNom.getScene().getWindow().hide();
                    return true;
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Échec de la modification de la compagnie.");
                    alert.showAndWait();
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("Erreur lors de la modification de la compagnie : " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    @FXML
    private void annuler() {
        // Fermer la fenêtre
        txtNom.getScene().getWindow().hide();
    }

}