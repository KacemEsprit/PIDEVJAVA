package com.pfe.nova.Controller;

import com.pfe.nova.configuration.SessionTraitementDAO;
import com.pfe.nova.models.SessionDeTraitement;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
    public void initialize() {
        try {
            List<SessionDeTraitement> sessions = sessionDAO.getAllSessions();

            int row = 0;
            for (SessionDeTraitement session : sessions) {
                // Création des labels
                Label date = new Label("📅 " + session.getDateSession());
                Label type = new Label("🩺 " + session.getTypeSession());
                Label salle = new Label("🏥 Salle: " + session.getNumDeChambre());
                Label medecin = new Label("👨‍⚕️ Médecin: " + session.getMedecinId());
                Label patient = new Label("🧑‍🤝‍🧑 Patient: " + session.getPatientId());
                Label duree = new Label("⏱ " + session.getDuree() + " min");

                // Styles pour rendre les labels plus grands et visibles
                date.setStyle("-fx-font-size: 16px;");
                type.setStyle("-fx-font-size: 16px;");
                salle.setStyle("-fx-font-size: 16px;");
                medecin.setStyle("-fx-font-size: 16px;");
                patient.setStyle("-fx-font-size: 16px;");
                duree.setStyle("-fx-font-size: 16px;");

                // Boutons
                Button updateBtn = new Button("Mettre à jour");
                updateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
                updateBtn.setMinWidth(150); // taille plus grande pour les boutons
                updateBtn.setOnAction(e -> handleUpdate(session));

                Button deleteBtn = new Button("Supprimer");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
                deleteBtn.setMinWidth(150); // taille plus grande pour les boutons
                deleteBtn.setOnAction(e -> handleDelete(session));

                // Ajout dans le GridPane avec espacement et plus de taille
                sessionGrid.add(date, 0, row);
                sessionGrid.add(type, 1, row);
                sessionGrid.add(salle, 2, row);
                sessionGrid.add(medecin, 3, row);
                sessionGrid.add(patient, 4, row);
                sessionGrid.add(duree, 5, row);
                sessionGrid.add(updateBtn, 6, row);
                sessionGrid.add(deleteBtn, 7, row);

                row++;
            }

            // Modifier les espacements
            sessionGrid.setHgap(20); // Espacement horizontal plus grand
            sessionGrid.setVgap(20); // Espacement vertical plus grand
            sessionGrid.setPadding(new Insets(20)); // Ajouter un padding autour de la grille

            // Adapter la taille des colonnes
            for (int i = 0; i < sessionGrid.getColumnCount(); i++) {
                sessionGrid.getColumnConstraints().add(new ColumnConstraints(200)); // Ajuster la taille de chaque colonne
            }

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des sessions : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleUpdate(SessionDeTraitement session) {
        try {
            // Création de la fenêtre de mise à jour (un "Stage")
            Stage updateStage = new Stage();
            updateStage.initModality(Modality.APPLICATION_MODAL); // Empêche de revenir à la fenêtre principale avant de fermer celle-ci
            updateStage.setTitle("Modifier la session");

            // Utilisation d'un GridPane pour organiser les champs (comme une grille)
            GridPane updateForm = new GridPane();
            updateForm.setVgap(10);
            updateForm.setHgap(10);
            updateForm.setPadding(new Insets(20));

            // Champs de texte pour modifier les informations de la session
            Label dateLabel = new Label("Date de la session :");
            DatePicker datePicker = new DatePicker(LocalDate.parse(session.getDateSession())); // Afficher la date existante
            Label dureeLabel = new Label("Durée (minutes) :");
            TextField dureeField = new TextField(String.valueOf(session.getDuree())); // Afficher la durée existante

            // Ajouter les champs à la fenêtre de mise à jour
            updateForm.add(dateLabel, 0, 0);
            updateForm.add(datePicker, 1, 0);
            updateForm.add(dureeLabel, 0, 1);
            updateForm.add(dureeField, 1, 1);

            // Bouton "Mettre à jour"
            Button updateButton = new Button("Mettre à jour");
            updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            updateButton.setOnAction(e -> {
                try {
                    // Validation des champs
                    if (datePicker.getValue() == null || dureeField.getText().trim().isEmpty()) {
                        showAlert("Erreur", "Tous les champs doivent être remplis.", Alert.AlertType.ERROR);
                        return; // Ne pas continuer si les champs sont vides
                    }

                    // Récupérer les nouvelles valeurs des champs
                    session.setDateSession(datePicker.getValue().toString()); // Mettre à jour la date
                    session.setDuree(Integer.parseInt(dureeField.getText())); // Mettre à jour la durée

                    // Sauvegarder les nouvelles valeurs dans la base de données
                    boolean isUpdated = sessionDAO.updateSession(session); // Cette méthode met à jour la session dans la base de données

                    if (isUpdated) {
                        showAlert("Succès", "La session a été mise à jour avec succès.", Alert.AlertType.INFORMATION);
                        updateStage.close(); // Fermer la fenêtre après une mise à jour réussie
                       /* refreshSessions();*/
                    } else {
                        showAlert("Erreur", "Impossible de mettre à jour la session.", Alert.AlertType.ERROR);
                    }

                } catch (NumberFormatException ex) {
                    showAlert("Erreur", "Veuillez entrer une durée valide.", Alert.AlertType.ERROR);
                } catch (SQLException ex) {
                    showAlert("Erreur lors de la mise à jour", "Une erreur est survenue lors de la mise à jour de la session.", Alert.AlertType.ERROR);
                }
            });

            // Bouton "Annuler"
            Button cancelButton = new Button("Annuler");
            cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            cancelButton.setOnAction(e -> updateStage.close()); // Fermer la fenêtre sans rien modifier

            // Ajouter les boutons en bas de la fenêtre
            HBox buttonBox = new HBox(10, updateButton, cancelButton);
            updateForm.add(buttonBox, 0, 2, 2, 1);

            // Créer une scène et afficher la fenêtre
            Scene updateScene = new Scene(updateForm, 300, 200);
            updateStage.setScene(updateScene);
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
                sessionGrid.getChildren().clear();
                initialize(); // Recharger les données
            } else {
                showAlert("Erreur", "Échec de la suppression.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
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
