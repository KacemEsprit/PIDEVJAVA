package com.pfe.nova.Controller;

import com.pfe.nova.configuration.PatientDAO;
import com.pfe.nova.configuration.RapportDAO;
import com.pfe.nova.models.Medecin;
import com.pfe.nova.models.Rapport;
import com.pfe.nova.models.User;
import com.pfe.nova.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert.AlertType;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import com.pfe.nova.models.Patient;
public class RapportControlleur implements Initializable {

    @FXML
    private TextField ageField;

    @FXML
    private TextField complicationsField;

    @FXML
    private TextField creatinineField;

    @FXML
    private Button creerRapportButton;


    @FXML
    private DatePicker dateRapportField;

    @FXML
    private TextField doseField;

    @FXML
    private TextField douleurField;

    @FXML
    private TextField dureeField;

    @FXML
    private TextField filtrationField;

    @FXML
    private  ChoiceBox<String> frequenceField;

    @FXML
    private TextField glasgowField;

    @FXML
    private TextField imcField;

    @FXML
    private ComboBox<Integer> patientIdChoiceBox;

    @FXML
    private TextField perteSangField;

    @FXML
    private TextField poulsField;

    @FXML
    private ChoiceBox<String> respirationField;

    @FXML
    private TextField saturationField;

    @FXML
    private ChoiceBox<?> sexeChoiceBox;

    @FXML
    private TextField temperatureField;

    @FXML
    private TextField tempsOperationField;

    @FXML
    private TextField tensionField;

    @FXML
    private  ChoiceBox<String>  traitementField;


