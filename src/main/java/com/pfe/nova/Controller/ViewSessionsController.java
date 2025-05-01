package com.pfe.nova.Controller;

import com.pfe.nova.configuration.SessionTraitementDAO;
import com.pfe.nova.models.SessionDeTraitement;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ViewSessionsController {

    @FXML
    private GridPane sessionGrid;

    private final SessionTraitementDAO sessionDAO = new SessionTraitementDAO();

    @FXML
    private FlowPane sessionFlow;
    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<String> sortCriteriaChoiceBox;
    private List<SessionDeTraitement> sessions;

    @FXML
    public void initialize() {
        try {
            List<SessionDeTraitement> sessions = sessionDAO.getAllSessions();
            if (sessions == null || sessions.isEmpty()) {
                System.out.println("No sessions found.");
                return;
            }

            sessionFlow.getChildren().clear();

            for (SessionDeTraitement session : sessions) {
                VBox card = new VBox(12);
                card.setPadding(new Insets(20));
                card.setPrefWidth(300); // Plus large
                card.setStyle("""
                -fx-background-color: white;
                -fx-border-radius: 15;
                -fx-background-radius: 15;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0.3, 0, 4);
                -fx-cursor: hand;
                """);

                // Hover effect
                card.setOnMouseEntered(e -> card.setStyle(
                        "-fx-background-color: #f0f8ff; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0.3, 0, 4);"
                ));
                card.setOnMouseExited(e -> card.setStyle(
                        "-fx-background-color: white; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0.3, 0, 4);"
                ));

                Label date = new Label("📅 Date: " + (session.getDateSession() != null ? session.getDateSession() : "N/A"));
                Label type = new Label("🩺 Type: " + (session.getTypeSession() != null ? session.getTypeSession() : "N/A"));
                Label salle = new Label("🏥 Salle: " + (session.getNumDeChambre() != 0 ? session.getNumDeChambre() : "N/A"));
                Label medecin = new Label("👨‍⚕️ Médecin: " + (session.getMedecinId() != 0 ? session.getMedecinId() : "N/A"));
                Label patient = new Label("🧑‍🤝‍🧑 Patient: " + (session.getPatientId() != 0 ? session.getPatientId() : "N/A"));
                Label duree = new Label("⏱ Durée: " + session.getDuree() + " min");

                // Style des labels
                for (Label label : List.of(date, type, salle, medecin, patient, duree)) {
                    label.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                }

                // Boutons
                Button updateBtn = new Button("✏️ Mettre à jour");
                updateBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 8;");
                updateBtn.setOnAction(e -> handleUpdate(session));

                Button deleteBtn = new Button("🗑 Supprimer");
                deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 8;");
                deleteBtn.setOnAction(e -> handleDelete(session));

                HBox buttonBox = new HBox(15, updateBtn, deleteBtn);
                buttonBox.setPadding(new Insets(10, 0, 0, 0));

                card.getChildren().addAll(date, type, salle, medecin, patient, duree, buttonBox);
                sessionFlow.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des sessions : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleUpdate(SessionDeTraitement session) {
        if (session == null) {
            showAlert("Erreur", "Aucune session sélectionnée.", Alert.AlertType.ERROR);
            return;
        }
        try {
            // Create a new stage for the update form
            Stage updateStage = new Stage();
            updateStage.initModality(Modality.APPLICATION_MODAL);
            updateStage.setTitle("Modifier la session");

            // Create a VBox to act as a card
            VBox card = new VBox(15);
            card.setPadding(new Insets(20));
            card.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

            // Form fields
            Label titleLabel = new Label("Modifier la session");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

            Label dateLabel = new Label("Date de la session :");
            DatePicker datePicker = new DatePicker(LocalDate.parse(session.getDateSession()));

            Label dureeLabel = new Label("Durée (minutes) :");
            TextField dureeField = new TextField(String.valueOf(session.getDuree()));

            // Buttons
            Button updateButton = new Button("Mettre à jour");
            updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
            updateButton.setOnAction(e -> {
                try {
                    if (datePicker.getValue() == null || dureeField.getText().trim().isEmpty()) {
                        showAlert("Erreur", "Tous les champs doivent être remplis.", Alert.AlertType.ERROR);
                        return;
                    }

                    session.setDateSession(datePicker.getValue().toString());
                    session.setDuree(Integer.parseInt(dureeField.getText()));

                    boolean isUpdated = sessionDAO.updateSession(session);
                    if (isUpdated) {
                        showAlert("Succès", "La session a été mise à jour avec succès.", Alert.AlertType.INFORMATION);
                        updateStage.close();
                        initialize(); // Rafraîchir l'affichage
                    } else {
                        showAlert("Erreur", "Impossible de mettre à jour la session. Veuillez réessayer.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Erreur", "Veuillez entrer une durée valide.", Alert.AlertType.ERROR);
                } catch (SQLException ex) {
                    showAlert("Erreur", "Une erreur est survenue lors de la mise à jour.", Alert.AlertType.ERROR);
                }
            });

            Button cancelButton = new Button("Annuler");
            cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
            cancelButton.setOnAction(e -> updateStage.close());

            HBox buttonBox = new HBox(10, updateButton, cancelButton);
            buttonBox.setStyle("-fx-alignment: center;");

            // Add components to the card
            card.getChildren().addAll(titleLabel, dateLabel, datePicker, dureeLabel, dureeField, buttonBox);

            // Create a scene and display the stage
            Scene scene = new Scene(card, 400, 300);
            updateStage.setScene(scene);
            updateStage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ouverture de la fenêtre de mise à jour : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    /*public void refreshSessions() {
        sessionGrid.getChildren().clear();
        initialize();
    }*/


private void handleDelete(SessionDeTraitement session) {
        try {
            boolean deleted = sessionDAO.deleteSession(session.getId());
            if (deleted) {
                showAlert("Succès", "Session ID: " + session.getId() + " supprimée avec succès.", Alert.AlertType.INFORMATION);
                initialize(); // Recharger les données
            } else {
                showAlert("Erreur", "Échec de la suppression. Veuillez réessayer.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur système", "Une erreur inattendue est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title); // Définir le titre de l'alerte
        alert.setHeaderText(null); // Supprimer l'entête de l'alerte
        alert.setContentText(msg); // Définir le message de l'alerte
        alert.showAndWait(); // Afficher l'alerte et attendre la réponse de l'utilisateur
    }
}
