package com.pfe.nova.Controller;

import com.pfe.nova.configuration.UserDAO;
import com.pfe.nova.models.User;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsController {
    @FXML private Label totalUsersLabel;
    @FXML private PieChart userRoleChart;

    @FXML
    public void initialize() {
        loadStatistics();
    }

    private void loadStatistics() {
        // Load users statistics
        List<User> users = UserDAO.getAllUsers();
        totalUsersLabel.setText(String.valueOf(users.size()));

        // User role distribution chart
        Map<String, Long> roleDistribution = users.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
        
        userRoleChart.setData(
            roleDistribution.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList))
        );
    }

    public void refreshData() {
        loadStatistics();
    }
}