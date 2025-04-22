package com.pfe.nova.Controller.DonController;

import com.pfe.nova.models.Compagnie;
import com.pfe.nova.models.Don;
import com.pfe.nova.services.CompagnieService;
import com.pfe.nova.services.DonService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Date;
import java.sql.SQLException;

public class AjouterDonController {

    @FXML
    private ComboBox<String> cbTypeDon; // ComboBox for Type de Don

    @FXML
    private ComboBox<Compagnie> cbCompagnie; // ComboBox for Compagnie

    @FXML
    private TextField txtMontant;

    @FXML
    private TextField txtDescriptionMateriel;

    @FXML
    private DatePicker dpDateDon;

    @FXML
    private TextField txtModePaiement;

    @FXML
    private Label lblPreuveDon;

    private File fichierPreuve; // Stocke le fichier sélectionné

    @FXML
    public void initialize() {
        // Initialiser les options du ComboBox pour le type de don
        ObservableList<String> typesDon = FXCollections.observableArrayList("Matériel", "Financier");
        cbTypeDon.setItems(typesDon);

        // Initialiser les options du ComboBox pour les compagnies
        try {
            CompagnieService compagnieService = new CompagnieService();
            ObservableList<Compagnie> compagnies = FXCollections.observableArrayList(compagnieService.getAll());
            cbCompagnie.setItems(compagnies);
            
            // Configurer l'affichage des compagnies dans le ComboBox
            cbCompagnie.setCellFactory(param -> new javafx.scene.control.ListCell<Compagnie>() {
                @Override
                protected void updateItem(Compagnie item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            
            // Configurer l'affichage de la compagnie sélectionnée
            cbCompagnie.setButtonCell(new javafx.scene.control.ListCell<Compagnie>() {
                @Override
                protected void updateItem(Compagnie item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger la liste des compagnies.");
        }
    }

    @FXML
    void choisirFichier(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier de preuve de don");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );

        // Ouvrir le sélecteur de fichiers
        File fichier = fileChooser.showOpenDialog(null);
        if (fichier != null) {
            fichierPreuve = fichier;
            lblPreuveDon.setText(fichier.getName()); // Affiche le nom du fichier sélectionné
        } else {
            lblPreuveDon.setText("Aucun fichier sélectionné");
        }
    }

    @FXML
    void ajouterDon(ActionEvent event) {
        try {
            // Vérifications des champs obligatoires
            if (cbTypeDon.getValue() == null || txtMontant.getText().isEmpty() || fichierPreuve == null || cbCompagnie.getValue() == null) {
                showErrorAlert("Champs obligatoires", "Veuillez remplir tous les champs obligatoires et sélectionner un fichier de preuve.");
                return;
            }

            // Capture des données
            String typeDon = cbTypeDon.getValue(); // Récupérer la valeur sélectionnée
            double montant = Double.parseDouble(txtMontant.getText());
            String descriptionMateriel = txtDescriptionMateriel.getText();
            Date dateDon = Date.valueOf(dpDateDon.getValue());
            String modePaiement = txtModePaiement.getText();
            String preuveDon = fichierPreuve.getAbsolutePath(); // Chemin complet du fichier

            // Debug statements
            System.out.println("Type de Don: " + typeDon);
            System.out.println("Montant: " + montant);
            System.out.println("Description du Matériel: " + descriptionMateriel);
            System.out.println("Date du Don: " + dateDon);
            System.out.println("ID Compagnie: " + cbCompagnie.getValue().getId());
            System.out.println("Mode de Paiement: " + modePaiement);
            System.out.println("Preuve de Don: " + preuveDon);

            // Créer un objet Don
            Compagnie compagnieSelectionnee = cbCompagnie.getValue();
            Don don = new Don(typeDon, montant, descriptionMateriel, dateDon, 0, 0, modePaiement, preuveDon);

            // Ajouter le Don à la base de données
            DonService donService = new DonService();
            donService.ajouter(don);

            // Afficher un message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Le don a été ajouté avec succès !");
            alert.showAndWait();

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
            showErrorAlert("Erreur SQL", e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Erreur de format : " + e.getMessage());
            showErrorAlert("Erreur de format", "Veuillez vérifier les champs numériques.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur de date : " + e.getMessage());
            showErrorAlert("Erreur de date", "Veuillez vérifier le format de la date (yyyy-mm-dd).");
        }
    }

    private Date validateAndParseDate(String dateString) {
        try {
            return Date.valueOf(dateString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Format de date invalide. Utilisez 'yyyy-mm-dd'.");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void annuler(ActionEvent event) {
        // Réinitialiser tous les champs
        cbTypeDon.setValue(null);
        cbCompagnie.setValue(null);
        txtMontant.clear();
        dpDateDon.setValue(null);
        txtDescriptionMateriel.clear();
        txtModePaiement.clear();
        lblPreuveDon.setText("Aucun fichier sélectionné");
        fichierPreuve = null;

        // Fermer la fenêtre
        txtMontant.getScene().getWindow().hide();
    }
}