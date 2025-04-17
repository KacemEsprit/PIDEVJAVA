package com.pfe.nova.Controller;

import com.pfe.nova.configuration.MedecinDAO;
import com.pfe.nova.configuration.PatientDAO;
import com.pfe.nova.configuration.SessionTraitementDAO;
import com.pfe.nova.models.SessionDeTraitement;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CreateSessionDeTraitementController {

    // === FXML Elements ===
    @FXML private Button creerSessionButton;
    @FXML private Button cancelButton;
    @FXML private DatePicker dateSessionPicker;
    @FXML private TextField dureeField;
    @FXML private ComboBox<Integer> medecinComboBox;
    @FXML private ComboBox<Integer> patientIdComboBox;
    @FXML private ComboBox<Integer> salleComboBox;
    @FXML private ComboBox<String> typeTraitementComboBox;


    private MedecinDAO medecinDAO = new MedecinDAO();
    private PatientDAO patientsDao = new PatientDAO();
    private SessionTraitementDAO sessionDAO = new SessionTraitementDAO();

    public CreateSessionDeTraitementController() throws SQLException {
    }


    @FXML
    public void initialize() {
        setupTypeSessionComboBox();
        setupTypeChamber();
        setupMedecinComboBox();
        try {
            setupPatientIdComboBox();
        } catch (SQLException e) {
            showError("Erreur lors du chargement des patients: " + e.getMessage());
        }
    }


    private void setupTypeSessionComboBox() {
        typeTraitementComboBox.setItems(FXCollections.observableArrayList(
                "Chimiothérapie", "Chirurgie", "Dialyse", "Réanimation"
        ));
    }
    /*  <String fx:value="Chimiothérapie" />
                                    <String fx:value="Chirurgie" />
                                    <String fx:value="Dialyse" />
                                    <String fx:value="Réanimation" />*/

    private void setupTypeChamber() {
        salleComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7,8,9,10));
    }

    private void setupMedecinComboBox() {
        List<Integer> ids = medecinDAO.getAllIds();
        medecinComboBox.setItems(FXCollections.observableArrayList(ids));
    }

    private void setupPatientIdComboBox() throws SQLException {
        List<Integer> ids = patientsDao.getAllIds();
        patientIdComboBox.setItems(FXCollections.observableArrayList(ids));
    }


    @FXML
    private void handleSave() {
        if (!validateInputs()) return;

        try {
            SessionDeTraitement session = createSessionFromInputs();
            boolean success = sessionDAO.createSession(session);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "La session de traitement a été créée avec succès.");
                clearFields();
            } else {
                showError("Échec de la création de la session.");
            }
        } catch (SQLException e) {
            showError("Erreur de base de données: " + e.getMessage());
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }
public void clearFields(){
    dateSessionPicker.setValue(null);
    typeTraitementComboBox.setValue(null);
    salleComboBox.setValue(null);
    dureeField.clear();
    medecinComboBox.setValue(null);
    patientIdComboBox.setValue(null);
}

    private SessionDeTraitement createSessionFromInputs() {
        String dateSession = dateSessionPicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String typeSession = typeTraitementComboBox.getValue();
        int numDeChambre = salleComboBox.getValue();
        int patientId = patientIdComboBox.getValue();
        int duree = Integer.parseInt(dureeField.getText());
        int medecinId = medecinComboBox.getValue();

        return new SessionDeTraitement(dateSession, typeSession, numDeChambre, patientId, duree, medecinId);
    }


    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (dateSessionPicker.getValue() == null) {
            errors.append("Veuillez sélectionner une date.\n");
        } else if (!dateSessionPicker.getValue().isAfter(LocalDate.now())) {
            errors.append("La date de session doit être supérieure à la date d'aujourd'hui.\n");
        }

        if (typeTraitementComboBox.getValue() == null) errors.append("Veuillez sélectionner un type de traitement.\n");
        if (salleComboBox.getValue() == null) errors.append("Veuillez sélectionner une salle.\n");
        if (dureeField.getText().isEmpty()) {
            errors.append("Veuillez entrer une durée.\n");
        } else {
            try {
                Integer.parseInt(dureeField.getText());
            } catch (NumberFormatException e) {
                errors.append("La durée doit être un nombre.\n");
            }
        }
        if (medecinComboBox.getValue() == null) errors.append("Veuillez sélectionner un médecin.\n");
        if (patientIdComboBox.getValue() == null) errors.append("Veuillez sélectionner un patient.\n");

        if (errors.length() > 0) {
            showError(errors.toString());
            return false;
        }
        return true;
    }


    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Erreur", message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
