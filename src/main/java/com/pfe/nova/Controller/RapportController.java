package com.pfe.nova.Controller;

import com.pfe.nova.models.Rapport;
import com.pfe.nova.services.RapportService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public class RapportController {

    @FXML
    private TextField patientIdField, medecinIdField, sexeField, ageField, traitementField, doseMedicamentField, frequenceTraitementField, perteDeSangField, tempsOperationField, filtrationSangField, creatinineField, scoreGlasgowField, respirationAssisteeField, complicationsField;

    @FXML
    private DatePicker dateRapportPicker;

    @FXML
    private TableView<Rapport> rapportTable;

    @FXML
    private TableColumn<Rapport, Integer> patientIdColumn;

    @FXML
    private TableColumn<Rapport, LocalDate> dateRapportColumn;

    @FXML
    private TableColumn<Rapport, String> sexeColumn;

    private final RapportService rapportService = new RapportService();
    private int connectedMedecinId;

    public void setConnectedMedecinId(int medecinId) {
        this.connectedMedecinId = medecinId;
    }

    @FXML
    private void handleCreateRapport() {
        try {
            // Validate and parse patientId
            String patientIdText = patientIdField.getText();
            if (!isNumeric(patientIdText)) {
                System.out.println("Invalid Patient ID. Please enter a valid number.");
                return;
            }
            int patientId = Integer.parseInt(patientIdText);

            // Create a new Rapport object
            Rapport rapport = new Rapport();
            rapport.setPatientId(patientId);
            rapport.setMedecinId(connectedMedecinId); // Use the connected doctor's ID
            rapport.setDateRapport(dateRapportPicker.getValue());
            rapport.setSexe(sexeField.getText());
            rapport.setAge(Integer.parseInt(ageField.getText()));
            rapport.setTraitement(traitementField.getText());
            rapport.setDoseMedicament(doseMedicamentField.getText());
            rapport.setFrequenceTraitement(frequenceTraitementField.getText());
            rapport.setPerteDeSang(perteDeSangField.getText());
            rapport.setTempsOperation(tempsOperationField.getText());
            rapport.setFiltrationSang(filtrationSangField.getText());
            rapport.setCreatinine(creatinineField.getText());
            rapport.setScoreGlasgow(scoreGlasgowField.getText());
            rapport.setRespirationAssistee(respirationAssisteeField.getText());
            rapport.setComplications(complicationsField.getText());

            // Call the service to create the rapport
            boolean success = rapportService.createRapport(rapport);
            if (success) {
                System.out.println("Rapport created successfully!");
                refreshTable();
            } else {
                System.out.println("Failed to create rapport.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Utility method to check if a string is numeric
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FXML
    private void handleUpdateRapport() {
        try {
            Rapport selectedRapport = rapportTable.getSelectionModel().getSelectedItem();
            if (selectedRapport == null) {
                System.out.println("No rapport selected.");
                return;
            }

            selectedRapport.setPatientId(Integer.parseInt(patientIdField.getText()));
            selectedRapport.setMedecinId(Integer.parseInt(medecinIdField.getText()));
            selectedRapport.setDateRapport(dateRapportPicker.getValue());
            selectedRapport.setSexe(sexeField.getText());
            selectedRapport.setAge(Integer.parseInt(ageField.getText()));
            selectedRapport.setTraitement(traitementField.getText());
            selectedRapport.setDoseMedicament(doseMedicamentField.getText());
            selectedRapport.setFrequenceTraitement(frequenceTraitementField.getText());
            selectedRapport.setPerteDeSang(perteDeSangField.getText());
            selectedRapport.setTempsOperation(tempsOperationField.getText());
            selectedRapport.setFiltrationSang(filtrationSangField.getText());
            selectedRapport.setCreatinine(creatinineField.getText());
            selectedRapport.setScoreGlasgow(scoreGlasgowField.getText());
            selectedRapport.setRespirationAssistee(respirationAssisteeField.getText());
            selectedRapport.setComplications(complicationsField.getText());

            boolean success = rapportService.updateRapport(selectedRapport);
            if (success) {
                System.out.println("Rapport updated successfully!");
                refreshTable();
            } else {
                System.out.println("Failed to update rapport.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteRapport() {
        try {
            Rapport selectedRapport = rapportTable.getSelectionModel().getSelectedItem();
            if (selectedRapport == null) {
                System.out.println("No rapport selected.");
                return;
            }

            boolean success = rapportService.deleteRapport(selectedRapport.getId());
            if (success) {
                System.out.println("Rapport deleted successfully!");
                refreshTable();
            } else {
                System.out.println("Failed to delete rapport.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshTable() {
        if (rapportTable == null) {
            System.out.println("TableView is not initialized.");
            return;
        }

        // Ensure columns are properly populated with data
        ObservableList<Rapport> rapports = FXCollections.observableArrayList(rapportService.getAllRapports());
        rapportTable.setItems(rapports);


    }
}
