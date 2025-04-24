package com.pfe.nova.Controller.DonController;

import com.pfe.nova.models.Compagnie;
import com.pfe.nova.models.Don;
import com.pfe.nova.models.Donateur;
import com.pfe.nova.models.User;
import com.pfe.nova.services.CompagnieService;
import com.pfe.nova.services.DonService;
import com.pfe.nova.utils.SessionManager;
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
    private ComboBox<String> cbTypeDon;

    @FXML
    private ComboBox<Compagnie> cbCompagnie;

    @FXML
    private TextField txtMontant;

    @FXML
    private TextArea txtDescriptionMateriel;

    @FXML
    private DatePicker dpDateDon;

    @FXML
    private TextField txtModePaiement;

    @FXML
    private Label lblPreuveDon;

    private File fichierPreuve;
    private boolean isIndividualDonor;

    @FXML
    public void initialize() {
        // Initialiser les types de don
        ObservableList<String> typesDon = FXCollections.observableArrayList("Matériel", "Financier");
        cbTypeDon.setItems(typesDon);

        // Vérifier le type de donateur
        User currentUser = SessionManager.getCurrentUser();
        System.out.println("Current user: " + currentUser);

        if (currentUser instanceof Donateur) {
            Donateur donateur = (Donateur) currentUser;
            String donateurType = donateur.getDonateurType();
            System.out.println("Donateur type: " + donateurType);

            isIndividualDonor = "INDIVIDUEL".equalsIgnoreCase(donateurType);
            System.out.println("Is individual donor: " + isIndividualDonor);

            if (isIndividualDonor) {
                System.out.println("Hiding company selection for individual donor");
                // Cacher le choix de compagnie pour les donateurs individuels
                cbCompagnie.setVisible(false);
                cbCompagnie.setManaged(false); // Pour ne pas laisser d'espace vide
                // Cacher aussi le label et l'icône
                cbCompagnie.getParent().setVisible(false);
                cbCompagnie.getParent().setManaged(false);
            } else {
                System.out.println("Initializing companies for company donor");
                // Initialiser les compagnies pour les donateurs non-individuels
                initializeCompagnies();
            }
        } else {
            System.out.println("User is not a Donateur");
        }
    }

    private void initializeCompagnies() {
        try {
            CompagnieService compagnieService = new CompagnieService();
            ObservableList<Compagnie> compagnies = FXCollections.observableArrayList(compagnieService.getAll());
            cbCompagnie.setItems(compagnies);

            // Configuration de l'affichage des compagnies
            cbCompagnie.setCellFactory(param -> new ListCell<Compagnie>() {
                @Override
                protected void updateItem(Compagnie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });

            cbCompagnie.setButtonCell(new ListCell<Compagnie>() {
                @Override
                protected void updateItem(Compagnie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Impossible de charger les compagnies: " + e.getMessage());
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

        File fichier = fileChooser.showOpenDialog(null);
        if (fichier != null) {
            fichierPreuve = fichier;
            lblPreuveDon.setText(fichier.getName());
        }
    }

    @FXML
    void ajouterDon(ActionEvent event) {
        try {
            // Validation des champs obligatoires
            if (cbTypeDon.getValue() == null ||
                    txtMontant.getText().isEmpty() ||
                    fichierPreuve == null ||
                    (!isIndividualDonor && cbCompagnie.getValue() == null)) {

                showErrorAlert("Champs obligatoires", "Veuillez remplir tous les champs obligatoires et sélectionner un fichier de preuve.");
                return;
            }

            // Récupération des données
            String typeDon = cbTypeDon.getValue();
            double montant = Double.parseDouble(txtMontant.getText());
            String descriptionMateriel = txtDescriptionMateriel.getText();
            Date dateDon = dpDateDon.getValue() != null ? Date.valueOf(dpDateDon.getValue()) : null;
            String modePaiement = txtModePaiement.getText();
            String preuveDon = fichierPreuve.getName();

            // Création du don
            Don don;
            if (!isIndividualDonor && cbCompagnie.getValue() != null) {
                // Don avec ID de compagnie pour les donateurs non-individuels
                don = new Don(typeDon, montant, descriptionMateriel, dateDon, 0, 0, cbCompagnie.getValue().getId(), modePaiement, preuveDon);
            } else {
                // Don sans ID de compagnie pour les donateurs individuels
                don = new Don(typeDon, montant, descriptionMateriel, dateDon, 0, 0, modePaiement, preuveDon);
            }

            // Sauvegarde du don
            DonService donService = new DonService();
            donService.ajouter(don);

            showSuccessAlert("Succès", "Don ajouté avec succès!");
            clearFields();

        } catch (NumberFormatException e) {
            showErrorAlert("Erreur de format", "Le montant doit être un nombre valide.");
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Erreur lors de l'ajout du don: " + e.getMessage());
        }
    }

    private void clearFields() {
        cbTypeDon.setValue(null);
        txtMontant.clear();
        txtDescriptionMateriel.clear();
        dpDateDon.setValue(null);
        txtModePaiement.clear();
        lblPreuveDon.setText("");
        fichierPreuve = null;
        if (!isIndividualDonor) {
            cbCompagnie.setValue(null);
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void annuler(ActionEvent event) {
        clearFields();
        txtMontant.getScene().getWindow().hide();
    }
}