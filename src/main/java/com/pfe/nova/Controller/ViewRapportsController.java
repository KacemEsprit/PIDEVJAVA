package com.pfe.nova.Controller;

import com.pfe.nova.configuration.PatientDAO;
import com.pfe.nova.configuration.RapportDAO;
import com.pfe.nova.models.Patient;
import com.pfe.nova.models.Rapport;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.collections.FXCollections;
import java.sql.SQLException;
import java.util.List;

public class ViewRapportsController {
    @FXML
    private TableColumn<Rapport, String> complicationsColumn;
    @FXML
    private TableColumn<Rapport, String> imcColumn;
    @FXML
    private Circle patientAvatar;
    @FXML
    private VBox patientCardsContainer;
    @FXML
    private TableView<Rapport> patientReportsTable;
    @FXML
    private TableColumn<Rapport, String> reportActionsColumn;
    @FXML
    private TableColumn<Rapport, String> reportDateColumn;
    @FXML
    private TableColumn<Rapport, String> saturationColumn;
    @FXML
    private TextField searchPatientField;
    @FXML
    private Label selectedPatientName;
    @FXML
    private TableColumn<Rapport, String> temperatureColumn;
    @FXML
    private TableColumn<Rapport, String> tensionColumn;
    @FXML
    private TableColumn<Rapport, String> traitementColumn;

    private PatientDAO patientDAO;
    private RapportDAO rapportDAO;

    @FXML
    public void initialize() {
        try {
            patientDAO = new PatientDAO();
            rapportDAO = new RapportDAO();
            loadPatients();
            setupSearch();
            System.out.println("Initializing controller...");
        } catch (SQLException e) {
            showError("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientDAO.displayPatients();
            System.out.println("Nombre de patients chargés: " + patients.size());
            patientCardsContainer.getChildren().clear();

            for (Patient patient : patients) {
                VBox card = createPatientCard(patient);
                patientCardsContainer.getChildren().add(card);
                System.out.println("Added patient: " + patient.getNom() + " " + patient.getPrenom());
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les patients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VBox createPatientCard(Patient patient) {
        VBox card = new VBox(15);
        card.getStyleClass().add("patient-card");
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);" +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 15; -fx-border-width: 1;");
        card.setPadding(new Insets(15));

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);

        // Create avatar with initials
        StackPane avatarPane = new StackPane();
        Circle avatar = new Circle(25, Color.valueOf("#1a237e"));
        Label initials = new Label(getInitials(patient.getNom(), patient.getPrenom()));
        initials.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        avatarPane.getChildren().addAll(avatar, initials);

        // Patient info
        VBox info = new VBox(5);
        Label nameLabel = new Label(patient.getNom() + " " + patient.getPrenom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        Label idLabel = new Label("ID: " + patient.getId());
        idLabel.setStyle("-fx-text-fill: #666;");
        info.getChildren().addAll(nameLabel, idLabel);

        // Consult button
        Button consultButton = new Button("Consulter Rapports");
        consultButton.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-padding: 8 20;");
        consultButton.setOnAction(e -> showPatientReports(patient));

        content.getChildren().addAll(avatarPane, info, consultButton);
        card.getChildren().add(content);

        return card;
    }

    private void setupSearch() {
        searchPatientField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterPatients(newValue.toLowerCase());
        });
    }

    private void filterPatients(String searchText) {
        try {
            List<Patient> allPatients = patientDAO.displayPatients();
            patientCardsContainer.getChildren().clear();

            for (Patient patient : allPatients) {
                if (patient.getNom().toLowerCase().contains(searchText) ||
                        patient.getPrenom().toLowerCase().contains(searchText)) {
                    patientCardsContainer.getChildren().add(createPatientCard(patient));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur" + "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    private String getInitials(String nom, String prenom) {
        String firstInitial = nom.isEmpty() ? "" : nom.substring(0, 1).toUpperCase();
        String secondInitial = prenom.isEmpty() ? "" : prenom.substring(0, 1).toUpperCase();
        return firstInitial + secondInitial;
    }

    private void showPatientReports(Patient patient) {

    }

}