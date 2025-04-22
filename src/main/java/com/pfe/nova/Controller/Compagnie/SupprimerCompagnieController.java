package com.pfe.nova.Controller.Compagnie;

import com.pfe.nova.models.Compagnie;
import com.pfe.nova.services.CompagnieService;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.sql.SQLException;

public class SupprimerCompagnieController {

    public boolean supprimerCompagnie(Compagnie compagnie) {
        if (compagnie != null) {

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette compagnie ?");
            alert.setContentText("Cela supprimera définitivement la compagnie.");
            alert.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    CompagnieService service = new CompagnieService();
                    service.supprimer(compagnie.getId());
                    System.out.println("Compagnie supprimée avec succès.");
                    return true;
                } catch (SQLException e) {
                    System.out.println("Erreur lors de la suppression de la compagnie : " + e.getMessage());

                    // Affichage d'une alerte graphique en cas d'erreur
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText("La suppression a échoué");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();

                    return false;
                }
            }
        }
        return false;
    }
}