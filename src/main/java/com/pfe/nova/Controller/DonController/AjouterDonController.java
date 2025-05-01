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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

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

    @FXML
    private Button btnPayer;

    private File fichierPreuve;
    private boolean isIndividualDonor;

    @FXML
    public void initialize() {
        // Initialiser les types de don
        ObservableList<String> typesDon = FXCollections.observableArrayList("Matériel", "Financier");
        cbTypeDon.setItems(typesDon);

        cbTypeDon.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isFinancier = newVal != null && (newVal.equalsIgnoreCase("financier") || newVal.equalsIgnoreCase("financière"));
            btnPayer.setVisible(isFinancier);
            btnPayer.setManaged(isFinancier);
        });
        btnPayer.setVisible(false);
        btnPayer.setManaged(false);

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
            // Récupérer l'utilisateur connecté
            User currentUser = SessionManager.getCurrentUser();
            int donateurId = -1;
            if (currentUser instanceof Donateur) {
                donateurId = currentUser.getId();
            }
            // Récupérer uniquement les compagnies du donateur connecté qui sont validées
            ObservableList<Compagnie> compagnies = FXCollections.observableArrayList(
                compagnieService.recupererParDonateurId(donateurId).stream()
                    .filter(c -> "CONFIRMEE".equalsIgnoreCase(c.getStatut_validation()))
                    .toList()
            );
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
        } catch (Exception e) {
            e.printStackTrace();
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

    @FXML
    private void handlePayer(ActionEvent event) {
        // 1. Ajout du don dans la base de données (comme ajouterDon)
        Platform.runLater(() -> {
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
                    don = new Don(typeDon, montant, descriptionMateriel, dateDon, 0, 0, cbCompagnie.getValue().getId(), modePaiement, preuveDon);
                } else {
                    don = new Don(typeDon, montant, descriptionMateriel, dateDon, 0, 0, modePaiement, preuveDon);
                }

                // Sauvegarde du don
                DonService donService = new DonService();
                donService.ajouter(don);

                // Ensuite, lancer la logique de paiement Stripe dans un thread
                new Thread(() -> {
                    try {
                        String montantStr = txtMontant.getText();
                        int montantCents = (int) (Double.parseDouble(montantStr) * 100);
                        java.net.URL url = new java.net.URL("http://localhost/PIDEVJAVA/backend/create_checkout.php");
                        java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        con.setDoOutput(true);
                        String params = "montant=" + montantCents;
                        try (java.io.OutputStream os = con.getOutputStream()) {
                            byte[] input = params.getBytes("utf-8");
                            os.write(input, 0, input.length);
                        }
                        int status = con.getResponseCode();
                        if (status == 200) {
                            java.io.InputStream is = con.getInputStream();
                            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                            String response = s.hasNext() ? s.next() : "";
                            is.close();
                            s.close();
                            String checkoutUrl = extractStripeUrl(response);
                            if (checkoutUrl != null) {
                                javafx.application.Platform.runLater(() -> showStripeQrPopup(checkoutUrl));
                            } else {
                                javafx.application.Platform.runLater(() -> showErrorAlert("Erreur", "Impossible de récupérer l'URL de paiement Stripe."));
                            }
                        } else {
                            javafx.application.Platform.runLater(() -> showErrorAlert("Erreur", "Erreur lors de la connexion au serveur de paiement (code: " + status + ")"));
                        }
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> showErrorAlert("Erreur", "Erreur lors de la connexion à Stripe : " + e.getMessage()));
                    }
                }).start();

                // Afficher le message de succès pour tous les dons
                showSuccessAlert("Attention", "Scanner le code qr pour terminer votre donation svp!");
                clearFields();
            } catch (NumberFormatException e) {
                showErrorAlert("Erreur de format", "Le montant doit être un nombre valide.");
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Erreur lors de l'ajout du don: " + e.getMessage());
            }
        });
    }

    private String extractStripeUrl(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("url")) {
                return obj.get("url").getAsString();
            }
        } catch (Exception e) {
            // Optionnel : log l'erreur
        }
        return null;
    }

    private void showStripeQrPopup(String checkoutUrl) {
        javafx.application.Platform.runLater(() -> {
            javafx.stage.Stage qrStage = new javafx.stage.Stage();
            qrStage.setTitle("QR Code à scanner");

            // Générer le QR code à partir de checkoutUrl
            javafx.scene.image.ImageView qrView;
            try {
                com.google.zxing.Writer writer = new com.google.zxing.qrcode.QRCodeWriter();
                com.google.zxing.common.BitMatrix matrix = writer.encode(checkoutUrl, com.google.zxing.BarcodeFormat.QR_CODE, 250, 250);
                javafx.scene.image.WritableImage qrImage = new javafx.scene.image.WritableImage(250, 250);
                for (int x = 0; x < 250; x++) {
                    for (int y = 0; y < 250; y++) {
                        qrImage.getPixelWriter().setColor(x, y, matrix.get(x, y) ? javafx.scene.paint.Color.BLACK : javafx.scene.paint.Color.WHITE);
                    }
                }
                qrView = new javafx.scene.image.ImageView(qrImage);
            } catch (Exception e) {
                qrView = new javafx.scene.image.ImageView();
            }

            javafx.scene.control.Label label = new javafx.scene.control.Label("Scannez pour payer votre don");
            label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px;");
            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(20, label, qrView);
            vbox.setAlignment(javafx.geometry.Pos.CENTER);
            vbox.setStyle("-fx-padding: 30px; -fx-background-color: white;");
            javafx.scene.Scene scene = new javafx.scene.Scene(vbox);
            qrStage.setScene(scene);
            qrStage.setResizable(false);
            qrStage.show();
        });
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