package com.pfe.nova.Controller.Don;

import com.pfe.nova.models.Don;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class DonCardController {
    @FXML private VBox rootVBox;
    @FXML private Label lblTypeDon;
    @FXML private Text txtDescription;
    @FXML private Label lblMontant;
    @FXML private Label lblDate;
    @FXML private Label lblModePaiement;
    @FXML private Label lblPreuve;
    @FXML private Label lblCampagneId;
    @FXML private Label lblDonateur;
    @FXML private Label lblTypeDonateur;
    @FXML private Label lblBeneficiaire;

    public void initialize() {
        if (rootVBox != null) {
            rootVBox.setOnMouseClicked(event -> toggleCardSize());
        }
    }

    private void toggleCardSize() {
        if (rootVBox.getStyleClass().contains("selected")) {
            rootVBox.getStyleClass().remove("selected");
        } else {
            rootVBox.getStyleClass().add("selected");
        }
    }

    public void setDon(Don don) {
        lblTypeDon.setText(don.getTypeDon());
        txtDescription.setText(don.getDescriptionMateriel());
        lblMontant.setText("Montant : " + don.getMontant() + " €");
        lblDate.setText("Date : " + (don.getDateDon() != null ? don.getDateDon().toString() : "N/A"));
        lblModePaiement.setText("Paiement : " + don.getModePaiement());
        lblPreuve.setText("Preuve : " + don.getPreuveDon());
        lblCampagneId.setText("Campagne ID : " + don.getCampagneId());
        lblDonateur.setText("Donateur : " + (don.getDonateur() != null ? don.getDonateur() : "N/A"));
        lblTypeDonateur.setText("Type : " + (don.getTypeDonateur() != null ? don.getTypeDonateur() : "Individuel"));
        lblBeneficiaire.setText("Bénéficiaire : " + (don.getBeneficiaire() != null ? don.getBeneficiaire() : "N/A"));
    }
}
