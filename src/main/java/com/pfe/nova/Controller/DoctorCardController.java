package com.pfe.nova.Controller;

import com.pfe.nova.models.Medecin;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class DoctorCardController {
    @FXML private VBox doctorCard;
    @FXML private ImageView doctorImage;
    @FXML private Label nameLabel;
    @FXML private Label specialityLabel;
    @FXML private Label experienceLabel;
    @FXML private Label contactLabel;
    @FXML private Button bookButton;

    private Medecin doctor;

    public void setDoctorData(Medecin doctor) {
        this.doctor = doctor;


        nameLabel.setText("Dr. " + doctor.getNom() + " " + doctor.getPrenom());
        specialityLabel.setText("Spécialité: " + doctor.getSpecialite());
        experienceLabel.setText("Expérience: " + doctor.getExperience() + " ans");
        contactLabel.setText("Contact: " + doctor.getTel());
    }


}