package com.pfe.nova.Controller;

import com.pfe.nova.configuration.PatientDAO;
import com.pfe.nova.configuration.RapportDAO;
import com.pfe.nova.models.Patient;
import com.pfe.nova.models.Rapport;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ViewRapportsController {

    @FXML
    private Circle patientAvatar;
    @FXML
    private VBox patientCardsContainer;
    @FXML
    private TableView<Rapport>  patientReportsTable;

    @FXML
    private TextField searchPatientField;
    @FXML
    private Label selectedPatientName;
    @FXML
    private TableColumn<Rapport, String> ageColumn;
    @FXML
    private TableColumn<Rapport, String> dateRapportColumn;
    @FXML
    private TableColumn<Rapport, String> sexeColumn;
    @FXML
    private TableColumn<Rapport, String> tensionColumn;
    @FXML
    private TableColumn<Rapport, String> poulsColumn;
    @FXML
    private TableColumn<Rapport, String> temperatureColumn;
    @FXML
    private TableColumn<Rapport, String> saturationColumn;
    @FXML
    private TableColumn<Rapport, String> imcColumn;
    @FXML
    private TableColumn<Rapport, String> niveauDouleurColumn;
    @FXML
    private TableColumn<Rapport, String> traitementColumn;
    @FXML
    private TableColumn<Rapport, String> doseColumn;
    @FXML
    private TableColumn<Rapport, String> frequenceColumn;
    @FXML
    private TableColumn<Rapport, String> perteSangColumn;
    @FXML
    private TableColumn<Rapport, String> tempsOperationColumn;
    @FXML
    private TableColumn<Rapport, String> dureeSeanceColumn;
    @FXML
    private TableColumn<Rapport, String> filtrationColumn;
    @FXML
    private TableColumn<Rapport, String> creatinineColumn;
    @FXML
    private TableColumn<Rapport, String> glasgowColumn;
    @FXML
    private TableColumn<Rapport, String> respirationColumn;
    @FXML
    private TableColumn<Rapport, String> complicationsColumn;
@FXML
    private TableColumn<Rapport, Void> action;
    private PatientDAO patientDAO;
    private RapportDAO rapportDAO;
    @FXML
    private StackPane contrainer;
@FXML
    private StackPane contentArea;
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

    public void showReports(Patient patient) {
        try {
            // Load the AdminRapport.fxml view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/RapportsAdmin.fxml"));
            Parent root = loader.load();

            // Get the controller instance
            AdminRapportController controller = loader.getController();

            // Pass the patient to the controller
            if (patient != null) {
                controller.setPatient(patient);
            }

            // Set the loaded view into the contentArea
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load RapportsAdmin.fxml. Please check the file path.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);" +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 15; -fx-border-width: 1;");
        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);
        Circle avatar = new Circle(25, Color.web("#2980b9"));
        Label initialsLabel = new Label(getInitials(patient.getNom(), patient.getPrenom()));
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setStyle("-fx-font-weight: bold;");
        StackPane avatarPane = new StackPane(avatar, initialsLabel);
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        Label nameLabel = new Label(patient.getNom() + " " + patient.getPrenom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        Label idLabel = new Label("ID: " + patient.getId());
        idLabel.setStyle("-fx-text-fill: #666;");
        infoBox.getChildren().addAll(nameLabel, idLabel);
        Button viewReportsButton = new Button("Consulter Rapports");
        viewReportsButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-padding: 8 20;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(63,81,181,0.2), 5, 0, 0, 2);");
        viewReportsButton.setOnAction(e -> showReports(patient));
        content.getChildren().addAll(avatarPane, infoBox, viewReportsButton);
        card.getChildren().add(content);
        return card;
    }


    private String getInitials(String nom, String prenom) {
        String firstInitial = nom.isEmpty() ? "" : nom.substring(0, 1).toUpperCase();
        String secondInitial = prenom.isEmpty() ? "" : prenom.substring(0, 1).toUpperCase();
        return firstInitial + secondInitial;
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
                if (String.valueOf(patient.getId()).contains(searchText) ||
                        patient.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                        patient.getPrenom().toLowerCase().contains(searchText.toLowerCase())) {
                    patientCardsContainer.getChildren().add(createPatientCard(patient));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur" + "Erreur lors de la recherche: " + e.getMessage());
        }
    }

            private void updateRapport(Rapport rapport) {
                try {
                    Stage updateStage = new Stage();
                    updateStage.initModality(Modality.APPLICATION_MODAL);
                    updateStage.setTitle("Modifier le Rapport (Champs essentiels)");

                    GridPane form = new GridPane();
                    form.setPadding(new Insets(20));
                    form.setHgap(10);
                    form.setVgap(10);

                    // Champs essentiels à modifier
                    Label tensionLabel = new Label("Tension Artérielle:");
                    TextField tensionField = new TextField(String.valueOf(rapport.getTensionArterielle()));

                    Label poulsLabel = new Label("Pouls:");
                    TextField poulsField = new TextField(String.valueOf(rapport.getPouls()));

                    Label temperatureLabel = new Label("Température:");
                    TextField temperatureField = new TextField(String.valueOf(rapport.getTemperature()));

                    Label saturationLabel = new Label("Saturation Oxygène:");
                    TextField saturationField = new TextField(String.valueOf(rapport.getSaturationOxygene()));

                    Label traitementLabel = new Label("Traitement:");
                    TextField traitementField = new TextField(rapport.getTraitement());

                    Label complicationsLabel = new Label("Complications:");
                    TextField complicationsField = new TextField(rapport.getComplications());

                    // Ajout des champs au formulaire
                    form.add(tensionLabel, 0, 0); form.add(tensionField, 1, 0);
                    form.add(poulsLabel, 0, 1); form.add(poulsField, 1, 1);
                    form.add(temperatureLabel, 0, 2); form.add(temperatureField, 1, 2);
                    form.add(saturationLabel, 0, 3); form.add(saturationField, 1, 3);
                    form.add(traitementLabel, 0, 4); form.add(traitementField, 1, 4);
                    form.add(complicationsLabel, 0, 5); form.add(complicationsField, 1, 5);

                    // Boutons
                    Button saveButton = new Button("Enregistrer");
                    saveButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    saveButton.setOnAction(e -> {
                        try {
                            // Mise à jour de l'objet
                            rapport.setTensionArterielle(Integer.parseInt(tensionField.getText()));
                            rapport.setPouls(Integer.parseInt(poulsField.getText()));
                            rapport.setTemperature(Double.parseDouble(temperatureField.getText()));
                            rapport.setSaturationOxygene(Integer.parseInt(saturationField.getText()));
                            rapport.setTraitement(traitementField.getText());
                            rapport.setComplications(complicationsField.getText());

                            // Mise à jour base de données
                            boolean success = rapportDAO.update(rapport);
                            if (success) {
                                showSuccess("Succès", "Rapport mis à jour avec succès !");
                                patientReportsTable.refresh();
                                updateStage.close();
                            } else {
                                showError("Erreur", "Échec de la mise à jour du rapport.");
                            }
                        } catch (Exception ex) {
                            showError("Erreur", "Données invalides: " + ex.getMessage());
                        }
                    });

                    Button cancelButton = new Button("Annuler");
                    cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    cancelButton.setOnAction(e -> updateStage.close());

                    HBox buttonBox = new HBox(10, saveButton, cancelButton);
                    buttonBox.setAlignment(Pos.CENTER);
                    form.add(buttonBox, 0, 6, 2, 1);

                    Scene scene = new Scene(form, 500, 350);
                    updateStage.setScene(scene);
                    updateStage.showAndWait();
                } catch (Exception e) {
                    showError("Erreur", "Impossible d'afficher la fenêtre de modification: " + e.getMessage());
                }
            }




    private void deleteRapport(Rapport rapport) {
        boolean success = rapportDAO.delete(rapport.getId());
        if (success) {
            patientReportsTable.getItems().remove(rapport);
            showSuccess("Succès", "Rapport supprimé avec succès !");
        } else {
            showError("Erreur", "Échec de la suppression du rapport.");
        }
    }

    private void showSuccess(String succès, String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(succès);
        alert.setContentText(s);
        alert.showAndWait();
    }

}