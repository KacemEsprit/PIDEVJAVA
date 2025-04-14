package com.pfe.nova.Controller;

import com.pfe.nova.configuration.RapportDAO;
import com.pfe.nova.models.Rapport;
import com.pfe.nova.models.User;
import com.pfe.nova.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class RapportControlleur {

    @FXML
    private TextField ageField;

    @FXML
    private TextField complicationsField;

    @FXML
    private TextField creatinineField;

    @FXML
    private Button creerRapportButton;

    @FXML
    private TextField dateRapportField;

    @FXML
    private TextField doseField;

    @FXML
    private TextField douleurField;

    @FXML
    private TextField dureeField;

    @FXML
    private TextField filtrationField;

    @FXML
    private TextField frequenceField;

    @FXML
    private TextField glasgowField;

    @FXML
    private TextField imcField;

    @FXML
    private ChoiceBox<?> patientIdChoiceBox;

    @FXML
    private TextField perteSangField;

    @FXML
    private TextField poulsField;

    @FXML
    private TextField respirationField;

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
    private TextField traitementField;

    @FXML
    public void handleCreate() {
        try {
            User utilisateur = Session.getUtilisateurConnecte();
            /*Medecin medecin = (Medecin) utilisateur;*/

            Rapport rapport = new Rapport();
            rapport.setAge(Integer.parseInt(ageField.getText()));
            rapport.setComplications(complicationsField.getText());
            rapport.setCreatinine(Integer.parseInt(creatinineField.getText()));
            rapport.setDateRapport(dateRapportField.getText());
            rapport.setDoseMedicament(Integer.parseInt(doseField.getText()));
            rapport.setNiveauDouleur(Integer.parseInt(douleurField.getText()));
            rapport.setDureeSeance(Integer.parseInt(dureeField.getText()));
            rapport.setFiltrationSang(Integer.parseInt(filtrationField.getText()));
            rapport.setFrequenceTraitement(frequenceField.getText());
            rapport.setScoreGlasgow(Integer.parseInt(glasgowField.getText()));
            rapport.setImc(Double.parseDouble(imcField.getText()));
            rapport.setPatientId((int) patientIdChoiceBox.getValue());
            /*rapport.setMedecinId(medecin.getId());*/
            rapport.setPerteDeSang(Integer.parseInt(perteSangField.getText()));
            rapport.setPouls(Integer.parseInt(poulsField.getText()));
            rapport.setRespirationAssistee(Boolean.parseBoolean(respirationField.getText()));
            rapport.setSaturationOxygene(Integer.parseInt(saturationField.getText()));
            rapport.setSexe((String) sexeChoiceBox.getValue());
            rapport.setTemperature(Double.parseDouble(temperatureField.getText()));
            rapport.setTempsOperation(Integer.parseInt(tempsOperationField.getText()));
            rapport.setTensionArterielle(Integer.parseInt(tensionField.getText()));
            rapport.setTraitement(traitementField.getText());

            RapportDAO rapportDAO = new RapportDAO();
            boolean success = rapportDAO.create(rapport);

            if (success) {
                System.out.println("Rapport créé avec succès !");
                // afficher un message à l'utilisateur ou rafraîchir la liste
            } else {
                System.out.println("Échec de la création du rapport.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la création du rapport.");
        }
    }
}



