package com.pfe.nova.Controller.Compagnie;

import com.pfe.nova.models.Compagnie;
import com.pfe.nova.services.CompagnieService;
import com.pfe.nova.utils.ImageUploader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class AjouterCompagnieController {

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
    private TextField txtSiret;

    @FXML
    private Label lblLogo;

    private File fichierLogo; // Stocke le fichier sélectionné

    @FXML
    void choisirFichier(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier de logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );


        File fichier = fileChooser.showOpenDialog(null);
        if (fichier != null) {
            fichierLogo = fichier;
            lblLogo.setText(fichier.getName()); // Affiche le nom du fichier sélectionné
        } else {
            lblLogo.setText("Aucun fichier sélectionné");
        }
    }

    @FXML
    void ajouterCompagnie(ActionEvent event) {
        try {
            // Vérifications des champs obligatoires
            if (txtNom.getText().isEmpty() || txtEmail.getText().isEmpty() || txtTelephone.getText().isEmpty() || txtSiret.getText().isEmpty() || fichierLogo == null) {
                showErrorAlert("Champs obligatoires", "Veuillez remplir tous les champs obligatoires, y compris le numéro SIRET, et sélectionner un fichier de logo.");
                return;
            }

            // Validation du SIRET (14 chiffres)
            if (!isValidSiret(txtSiret.getText())) {
                showErrorAlert("SIRET invalide", "Le numéro SIRET doit contenir exactement 14 chiffres.");
                return;
            }

            // Validation de l'adresse email
            if (!isValidEmail(txtEmail.getText())) {
                showErrorAlert("Adresse email invalide", "Veuillez entrer une adresse email valide.");
                return;
            }

            // Validation du numéro de téléphone (8 chiffres)
            if (!isValidPhoneNumber(txtTelephone.getText())) {
                showErrorAlert("Numéro de téléphone invalide", "Le numéro de téléphone doit contenir exactement 8 chiffres.");
                return;
            }

            // Validation du site web
            if (!isValidWebsite(txtSiteWeb.getText())) {
                showErrorAlert("Site Web invalide", "Veuillez entrer une URL valide (par exemple, https://example.com).");
                return;
            }

            // Capture des données
            String nom = txtNom.getText();
            String adresse = txtAdresse.getText();
            String telephone = txtTelephone.getText();
            String email = txtEmail.getText();
            String siteWeb = txtSiteWeb.getText();
            String description = txtDescription.getText();
            String siret = txtSiret.getText(); // Récupérer la valeur du SIRET

            // Upload du logo et récupération de l'URL
            String logoUrl = ImageUploader.uploadImage(fichierLogo);

            // Créer un objet Compagnie avec le chemin relatif ou l'URL du logo
            Compagnie compagnie = new Compagnie(nom, adresse, telephone, email, siteWeb, description, logoUrl, siret);

            // Debug statements
            System.out.println("Nom: " + nom);
            System.out.println("Adresse: " + adresse);
            System.out.println("Téléphone: " + telephone);
            System.out.println("Email: " + email);
            System.out.println("Site Web: " + siteWeb);
            System.out.println("Description: " + description);
            System.out.println("Logo: " + logoUrl);
            System.out.println("SIRET: " + siret);

            // Ajouter la compagnie à la base de données
            CompagnieService compagnieService = new CompagnieService();
            compagnieService.ajouter(compagnie);

            // Afficher un message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("La compagnie a été ajoutée avec succès !");
            alert.showAndWait();

            // Redirection vers l'interface de don
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/pfe/novaview/Don/AjouterDon.fxml"));
                Stage stage = (Stage) txtNom.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                System.out.println("Erreur lors de la redirection : " + e.getMessage());
                showErrorAlert("Erreur de redirection", "Impossible de charger l'interface de don.");
            }

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
            showErrorAlert("Erreur SQL", e.getMessage());
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
            showErrorAlert("Erreur", "Une erreur est survenue lors de l'ajout de la compagnie.");
        }
    }

    private boolean isValidEmail(String email) {

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {

        return phoneNumber.matches("\\d{8}");
    }

    private boolean isValidWebsite(String website) {

        String websiteRegex = "^(http(s)?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(/.*)?$";
        Pattern pattern = Pattern.compile(websiteRegex);
        return pattern.matcher(website).matches();
    }

    private boolean isValidSiret(String siret) {

        return siret.matches("\\d{14}");
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
