package com.pfe.nova.Controller;
import com.pfe.nova.models.Task;
import com.pfe.nova.configuration.TaskDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.sql.SQLException;
import java.util.List;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.time.format.TextStyle;
import java.util.Objects;

import org.json.JSONObject;


public class TodoListController {

    @FXML
    private TextField taskInput;

    @FXML
    private VBox taskList;
    @FXML private Label dateLabel;
    @FXML private Label dayLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label locationLabel;
    @FXML private ImageView weatherIcon;
    @FXML
    private ImageView stateImage;
    @FXML
    public void initialize() {
        // Style du VBox principal
        taskList.setStyle("-fx-background-color: white; -fx-padding: 20;");
        taskInput.setPromptText("Ajouter une nouvelle tâche...");
        taskInput.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 10 15;");
        
        // Configurer l'action d'ajout sur la touche Entrée
        taskInput.setOnAction(e -> addTask());
        
        loadTasksFromDatabase();
        loadWeatherAndDate();
        loadStateImage();
    }

    private void loadTasksFromDatabase() {
        try {
            List<Task> tasks = TaskDAO.getAllTasks();
            taskList.getChildren().clear();
            for (Task task : tasks) {
                taskList.getChildren().add(createTaskItem(task));
            }
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void addTask() {
        String taskName = taskInput.getText().trim();
        if (!taskName.isEmpty()) {
            try {
                Task newTask = new Task();
                newTask.setName(taskName);
                newTask.setCompleted(false);
                TaskDAO.createTask(newTask);

                loadTasksFromDatabase();
                taskInput.clear();
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
    }

    private HBox createTaskItem(Task task) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: " + (task.isCompleted() ? "#e8f0fe" : "#f7fafd") +
                      "; -fx-background-radius: 10;");
    
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(task.isCompleted());
        checkBox.setStyle("-fx-background-color: white; " +
                          "-fx-border-color: #adb5bd; " +
                          "-fx-border-radius: 5; " +
                          "-fx-background-radius: 5; " +
                          "-fx-pref-width: 18px; " +
                          "-fx-pref-height: 18px;");
    
        Label taskLabel = new Label(task.getName());
        taskLabel.setStyle("-fx-font-size: 16px; " +
                           "-fx-text-fill: #212529;" +
                           (task.isCompleted() ? "-fx-strikethrough: true; -fx-text-fill: #6c757d;" : ""));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // <-- LE SPACER MAGIQUE
    
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: transparent; " +
                              "-fx-text-fill: #ff6b6b; " +
                              "-fx-font-size: 20px;");
    
        checkBox.setOnAction(e -> {
            try {
                TaskDAO.updateTaskStatus(task.getId(), checkBox.isSelected());
                taskLabel.setStyle("-fx-font-size: 16px;" +
                                   (checkBox.isSelected() ? "-fx-strikethrough: true; -fx-text-fill: #6c757d;" : "-fx-text-fill: #212529;"));
                hbox.setStyle("-fx-background-color: " + (checkBox.isSelected() ? "#e8f0fe" : "#f7fafd") +
                              "; -fx-background-radius: 10;");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });
    
        deleteButton.setOnAction(e -> {
            try {
                TaskDAO.deleteTask(task.getId());
                taskList.getChildren().remove(hbox);
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });
    
        hbox.getChildren().addAll(checkBox, taskLabel, spacer, deleteButton);
        return hbox;
    }
    
    
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void loadWeatherAndDate() {
        // Affichage date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
        dateLabel.setText(today.format(formatter));
        dayLabel.setText(today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH));
    
        // Météo
        try {
            String apiKey = "4b97318665dcd93110cd5797d55c18d1"; // Remplace avec ta vraie clé API
            String city = "Ariana";
            String urlStr = "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                            ",tn&units=metric&appid=" + apiKey + "&lang=fr";
    
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
    
            InputStream inputStream = conn.getInputStream();
            StringBuilder jsonText = new StringBuilder();
            int b;
            while ((b = inputStream.read()) != -1) {
                jsonText.append((char) b);
            }
    
            JSONObject json = new JSONObject(jsonText.toString());
            double temp = json.getJSONObject("main").getDouble("temp");
            String description = json.getJSONArray("weather").getJSONObject(0).getString("description");
            String iconCode = json.getJSONArray("weather").getJSONObject(0).getString("icon");

            temperatureLabel.setText(temp + "°C");
            locationLabel.setText("Ariana, Tunisie - " + description);
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            weatherIcon.setImage(new javafx.scene.image.Image(iconUrl));
        } catch (Exception e) {
            temperatureLabel.setText("N/A");
            locationLabel.setText("Erreur de météo");
            e.printStackTrace();
        }
    }
    private void loadStateImage() {
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/oncophoto.png")));
            stateImage.setImage(img);
            stateImage.setVisible(true); // la rendre visible après chargement
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
        }
    }
}
