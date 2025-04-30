package com.pfe.nova.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.pfe.nova.configuration.OrderDAO;
import com.pfe.nova.models.Order;
import javafx.scene.control.Alert;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

public class ReviewsStatisticsController {
    @FXML
    private VBox chartContainer;
    @FXML
    private ComboBox<String> chartTypeComboBox;
    private PieChart pieChart;
    private BarChart<String, Number> barChart;
    private LineChart<String, Number> lineChart;
    @FXML
    private PieChart reviewsPieChart;
    @FXML
    private Label totalReviewsLabel;
    @FXML
    private Label positiveReviewsLabel;
    @FXML
    private Label negativeReviewsLabel;

    private OrderDAO orderDAO;
    @FXML
    private HBox reviewsContainer;
    
    @FXML
    public void initialize() {
        
        Platform.runLater(() -> {
            try {
                // Initialiser les types de graphiques disponibles
                chartTypeComboBox.getItems().addAll("Graphique Circulaire", "Graphique en Barres", "Graphique Linéaire");
                orderDAO = new OrderDAO();
                createCharts();

                chartTypeComboBox.setValue("Graphique Circulaire");
                updateChartType();
                
                // Créer les différents types de graphiques
                
                // Ajouter un écouteur pour le changement de type
                chartTypeComboBox.setOnAction(e -> updateChartType());
                loadReviewsStatistics();
                Circle positiveCircle = new Circle(6, javafx.scene.paint.Color.web("rgb(121, 211, 234);"));
Label positiveText = new Label("Avis Positifs");
HBox positiveBox = new HBox(5, positiveCircle, positiveText);

Circle negativeCircle = new Circle(6, javafx.scene.paint.Color.web("rgb(188, 197, 65);"));
Label negativeText = new Label("Avis Négatifs");
HBox negativeBox = new HBox(5, negativeCircle, negativeText);

HBox allReviewsBox = new HBox(20, positiveBox, negativeBox);
allReviewsBox.setAlignment(Pos.CENTER);
reviewsContainer.getChildren().add(allReviewsBox);
            } catch (Exception e) {
                showError("Erreur d'initialisation", "Impossible d'initialiser les statistiques: " + e.getMessage());
            }
        });
    }

    private void createCharts() {
        // Création du PieChart
        pieChart = new PieChart();
        pieChart.setTitle("Répartition Des Avis Clients (%)");
    
        // Création du BarChart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setCategories(FXCollections.observableArrayList("Avis Positifs", "Avis Négatifs")); // 💥 ICI
        xAxis.setTickLabelRotation(0);
        yAxis.setLabel("Pourcentage");
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Répartition Des Avis Clients (%)");
    
        // Création du LineChart
        CategoryAxis xAxisLine = new CategoryAxis();
        NumberAxis yAxisLine = new NumberAxis();
        xAxisLine.setCategories(FXCollections.observableArrayList("Avis Positifs", "Avis Négatifs")); 
        xAxisLine.setTickLabelRotation(0);
        yAxisLine.setLabel("Pourcentage");
        lineChart = new LineChart<>(xAxisLine, yAxisLine);
        lineChart.setTitle("Répartition Des Avis Clients (%)");
    
        // Appliquer style gras pour les titres
        Platform.runLater(() -> {
            if (pieChart.lookup(".chart-title") != null)
                pieChart.lookup(".chart-title").setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
            if (barChart.lookup(".chart-title") != null)
                barChart.lookup(".chart-title").setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
            if (lineChart.lookup(".chart-title") != null)
                lineChart.lookup(".chart-title").setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        });
    }
    
    