    public void handleCreate() {
        try {
            User currentUser = Session.getUtilisateurConnecte();
            if (currentUser instanceof Medecin) {
                // Input validation
                if (ageField.getText().isEmpty() || !ageField.getText().matches("\\d+")) {
                    showError("Validation Error", "L'âge doit être un nombre entier.");
                    return;
                }
                if (dateRapportField.getValue() == null) {
                    showError("Validation Error", "La date du rapport est obligatoire.");
                    return;
                }
                if (sexeChoiceBox.getValue() == null) {
                    showError("Validation Error", "Le sexe est obligatoire.");
                    return;
                }
                if (patientIdChoiceBox.getValue() == null) {
                    showError("Validation Error", "Veuillez sélectionner un ID de patient.");
                    return;
                }
                if (tensionField.getText().isEmpty() || !tensionField.getText().matches("\\d+")) {
                    showError("Validation Error", "La tension artérielle doit être un nombre entier.");
                    return;
                }
                if (poulsField.getText().isEmpty() || !poulsField.getText().matches("\\d+")) {
                    showError("Validation Error", "Le pouls doit être un nombre entier.");
                    return;
                }
                if (temperatureField.getText().isEmpty() || !temperatureField.getText().matches("\\d+(\\.\\d+)?")) {
                    showError("Validation Error", "La température doit être un nombre valide.");
                    return;
                }
                if (saturationField.getText().isEmpty() || !saturationField.getText().matches("\\d+")) {
                    showError("Validation Error", "La saturation en oxygène doit être un nombre entier.");
                    return;
                }
                if (imcField.getText().isEmpty() || !imcField.getText().matches("\\d+(\\.\\d+)?")) {
                    showError("Validation Error", "L'IMC doit être un nombre valide.");
                    return;
                }
                if (douleurField.getText().isEmpty() || !douleurField.getText().matches("\\d+")) {
                    showError("Validation Error", "Le niveau de douleur doit être un nombre entier.");
                    return;
                }
                if (doseField.getText().isEmpty() || !doseField.getText().matches("\\d+")) {
                    showError("Validation Error", "La dose de médicament doit être un nombre entier.");
                    return;
                }
                if (frequenceField.getValue().isEmpty()) {
                    showError("Validation Error", "La fréquence de traitement est obligatoire.");
                    return;
                }
                if (dureeField.getText().isEmpty() || !dureeField.getText().matches("\\d+")) {
                    showError("Validation Error", "La durée de la séance doit être un nombre entier.");
                    return;
                }
                if (perteSangField.getText().isEmpty() || !perteSangField.getText().matches("\\d+")) {
                    showError("Validation Error", "La perte de sang doit être un nombre entier.");
                    return;
                }
                if (tempsOperationField.getText().isEmpty() || !tempsOperationField.getText().matches("\\d+")) {
                    showError("Validation Error", "Le temps d'opération doit être un nombre entier.");
                    return;
                }
                if (filtrationField.getText().isEmpty() || !filtrationField.getText().matches("\\d+")) {
                    showError("Validation Error", "La filtration de sang doit être un nombre entier.");
                    return;
                }
                if (creatinineField.getText().isEmpty() || !creatinineField.getText().matches("\\d+")) {
                    showError("Validation Error", "La créatinine doit être un nombre entier.");
                    return;
                }
                if (glasgowField.getText().isEmpty() || !glasgowField.getText().matches("\\d+")) {
                    showError("Validation Error", "Le score de Glasgow doit être un nombre entier.");
                    return;
                }
                if (respirationField.getValue().isEmpty() ) {
                    showError("Validation Error", "La respiration assistée doit être 'true' ou 'false'.");
                    return;
                }
                if (frequenceField.getValue() == null) {
                    showError("Validation Error", "La fréquence de traitement est obligatoire.");
                    return;
                }

                // Create the Rapport object
                Rapport rapport = new Rapport();
                rapport.setAge(Integer.parseInt(ageField.getText()));
                rapport.setComplications(complicationsField.getText());
                rapport.setCreatinine(Integer.parseInt(creatinineField.getText()));
                rapport.setDateRapport(String.valueOf(dateRapportField.getValue()));
                rapport.setDoseMedicament(Integer.parseInt(doseField.getText()));
                rapport.setNiveauDouleur(Integer.parseInt(douleurField.getText()));
                rapport.setDureeSeance(Integer.parseInt(dureeField.getText()));
                rapport.setFiltrationSang(Integer.parseInt(filtrationField.getText()));
                rapport.setFrequenceTraitement(frequenceField.getValue());
                rapport.setScoreGlasgow(Integer.parseInt(glasgowField.getText()));
                rapport.setImc(Double.parseDouble(imcField.getText()));
                rapport.setPatientId(patientIdChoiceBox.getValue());
                rapport.setMedecinId(currentUser.getId());
                rapport.setPerteDeSang(Integer.parseInt(perteSangField.getText()));
                rapport.setPouls(Integer.parseInt(poulsField.getText()));
                if(respirationField.getValue()=="True"){
                    rapport.setRespirationAssistee(1);
                }
                else {
                    rapport.setRespirationAssistee(0);
                }

                rapport.setSaturationOxygene(Integer.parseInt(saturationField.getText()));
                rapport.setSexe((String) sexeChoiceBox.getValue());
                rapport.setTemperature(Double.parseDouble(temperatureField.getText()));
                rapport.setTempsOperation(Integer.parseInt(tempsOperationField.getText()));
                rapport.setTensionArterielle(Integer.parseInt(tensionField.getText()));
                rapport.setTraitement(traitementField.getValue());

                // Save the Rapport to the database
                RapportDAO rapportDAO = new RapportDAO();
                boolean success = rapportDAO.create(rapport);

                if (success) {
                    showSuccess("Succès", "Rapport créé avec succès !");
                    // Clear the fields after successful creation
                    clearFields(); // Clear the fields
                } else {
                    showError("Erreur", "Échec de la création du rapport.");
                }
            }
        } catch (NumberFormatException e) {
            showError("Validation Error", "Veuillez vérifier les champs numériques.");
        } catch (Exception e) {
            showError("Erreur", "Une erreur inattendue s'est produite : " + e.getMessage());
        }
    }
    private void clearFields() {
        ageField.clear();
        complicationsField.clear();
        creatinineField.clear();
        dateRapportField.setValue(null);
        doseField.clear();
        douleurField.clear();
        dureeField.clear();
        filtrationField.clear();
        frequenceField.setValue(null);
        glasgowField.clear();
        imcField.clear();
        patientIdChoiceBox.setValue(null);
        perteSangField.clear();
        poulsField.clear();
        respirationField.setValue(null);
        saturationField.clear();
        sexeChoiceBox.setValue(null);
        temperatureField.clear();
        tempsOperationField.clear();
        tensionField.clear();
        traitementField.setValue(null);
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            PatientDAO patientsDao = new PatientDAO();
            List<Integer> ids = patientsDao.getAllIds();

            if (ids.isEmpty()) {
                showError("Debug Information", "No Patient IDs found.");
            } else {
                patientIdChoiceBox.setItems(FXCollections.observableArrayList(ids));

            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load patient IDs: " + e.getMessage());
        }
    }

        // Add this method after initialize()
        private void showError(String title, String message) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}



