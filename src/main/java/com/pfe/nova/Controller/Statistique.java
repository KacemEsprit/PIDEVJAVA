package com.pfe.nova.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.nova.configuration.PatientDAO;
import com.pfe.nova.configuration.RapportDAO;
import com.pfe.nova.models.Patient;
import com.pfe.nova.models.Rapport;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.*;

public class Statistique implements Initializable {

    @FXML
    private Label headerLabel;

    @FXML
    private ComboBox<Patient> patientComboBox;

    @FXML
    private PieChart donutChart;

    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private LineChart<String, Number> pulseChart;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private LineChart<String, Number> temperatureChart;

    @FXML
    private LineChart<String, Number> oxygenChart;
    private PatientDAO patientDAO;
    private final RapportDAO rapportDAO = new RapportDAO();
    @FXML
    private Label ewsLabel;
    @FXML
    private Button predictButton;
    @FXML
    private VBox stateBox;
    @FXML
    private ImageView stateImage;
    @FXML
    private Label patientStateLabel;
    @FXML
    private Label patientStateLabel2;
    @FXML
    private HBox etat;
    @FXML
    private ImageView aiIcon;
    @Override
public void initialize(URL location, ResourceBundle resources) {
    try {
        patientDAO = new PatientDAO();
        List<Patient> patients = patientDAO.displayPatients();
        ObservableList<Patient> patientList = FXCollections.observableArrayList(patients);
        patientComboBox.setItems(patientList);

        // Set a StringConverter to display "Nom Prénom" in the ComboBox
        patientComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Patient patient) {
                return patient != null ? patient.getNom() + " " + patient.getPrenom() : "";
            }

            @Override
            public Patient fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        // Listener pour la sélection d’un patient
        patientComboBox.setOnAction(event -> handleReload());

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    @FXML
    private void handleReload() {
        Patient selectedPatient = patientComboBox.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            headerLabel.setText("Statistique d'état de " + selectedPatient.getNom() + " " + selectedPatient.getPrenom());
            updateCharts();
            predictButton.setVisible(true);
            aiIcon.setImage(new Image(getClass().getResourceAsStream("/images/ai.gif")));
            aiIcon.setStyle("-fx-opacity: 1;"); // Make it fully visible

            // Clear the state label and image
            patientStateLabel.setText("");
            patientStateLabel.setVisible(false);
            etat.setVisible(false);
            stateImage.setImage(null);
            stateImage.setVisible(false);
        }
    }


    private void updateCharts() {
        Patient selectedPatient = patientComboBox.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) return;

        try {
            List<Rapport> rapports = rapportDAO.getRapportsByPatientID(selectedPatient.getId());
            updateDonutChart(rapports);
            updateLineChart(rapports);
            updateBarChartFrequenceVsPerte(rapports);
            calculateEWS(rapports); // Calculate and display the EWS
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDonutChart(List<Rapport> rapports) {
        int normal = (int) rapports.stream().filter(r -> r.getTensionArterielle() < 120).count();
        int elevated = (int) rapports.stream().filter(r -> r.getTensionArterielle() >= 120 && r.getTensionArterielle() < 140).count();
        int high = (int) rapports.stream().filter(r -> r.getTensionArterielle() >= 140).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Normal", normal),
                new PieChart.Data("Élevé", elevated),
                new PieChart.Data("Haut", high)
        );
        donutChart.setData(pieData);
    }

