package com.pfe.nova.Controller;

import com.pfe.nova.configuration.OrderDAO;
import com.pfe.nova.models.Order;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.stage.StageStyle;

public class OrderRatingController {
    @FXML private HBox starsBox;
    @FXML private Label messageLabel;
    private final OrderDAO orderDAO = new OrderDAO();
    private Order order;
    private boolean isInitialRender = true;

    public void setOrder(Order order) {
        this.order = order;
        renderStars();
    }

    private void renderStars() {
        starsBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            
            // Vérifier si c'est le rendu initial et si la commande a déjà été évaluée
            if ( i <= order.getRate()) {
                star.setStyle("-fx-font-size: 24px; -fx-cursor: hand; -fx-text-fill: gold;");
            } else {
                star.setStyle("-fx-font-size: 24px; -fx-cursor: hand; -fx-text-fill: gray;");
            }

            int currentRating = i;
            star.setOnMouseEntered(e -> previewRating(currentRating));
            star.setOnMouseExited(e -> resetStars());
            star.setOnMouseClicked(event -> {
                try {
                    orderDAO.updateOrderRating(order.getId(), currentRating);
                    order.setRate(currentRating);
                    renderStars();
                    messageLabel.setText("Note enregistrée avec succès !");
                    messageLabel.setStyle("-fx-text-fill: green;");
                    
                    PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                    delay.setOnFinished(e -> star.getScene().getWindow().hide());
                    delay.play();
                } catch (Exception e) {
                    messageLabel.setText("Erreur lors de l'enregistrement de la note");
                    messageLabel.setStyle("-fx-text-fill: red;");
                    e.printStackTrace();
                }
            });

            starsBox.getChildren().add(star);
        }
        isInitialRender = false;
    }

    private void previewRating(int rating) {
        for (int i = 0; i < starsBox.getChildren().size(); i++) {
            Label star = (Label) starsBox.getChildren().get(i);
            star.setStyle("-fx-font-size: 24px; -fx-cursor: hand;" +
                         (i < rating ? "-fx-text-fill: gold;" : "-fx-text-fill: gray;"));
        }
    }

    private void resetStars() {
        for (int i = 0; i < starsBox.getChildren().size(); i++) {
            Label star = (Label) starsBox.getChildren().get(i);
            if (i < order.getRate()) {
                star.setStyle("-fx-font-size: 24px; -fx-cursor: hand; -fx-text-fill: gold;");
            } else {
                star.setStyle("-fx-font-size: 24px; -fx-cursor: hand; -fx-text-fill: gray;");
            }
        }
    }
    
    
}
