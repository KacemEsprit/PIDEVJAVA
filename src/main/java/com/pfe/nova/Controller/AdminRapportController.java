package com.pfe.nova.Controller;

import com.pfe.nova.configuration.RapportDAO;
import com.pfe.nova.models.Patient;
import com.pfe.nova.models.Rapport;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AdminRapportController {

    @FXML
    private ListView<HBox> rapportListView;

    @FXML
    private VBox detailsModal;

    @FXML
    private TextArea detailsTextArea;

    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<String> sortCriteriaChoiceBox;

    private List<Rapport> rapports;

    // Champ ajouté pour mémoriser le patient
    private Patient currentPatient;

    @FXML
    private void initialize() throws SQLException {
        RapportDAO rapportDAO = new RapportDAO();

        // Load reports based on the current patient or all reports
        if (currentPatient != null) {
            rapports = rapportDAO.getRapportsByPatientID(currentPatient.getId());
        } else {
            rapports = rapportDAO.getAlls();
        }

        populateListView();

        // Add listener for search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterList(newValue));

        // Populate the ChoiceBox with sorting options (only once)
        if (sortCriteriaChoiceBox.getItems().isEmpty()) {
            sortCriteriaChoiceBox.getItems().addAll("Sort by Oxygen", "Sort by Pulse", "Sort by Temperature", "Sort by Blood Loss");
            sortCriteriaChoiceBox.setValue("Sort by Oxygen"); // Set default value
        }
    }

    @FXML
    private void handleSortSelection() {
        String selectedCriteria = sortCriteriaChoiceBox.getValue();

        if (selectedCriteria == null || rapports == null) {
            return; // Exit if no criteria or reports are available
        }

        // Sort the reports based on the selected criteria
        switch (selectedCriteria) {
            case "Sort by Oxygen":
                rapports.sort((r1, r2) -> Integer.compare(r2.getSaturationOxygene(), r1.getSaturationOxygene()));
                break;
            case "Sort by Pulse":
                rapports.sort((r1, r2) -> Integer.compare(r2.getPouls(), r1.getPouls()));
                break;
            case "Sort by Temperature":
                rapports.sort((r1, r2) -> Double.compare(r2.getTemperature(), r1.getTemperature()));
                break;
            case "Sort by Blood Loss":
                rapports.sort((r1, r2) -> Double.compare(r2.getPerteDeSang(), r1.getPerteDeSang()));
                break;
            default:
                return; // Exit if no valid criteria is selected
        }

        // Refresh the ListView with the sorted reports
        refreshListView();
    }

    private void refreshListView() {
        rapportListView.getItems().clear(); // Clear the current items
        populateListView(); // Repopulate the ListView with the updated list
    }
    private void populateListView() {
        rapportListView.getItems().clear();
        for (Rapport rapport : rapports) {
            HBox card = createRapportCard(rapport);
            rapportListView.getItems().add(card);
        }
    }

    private HBox createRapportCard(Rapport rapport) {
        HBox card = new HBox();
        card.setStyle("-fx-padding: 20; -fx-border-color: #dcdcdc; -fx-border-width: 1; "
                + "-fx-background-color: #f9f9f9; -fx-background-radius: 15; -fx-border-radius: 15; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");
        card.setSpacing(20);

        VBox textContainer = new VBox(10);
        textContainer.setStyle("-fx-alignment: top-left;");

        Text patientInfo = new Text("👤 Patient: " + rapport.getPatientId());
        patientInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Text ageInfo = new Text("👶 Age: " + rapport.getAge() + " years");
        ageInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        Text tensionInfo = new Text("💓 Tension: " + rapport.getTensionArterielle());
        tensionInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        Text oxygenInfo = new Text("🌬️ Oxygen: " + rapport.getSaturationOxygene() + "%");
        oxygenInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        textContainer.getChildren().addAll(patientInfo, ageInfo, tensionInfo, oxygenInfo);

        // Show Details Button
        Button detailsButton = new Button("Show Details ▼");
        detailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; "
                + "-fx-padding: 10 20; -fx-background-radius: 10;");
        detailsButton.setOnAction(e -> showDetailsModal(rapport));

        // Update Button
        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px; "
                + "-fx-padding: 10 20; -fx-background-radius: 10;");
        updateButton.setOnAction(e -> showUpdateModal(rapport));

        // Delete Button
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; "
                + "-fx-padding: 10 20; -fx-background-radius: 10;");
        deleteButton.setOnAction(e -> deleteRapport(rapport));

        // Button Container
        HBox buttonContainer = new HBox(10, detailsButton, updateButton, deleteButton);
        buttonContainer.setStyle("-fx-alignment: bottom-right; -fx-padding: 10;");
        buttonContainer.setSpacing(20);

        VBox cardContent = new VBox(10, textContainer, buttonContainer);
        cardContent.setStyle("-fx-alignment: top-left;");

        card.getChildren().add(cardContent);
        return card;
    }
