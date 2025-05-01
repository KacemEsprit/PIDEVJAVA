package com.pfe.nova.Controller.Don;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import com.pfe.nova.models.Don;
import com.pfe.nova.services.DonService;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class HistoriqueDonController implements Initializable {
    @FXML private BorderPane rootPane;
    @FXML private FlowPane cardsContainer;
    @FXML private VBox chartContainer;
    @FXML private VBox statSection;
    @FXML private ComboBox<String> typeDonFilter;
    private final DonService donService = new DonService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Application du CSS custom à la racine de la vue
        rootPane.getStylesheets().add(getClass().getResource("/com/pfe/novaview/Don/historiqueDon.css").toExternalForm());
        ObservableList<String> types = FXCollections.observableArrayList();
        types.add("Tous");
        types.add("Matériel");
        types.add("Financière");
        typeDonFilter.setItems(types);
        typeDonFilter.setValue("Tous");
        typeDonFilter.setOnAction(e -> {
            loadDons();
            updateChart();
        });
        loadDons();
        updateChart();
    }

    private void loadDons() {
        List<Don> dons = donService.recupererTousLesDons();
        String selectedType = typeDonFilter != null ? typeDonFilter.getValue() : "Tous";
        cardsContainer.getChildren().clear();
        for (Don don : dons) {
            String typeDon = don.getTypeDon();
            boolean afficher = selectedType.equals("Tous")
                || (selectedType.equals("Matériel") && (typeDon != null && (typeDon.equalsIgnoreCase("materiel") || typeDon.equalsIgnoreCase("matériel"))))
                || (selectedType.equals("Financière") && (typeDon != null && (typeDon.equalsIgnoreCase("financiere") || typeDon.equalsIgnoreCase("financier"))));
            if (afficher) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/Don/DonCard.fxml"));
                    Node card = loader.load();
                    com.pfe.nova.Controller.Don.DonCardController controller = loader.getController();
                    controller.setDon(don);
                    cardsContainer.getChildren().add(card);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateChart() {
        chartContainer.getChildren().clear();
        String selectedType = typeDonFilter != null ? typeDonFilter.getValue() : "Tous";
        statSection.setVisible(!selectedType.equals("Tous"));
        statSection.setManaged(!selectedType.equals("Tous"));
        if (selectedType.equals("Financière")) {
            LineChart<String, Number> chart = createFinanceLineChart();
            chartContainer.getChildren().add(chart);
        } else if (selectedType.equals("Matériel")) {
            PieChart pie = createMaterielPieChart();
            chartContainer.getChildren().add(pie);
        }
    }

    private LineChart<String, Number> createFinanceLineChart() {
        List<Don> dons = donService.recupererTousLesDons();
        Map<String, Double> sommeParMois = new HashMap<>();
        for (Don don : dons) {
            String typeDon = don.getTypeDon();
            if (typeDon != null && (typeDon.equalsIgnoreCase("financiere") || typeDon.equalsIgnoreCase("financier"))) {
                LocalDate date = don.getDateDon() != null ? don.getDateDon().toLocalDate() : null;
                if (date != null) {
                    String mois = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
                    sommeParMois.put(mois, sommeParMois.getOrDefault(mois, 0.0) + don.getMontant());
                }
            }
        }
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mois");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Montant (€)");
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Évolution des dons financiers par mois");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Dons financiers");
        for (Map.Entry<String, Double> entry : sommeParMois.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        lineChart.getData().add(series);
        lineChart.setLegendVisible(false);
        lineChart.setPrefHeight(280);
        return lineChart;
    }

    private PieChart createMaterielPieChart() {
        List<Don> dons = donService.recupererTousLesDons();
        Map<String, Integer> repartition = new HashMap<>();
        for (Don don : dons) {
            String typeDon = don.getTypeDon();
            if (typeDon != null && (typeDon.equalsIgnoreCase("materiel") || typeDon.equalsIgnoreCase("matériel"))) {
                String objet = don.getDescriptionMateriel();
                if (objet != null && !objet.isBlank()) {
                    repartition.put(objet, repartition.getOrDefault(objet, 0) + 1);
                } else {
                    repartition.put("Autre", repartition.getOrDefault("Autre", 0) + 1);
                }
            }
        }
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : repartition.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Répartition des dons matériels par type d'objet");
        pieChart.setLegendVisible(true);
        pieChart.setPrefHeight(280);
        return pieChart;
    }
}
