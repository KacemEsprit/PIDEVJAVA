package com.pfe.nova.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

public class RatingDialogController {
    @FXML
    private Slider ratingSlider;
    
    private Stage dialogStage;
    private int rating = 0;
    private boolean submitted = false;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    @FXML
    private void handleSubmit() {
        rating = (int) ratingSlider.getValue();
        submitted = true;
        dialogStage.close();
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    public int getRating() {
        return rating;
    }
    
    public boolean isSubmitted() {
        return submitted;
    }
}