private void showUpdateModal(Rapport rapport) {
    Stage updateStage = new Stage();
    updateStage.setTitle("Update Report");

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    form.setPadding(new Insets(20));

    // Form Fields
    TextField tensionField = new TextField(String.valueOf(rapport.getTensionArterielle()));
    TextField poulsField = new TextField(String.valueOf(rapport.getPouls()));
    TextField temperatureField = new TextField(String.valueOf(rapport.getTemperature()));
    TextField saturationField = new TextField(String.valueOf(rapport.getSaturationOxygene()));
    TextField traitementField = new TextField(rapport.getTraitement());
    TextField complicationsField = new TextField(rapport.getComplications());

    form.add(new Label("Tension Arterielle:"), 0, 0);
    form.add(tensionField, 1, 0);
    form.add(new Label("Pouls:"), 0, 1);
    form.add(poulsField, 1, 1);
    form.add(new Label("Temperature:"), 0, 2);
    form.add(temperatureField, 1, 2);
    form.add(new Label("Saturation Oxygene:"), 0, 3);
    form.add(saturationField, 1, 3);
    form.add(new Label("Traitement:"), 0, 4);
    form.add(traitementField, 1, 4);
    form.add(new Label("Complications:"), 0, 5);
    form.add(complicationsField, 1, 5);

    // Save Button
    Button saveButton = new Button("Save");
    saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
    saveButton.setOnAction(e -> {
        try {
            // Update the report object
            rapport.setTensionArterielle(Integer.parseInt(tensionField.getText()));
            rapport.setPouls(Integer.parseInt(poulsField.getText()));
            rapport.setTemperature(Double.parseDouble(temperatureField.getText()));
            rapport.setSaturationOxygene(Integer.parseInt(saturationField.getText()));
            rapport.setTraitement(traitementField.getText());
            rapport.setComplications(complicationsField.getText());

            // Update in the database
            boolean success = new RapportDAO().update(rapport);
            if (success) {
                showSuccess("Success", "Report updated successfully!");
                refreshListView();
                updateStage.close();
            } else {
                showError("Error", "Failed to update the report.");
            }
        } catch (Exception ex) {
            showError("Error", "Invalid data: " + ex.getMessage());
        }
    });

    // Cancel Button
    Button cancelButton = new Button("Cancel");
    cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
    cancelButton.setOnAction(e -> updateStage.close());

    HBox buttonBox = new HBox(10, saveButton, cancelButton);
    buttonBox.setAlignment(Pos.CENTER);
    form.add(buttonBox, 0, 6, 2, 1);

    Scene scene = new Scene(form, 500, 400);
    updateStage.setScene(scene);
    updateStage.showAndWait();
}

public void showSuccess(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
public void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
private void deleteRapport(Rapport rapport) {
    boolean success = new RapportDAO().delete(rapport.getId());
    if (success) {
        rapports.remove(rapport);
        refreshListView();
        showSuccess("Success", "Report deleted successfully!");
    } else {
        showError("Error", "Failed to delete the report.");
    }
}
    private void showDetailsModal(Rapport rapport) {
        detailsModal.getChildren().clear();

        VBox frame = new VBox(10);
        frame.setStyle("-fx-padding: 20; -fx-border-color: #2980b9; -fx-border-width: 2; "
                + "-fx-border-radius: 10; -fx-background-color: #ffffff; -fx-background-radius: 10;");

        Text title = new Text("Medical Report");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Text date = new Text("Date: " + rapport.getDateRapport());
        date.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        VBox header = new VBox(5, title, date);
        header.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 0 0 1 0;");

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15;");
        content.getChildren().addAll(
                createDetailRow("Patient ID", String.valueOf(rapport.getPatientId())),
                createDetailRow("Age", String.valueOf(rapport.getAge())),
                createDetailRow("Sexe", rapport.getSexe()),
                createDetailRow("Tension Arterielle", String.valueOf(rapport.getTensionArterielle())),
                createDetailRow("Pouls", String.valueOf(rapport.getPouls())),
                createDetailRow("Temperature", String.valueOf(rapport.getTemperature())),
                createDetailRow("Saturation Oxygene", rapport.getSaturationOxygene() + "%"),
                createDetailRow("IMC", String.valueOf(rapport.getImc())),
                createDetailRow("Traitement", rapport.getTraitement()),
                createDetailRow("Complications", rapport.getComplications())
        );

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; "
                + "-fx-padding: 5 15; -fx-background-radius: 5;");
        closeButton.setOnAction(e -> closeModal());

        VBox footer = new VBox(closeButton);
        footer.setStyle("-fx-alignment: center; -fx-padding: 10;");

        frame.getChildren().addAll(header, content, footer);
        detailsModal.getChildren().add(frame);
        detailsModal.setVisible(true);
    }

    private HBox createDetailRow(String label, String value) {
        Text labelText = new Text(label + ":");
        labelText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Text valueText = new Text(value);
        valueText.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        return new HBox(10, labelText, valueText);
    }

    private void filterList(String query) {
        rapportListView.getItems().clear();
        RapportDAO rapportDAO = new RapportDAO();
        for (Rapport rapport : rapports) {
            if (String.valueOf(rapport.getPatientId()).contains(query) ||
                    rapportDAO.getPatientName(rapport.getId()).toLowerCase().contains(query.toLowerCase())) {
                rapportListView.getItems().add(createRapportCard(rapport));
            }
        }
    }


    @FXML
    private void closeModal() {
        detailsModal.setVisible(false);
    }

    // Correction ici : on mémorise le patient et on relance initialize()
    public void setPatient(Patient patient) throws SQLException {
        this.currentPatient = patient;
        initialize(); // Recharge la liste selon le patient défini
    }
}