    private void updateChartType() {
        chartContainer.getChildren().clear();
        javafx.scene.Node chartToShow = null;

        switch (chartTypeComboBox.getValue()) {
            case "Graphique Circulaire":
                chartContainer.getChildren().add(pieChart);
                break;
            case "Graphique en Barres":
                chartContainer.getChildren().add(barChart);
                break;
            case "Graphique Linéaire":
                chartContainer.getChildren().add(lineChart);
                break;
        }
        if (chartToShow != null) {
            chartContainer.getChildren().add(chartToShow);
    
            // 🎞️ Appliquer une animation fade
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), chartToShow);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
        loadReviewsStatistics();
    }

    private void loadReviewsStatistics() {
        try {
            // Initialiser avec des valeurs par défaut
            totalReviewsLabel.setText("0");
            positiveReviewsLabel.setText("0");
            negativeReviewsLabel.setText("0");

            List<Order> ratedOrders = orderDAO.getAllRatedOrders();
            
            if (ratedOrders == null) {
                ratedOrders = new ArrayList<>();
            }
            
            final int[] positiveReviews = {0};
            final int[] negativeReviews = {0};

            // Compter uniquement les avis avec une note
            for (Order order : ratedOrders) {
                if (order.getRate() > 2) {
                    positiveReviews[0]++;
                } else if (order.getRate() > 0) {
                    negativeReviews[0]++;
                }
            }

            // Calculer le total réel des avis (somme des positifs et négatifs)
            final int totalReviews = positiveReviews[0] + negativeReviews[0];

            final double positivePercentage = totalReviews > 0 ? 
                (positiveReviews[0] * 100.0) / totalReviews : 0;
            final double negativePercentage = totalReviews > 0 ? 
                (negativeReviews[0] * 100.0) / totalReviews : 0;

            Platform.runLater(() -> {
                // Mettre à jour les labels avec les valeurs correctes
                totalReviewsLabel.setText(String.valueOf(totalReviews));
                positiveReviewsLabel.setText(String.valueOf(positiveReviews[0]));
                negativeReviewsLabel.setText(String.valueOf(negativeReviews[0]));

                switch (chartTypeComboBox.getValue()) {
                    case "Graphique Circulaire":
                        updatePieChart(positivePercentage, negativePercentage);
                        break;
                    case "Graphique en Barres":
                        updateBarChart(positivePercentage, negativePercentage);
                        break;
                    case "Graphique Linéaire":
                        updateLineChart(positivePercentage, negativePercentage);
                        break;
                }

                // Mettre à jour les labels
                totalReviewsLabel.setText(String.valueOf(totalReviews));
                positiveReviewsLabel.setText(String.valueOf(positiveReviews[0]));
                negativeReviewsLabel.setText(String.valueOf(negativeReviews[0]));
            });
        } catch (SQLException e) {
            Platform.runLater(() -> showError("Erreur de base de données", 
                "Impossible de charger les statistiques: " + e.getMessage()));
        } catch (Exception e) {
            Platform.runLater(() -> showError("Erreur", 
                "Une erreur inattendue s'est produite: " + e.getMessage()));
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

private void updatePieChart(double positivePercentage, double negativePercentage) {
    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
        new PieChart.Data(String.format("Avis Positifs (%.1f%%)", positivePercentage), positivePercentage),
        new PieChart.Data(String.format("Avis Négatifs (%.1f%%)", negativePercentage), negativePercentage)
    );
    pieChart.setData(pieChartData);

    pieChartData.forEach(data -> {
        if (data.getName().contains("Positifs")) {
            data.getNode().setStyle("-fx-pie-color:rgb(121, 211, 234);");
        } else {
            data.getNode().setStyle("-fx-pie-color:rgb(244, 253, 121);");
        }
    });
    
    pieChart.setLegendVisible(false);
             
}

private void updateBarChart(double positivePercentage, double negativePercentage) {
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.getData().add(new XYChart.Data<>("Avis Positifs", positivePercentage));
    series.getData().add(new XYChart.Data<>("Avis Négatifs", negativePercentage));
    barChart.getData().clear();
    barChart.getData().add(series);
    barChart.setLegendVisible(false);
    for (XYChart.Data<String, Number> data : series.getData()) {
        if (data.getXValue().contains("Positifs")) {
            data.getNode().setStyle("-fx-bar-fill: rgb(121, 211, 234);"); // Bleu clair
        } else {
            data.getNode().setStyle("-fx-bar-fill: rgb(244, 253, 121);"); // Jaune clair
        }
        data.getNode().setOnMouseEntered(e -> {
            data.getNode().setStyle(data.getXValue().contains("Positifs")
                ? "-fx-bar-fill: rgb(121, 211, 234); -fx-effect: dropshadow(three-pass-box, rgb(121, 211, 234), 10, 0, 0, 0);"
                : "-fx-bar-fill: rgb(244, 253, 121); -fx-effect: dropshadow(three-pass-box, rgb(244, 253, 121), 10, 0, 0, 0);");
        });
        data.getNode().setOnMouseExited(e -> {
            data.getNode().setStyle(data.getXValue().contains("Positifs")
                ? "-fx-bar-fill: rgb(121, 211, 234);"
                : "-fx-bar-fill: rgb(244, 253, 121);");
        });
        
    }

}

private void updateLineChart(double positivePercentage, double negativePercentage) {
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.getData().add(new XYChart.Data<>("Avis Positifs", positivePercentage));
    series.getData().add(new XYChart.Data<>("Avis Négatifs", negativePercentage));
    
    lineChart.getData().clear();
    lineChart.getData().add(series);
    lineChart.setLegendVisible(false);

    for (XYChart.Data<String, Number> data : series.getData()) {
        if (data.getNode() != null) {
            if (data.getXValue().contains("Positifs")) {
                data.getNode().setStyle("-fx-background-color: rgb(121, 211, 234), white;");
            } else {
                data.getNode().setStyle("-fx-background-color: rgb(244, 253, 121), white;");
            }

            // 🔥 Ajouter le hover GLOW ici pour les points LineChart
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setStyle(data.getXValue().contains("Positifs")
                    ? "-fx-background-color: rgb(121, 211, 234), white; -fx-effect: dropshadow(three-pass-box, rgb(121, 211, 234), 10, 0, 0, 0);"
                    : "-fx-background-color: rgb(244, 253, 121), white; -fx-effect: dropshadow(three-pass-box, rgb(244, 253, 121), 10, 0, 0, 0);");
            });

            data.getNode().setOnMouseExited(e -> {
                data.getNode().setStyle(data.getXValue().contains("Positifs")
                    ? "-fx-background-color: rgb(121, 211, 234), white;"
                    : "-fx-background-color: rgb(244, 253, 121), white;");
            });
        }
    }

    if (!series.getNode().getStyleClass().contains("custom-line")) {
        series.getNode().setStyle("-fx-stroke: rgb(121, 211, 234); -fx-stroke-width: 2px;"); // Bleu clair
    }
}


}