package com.pfe.nova.Controller;



import com.pfe.nova.configuration.RapportDAO;
import com.pfe.nova.models.Rapport;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Set;

public class RapportsAdminController {

    @FXML
    private GridPane reportsGridPane;

    @FXML
    public void initialize() {
        loadReports();
    }

    private void loadReports() {
        try {
            // Clear existing content and constraints
            reportsGridPane.getChildren().clear();
            reportsGridPane.getColumnConstraints().clear();

            // Set column widths (optional: adjust as needed)
            for (int i = 0; i < 22; i++) {
                ColumnConstraints col = new ColumnConstraints();
                col.setMinWidth(120); // largeur minimale par colonne
                col.setHgrow(Priority.ALWAYS); // permet à la colonne de grandir si possible
                reportsGridPane.getColumnConstraints().add(col);
            }

            // Add header row
            reportsGridPane.add(createWrappedLabel("ID"), 0, 0);
            reportsGridPane.add(createWrappedLabel("Patient ID"), 1, 0);
            reportsGridPane.add(createWrappedLabel("Age"), 2, 0);
            reportsGridPane.add(createWrappedLabel("Date"), 3, 0);
            reportsGridPane.add(createWrappedLabel("Sexe"), 4, 0);
            reportsGridPane.add(createWrappedLabel("Tension"), 5, 0);
            reportsGridPane.add(createWrappedLabel("Pouls"), 6, 0);
            reportsGridPane.add(createWrappedLabel("Température"), 7, 0);
            reportsGridPane.add(createWrappedLabel("Saturation"), 8, 0);
            reportsGridPane.add(createWrappedLabel("IMC"), 9, 0);
            reportsGridPane.add(createWrappedLabel("Niveau Douleur"), 10, 0);
            reportsGridPane.add(createWrappedLabel("Traitement"), 11, 0);
            reportsGridPane.add(createWrappedLabel("Dose"), 12, 0);
            reportsGridPane.add(createWrappedLabel("Fréquence"), 13, 0);
            reportsGridPane.add(createWrappedLabel("Perte de Sang"), 14, 0);
            reportsGridPane.add(createWrappedLabel("Temps Opération"), 15, 0);
            reportsGridPane.add(createWrappedLabel("Durée Séance"), 16, 0);
            reportsGridPane.add(createWrappedLabel("Filtration"), 17, 0);
            reportsGridPane.add(createWrappedLabel("Créatinine"), 18, 0);
            reportsGridPane.add(createWrappedLabel("Glasgow"), 19, 0);
            reportsGridPane.add(createWrappedLabel("Respiration"), 20, 0);
            reportsGridPane.add(createWrappedLabel("Complications"), 21, 0);

            // Fetch reports from the database
            RapportDAO rapportDAO = new RapportDAO();
            Set<Rapport> rapports = rapportDAO.getAll();

            int rowIndex = 1;
            for (Rapport rapport : rapports) {
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getId())), 0, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getPatientId())), 1, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getAge())), 2, rowIndex);
                reportsGridPane.add(createWrappedLabel(rapport.getDateRapport()), 3, rowIndex);
                reportsGridPane.add(createWrappedLabel(rapport.getSexe()), 4, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getTensionArterielle())), 5, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getPouls())), 6, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getTemperature())), 7, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getSaturationOxygene())), 8, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getImc())), 9, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getNiveauDouleur())), 10, rowIndex);
                reportsGridPane.add(createWrappedLabel(rapport.getTraitement()), 11, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getDoseMedicament())), 12, rowIndex);
                reportsGridPane.add(createWrappedLabel(rapport.getFrequenceTraitement()), 13, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getPerteDeSang())), 14, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getTempsOperation())), 15, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getDureeSeance())), 16, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getFiltrationSang())), 17, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getCreatinine())), 18, rowIndex);
                reportsGridPane.add(createWrappedLabel(String.valueOf(rapport.getScoreGlasgow())), 19, rowIndex);
                reportsGridPane.add(createWrappedLabel(rapport.isRespirationAssistee() == 1 ? "Oui" : "Non"), 20, rowIndex);
                reportsGridPane.add(createWrappedLabel(rapport.getComplications()), 21, rowIndex);

                rowIndex++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour créer un label expansible et non coupé
    private Label createWrappedLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(label, Priority.ALWAYS);
        return label;
    }

}