    private void updateLineChart(List<Rapport> rapports) {
        // Clear existing data
        lineChart.getData().clear();

        // Series for Tension Artérielle
        XYChart.Series<String, Number> tensionSeries = new XYChart.Series<>();
        tensionSeries.setName("Tension Artérielle");

        // Series for Oxygène
        XYChart.Series<String, Number> oxygenSeries = new XYChart.Series<>();
        oxygenSeries.setName("Oxygène");

        // Series for Pouls
        XYChart.Series<String, Number> pulseSeries = new XYChart.Series<>();
        pulseSeries.setName("Pouls");

        // Series for Température
        XYChart.Series<String, Number> temperatureSeries = new XYChart.Series<>();
        temperatureSeries.setName("Température");

        // Populate data for each series
        for (Rapport rapport : rapports) {
            tensionSeries.getData().add(new XYChart.Data<>(rapport.getDateRapport(), rapport.getTensionArterielle()));
            oxygenSeries.getData().add(new XYChart.Data<>(rapport.getDateRapport(), rapport.getSaturationOxygene()));
            pulseSeries.getData().add(new XYChart.Data<>(rapport.getDateRapport(), rapport.getPouls()));
            temperatureSeries.getData().add(new XYChart.Data<>(rapport.getDateRapport(), rapport.getTemperature()));
        }

        // Add all series to the chart
        lineChart.getData().addAll(tensionSeries, oxygenSeries, pulseSeries, temperatureSeries);

        // Apply custom colors using CSS
        lineChart.lookupAll(".series0").forEach(node -> node.setStyle("-fx-stroke: #3498db;")); // Blue for Tension
        lineChart.lookupAll(".series1").forEach(node -> node.setStyle("-fx-stroke: #2ecc71;")); // Green for Oxygen
        lineChart.lookupAll(".series2").forEach(node -> node.setStyle("-fx-stroke: #e74c3c;")); // Red for Pulse
        lineChart.lookupAll(".series3").forEach(node -> node.setStyle("-fx-stroke: #f1c40f;")); // Yellow for Temperature
    }
    private void calculateEWS(List<Rapport> rapports) {

        int ewsScore = 0;

        for (Rapport rapport : rapports) {

            if (rapport.getTensionArterielle() < 90 || rapport.getTensionArterielle() > 140) {
                ewsScore += 3;
            }
            if (rapport.getPouls() < 60 || rapport.getPouls() > 100) {
                ewsScore += 2;
            }
            if (rapport.getTemperature() < 36 || rapport.getTemperature() > 38) {
                ewsScore += 2;
            }
            if (rapport.getSaturationOxygene() < 92) {
                ewsScore += 3;
            }
        }

        // Update the EWS label
        ewsLabel.setText(String.valueOf(ewsScore));

        // Add clinical alert if the score is greater than 5
        if (ewsScore< 30) {
            ewsLabel.setStyle("-fx-background-color: #2ecc71; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        } else if (ewsScore < 40) {
            ewsLabel.setStyle("-fx-background-color: #f1c40f; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        } else {
            ewsLabel.setStyle("-fx-background-color: #e74c3c; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        }
    }

    private void updateBarChartFrequenceVsPerte(List<Rapport> rapports) {
        // Vider les anciennes données
        barChart.getData().clear();

        // Map: fréquence => [somme perte de sang, nombre d'occurrences]
        Map<String, int[]> frequenceMap = new HashMap<>();

        for (Rapport rapport : rapports) {
            String frequence = rapport.getFrequenceTraitement();
            int perteDeSang = rapport.getPerteDeSang();

            if (frequence == null || frequence.isEmpty()) continue;

            frequenceMap.putIfAbsent(frequence, new int[2]);
            frequenceMap.get(frequence)[0] += perteDeSang; // somme perte de sang
            frequenceMap.get(frequence)[1] += 1;            // compteur
        }

        // Série à afficher
        XYChart.Series<String, Number> moyennePerteSeries = new XYChart.Series<>();
        moyennePerteSeries.setName("Perte de Sang Moyenne");

        for (Map.Entry<String, int[]> entry : frequenceMap.entrySet()) {
            String frequence = entry.getKey();
            int[] valeurs = entry.getValue();

            double moyenne = (double) valeurs[0] / valeurs[1];
            moyennePerteSeries.getData().add(new XYChart.Data<>(frequence, moyenne));
        }

        barChart.setCategoryGap(50); // espace entre les groupes de catégories
        barChart.setBarGap(10);      // espace entre les barres d'une même catégorie

        barChart.getData().add(moyennePerteSeries);
    }



    /////prediction Aiiiiiiiiiiiiiiii

    @FXML
    private void handlePredict() {
        Patient selectedPatient = patientComboBox.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            showAlert("Erreur", "Veuillez sélectionner un patient avant de prédire.");
            return;
        }

        try {
            Rapport lastRapport = RapportDAO.getLatestPatientRapport(selectedPatient.getId());
            if (lastRapport == null) {
                showAlert("Erreur", "Aucun rapport trouvé pour ce patient.");
                return;
            }

            // Prediction logic (unchanged)
            Map<String, Object> features = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order
            features.put("age", lastRapport.getAge());
            features.put("sexe", "Homme".equals(lastRapport.getSexe()) ? 1 : 0);
            features.put("tension_arterielle", lastRapport.getTensionArterielle());
            features.put("pouls", lastRapport.getPouls());
            features.put("temperature", lastRapport.getTemperature());
            features.put("saturation_oxygene", lastRapport.getSaturationOxygene());
            features.put("imc", lastRapport.getImc());
            features.put("niveau_douleur", lastRapport.getNiveauDouleur());
            features.put("traitement", mapTraitement(lastRapport.getTraitement()));
            features.put("dose_medicament", lastRapport.getDoseMedicament());
            features.put("frequence_traitement", mapFrequence(lastRapport.getFrequenceTraitement()));
            features.put("perte_sang", lastRapport.getPerteDeSang());
            features.put("temps_operation", lastRapport.getTempsOperation());
            features.put("duree_seance", lastRapport.getDureeSeance());
            features.put("filtration_sang", lastRapport.getFiltrationSang());
            features.put("creatinine", lastRapport.getCreatinine());
            features.put("score_glasgow", lastRapport.getScoreGlasgow());
            features.put("respiration_assistee", lastRapport.getRespirationAssistee());
            features.put("complications", mapComplications(lastRapport.getComplications()));
            // Add other features...

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(Map.of("features", features));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:5000/predict"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        try {
                            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
                            List<String> predictions = (List<String>) response.get("prediction");
                            if (predictions == null || predictions.isEmpty()) {
                                Platform.runLater(() -> showAlert("Erreur", "Aucune prédiction trouvée."));
                                return;
                            }

                            String prediction = predictions.get(0);

                            Platform.runLater(() -> {
                                showAlert("Prédiction", "L'état du patient est : " + prediction);
                                patientStateLabel.setText("État du patient : " + prediction);
                                patientStateLabel.setVisible(true);
                                stateImage.setVisible(true);
                                etat.setVisible(true);

                                // Set the image and style based on the prediction
                                switch (prediction.toLowerCase()) {
                                    case "critique":

                                        stateImage.setImage(new Image(getClass().getResourceAsStream("/images/critique.gif"))); // Load the GIF
                                        patientStateLabel.setStyle("-fx-text-fill: #000000; -fx-background-color: #ff0000; -fx-padding: 10; -fx-background-radius: 5;");
                                        break;
                                    case "modere":
                                        stateImage.setImage(new Image(getClass().getResourceAsStream("/images/modere.jpg")));
                                        patientStateLabel.setStyle("-fx-text-fill: #000000; -fx-background-color: #fff8e1; -fx-padding: 10; -fx-background-radius: 5;");
                                        break;
                                    case "sévère":
                                        stateImage.setImage(new Image(getClass().getResourceAsStream("/images/sévère.jpg")));
                                        patientStateLabel.setStyle("-fx-text-fill: #000000; -fx-background-color: #fdf2e9; -fx-padding: 10; -fx-background-radius: 5;");
                                        break;
                                    case "stable":
                                        stateImage.setImage(new Image(getClass().getResourceAsStream("/images/stable.gif")));
                                        patientStateLabel.setStyle("-fx-text-fill: #000000; -fx-background-color: #e8f5e9; -fx-padding: 10; -fx-background-radius: 5;");
                                        break;
                                    default:
                                        stateImage.setImage(new Image(getClass().getResourceAsStream("/images/PNG.jpg")));
                                        patientStateLabel.setStyle("-fx-text-fill: #000000 ; -fx-background-color: #ecf0f1; -fx-padding: 10; -fx-background-radius: 5;");
                                        break;
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> showAlert("Erreur", "Réponse inattendue de l'API."));
                        }
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        Platform.runLater(() -> showAlert("Erreur", "Erreur lors de l'appel à l'API : " + e.getMessage()));
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite : " + e.getMessage());
        }
    }

    private int mapTraitement(String traitement) {
        if (traitement == null) return 0;
        switch (traitement.toLowerCase().trim()) {
            case "chirurgie":
                return 0;
            case "chimiothérapie":
                return 1;
            case "dialyse":
                return 2;
            case "réanimation":
                return 3;
            default:
                return 0;
        }
    }

    private int mapFrequence(String frequence) {
        if (frequence == null) return 0;
        switch (frequence.toLowerCase().trim()) {
            case "hebdomadaire":
                return 1;
            case "mensuel":
                return 2;
            case "quotidien":
                return 3;
            default:
                return 0;
        }
    }

    private int mapComplications(String complications) {
        if (complications == null || complications.trim().isEmpty()) {
            return 0;
        }
        return (int) (Math.random() * 3) + 1; // Random value between 1 and 3
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
