package com.pfe.nova.Controller;

import com.pfe.nova.configuration.CommentReportDAO;
import com.pfe.nova.configuration.CommentDAO;
import com.pfe.nova.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class ReportedCommentsController {

    @FXML private VBox reportsContainer;
    @FXML private Label statusLabel;
    @FXML private Label reportCountLabel;
    @FXML private ProgressBar progressBar;

    @FXML
    public void initialize() {
        // Show loading indicator
        statusLabel.setText("Loading reported comments...");
        progressBar.setVisible(true);

        // Use a separate thread to load data
        Thread loadThread = new Thread(() -> {
            try {
                List<Map<String, Object>> reports = CommentReportDAO.findAllReports();

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    displayReportsAsCards(reports);
                    progressBar.setVisible(false);
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Error loading reported comments: " + e.getMessage());
                    progressBar.setVisible(false);
                    statusLabel.setText("Error loading data");
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void displayReportsAsCards(List<Map<String, Object>> reports) {
        reportsContainer.getChildren().clear();
        
        if (reports.isEmpty()) {
            statusLabel.setText("No reported comments found");
            reportCountLabel.setText("0 reports");
            
            // Add empty state message
            Label emptyLabel = new Label("No reported comments found");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-padding: 20px;");
            reportsContainer.getChildren().add(emptyLabel);
            return;
        }
        
        statusLabel.setText("Showing all reported comments");
        reportCountLabel.setText(reports.size() + " reports");
        
        // Create a FlowPane to hold the cards with spacing
        FlowPane cardsPane = new FlowPane();
        cardsPane.setHgap(20); // Horizontal gap between cards
        cardsPane.setVgap(20); // Vertical gap between cards
        cardsPane.setPadding(new Insets(20));
        cardsPane.setPrefWrapLength(900); // Adjust based on your window width
        
        // Add each report as a card
        for (Map<String, Object> report : reports) {
            VBox card = createReportCard(report);
            cardsPane.getChildren().add(card);
        }
        
        reportsContainer.getChildren().add(cardsPane);
    }
    
    private VBox createReportCard(Map<String, Object> report) {
        // Create a card container
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setMinHeight(300);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("report-card");
        
        // Add card styling
        card.setStyle("-fx-background-color: white; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8;");
        
        // Report header with reporter info
        HBox reporterInfo = new HBox(10);
        reporterInfo.setAlignment(Pos.CENTER_LEFT);
        
        User reporter = (User) report.get("reporter");
        Label reporterLabel = new Label("🚩 Reported by: " + reporter.getNom() + " " + reporter.getPrenom());
        reporterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        reporterInfo.getChildren().add(reporterLabel);
        
        // Report reason
        String reason = (String) report.get("reason");
        Label reasonLabel = new Label(formatReason(reason));
        reasonLabel.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; " +
                "-fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 12px;");
        
        // Report date
        LocalDateTime createdAt = (LocalDateTime) report.get("createdAt");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        Label dateLabel = new Label("📅 " + createdAt.format(formatter));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        // Comment content section
        VBox commentBox = new VBox(8);
        commentBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");
        
        // Comment author
        User commentUser = (User) report.get("commentUser");
        Label commentAuthorLabel = new Label("👤 " + commentUser.getNom() + " " + commentUser.getPrenom());
        commentAuthorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        // Comment content with text wrapping
        String commentContent = (String) report.get("commentContent");
        Label contentLabel = new Label(commentContent);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 13px;");
        
        commentBox.getChildren().addAll(commentAuthorLabel, contentLabel);
        
        // Add a separator
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 5 0;");
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button ignoreButton = new Button("Ignore Report");
        ignoreButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        
        Button deleteButton = new Button("Delete Comment");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        
        int reportId = (int) report.get("id");
        int commentId = (int) report.get("commentId");
        
        ignoreButton.setOnAction(e -> handleIgnoreReport(reportId, commentId));
        deleteButton.setOnAction(e -> handleDeleteComment(reportId, commentId));
        
        actionButtons.getChildren().addAll(ignoreButton, deleteButton);
        
        // Add a spacer to push buttons to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Add all components to the card
        card.getChildren().addAll(
                reporterInfo,
                reasonLabel,
                dateLabel,
                separator,
                commentBox,
                spacer,
                actionButtons
        );
        
        return card;
    }
    
    private String formatReason(String reason) {
        // Format the reason code to a user-friendly string
        switch (reason) {
            case "contenu_inapproprie":
                return "Inappropriate Content";
            case "harcelement":
                return "Harassment";
            case "spam":
                return "Spam";
            case "fausse_information":
                return "False Information";
            default:
                return reason;
        }
    }

    @FXML
    public void refreshReports() {
        statusLabel.setText("Loading reported comments...");
        progressBar.setVisible(true);

        Thread loadThread = new Thread(() -> {
            try {
                List<Map<String, Object>> reports = CommentReportDAO.findAllReports();

                javafx.application.Platform.runLater(() -> {
                    displayReportsAsCards(reports);
                    progressBar.setVisible(false);
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Error loading reported comments: " + e.getMessage());
                    progressBar.setVisible(false);
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void handleIgnoreReport(int reportId, int commentId) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Ignore Report");
        confirmation.setHeaderText("Ignore Comment Report");
        confirmation.setContentText("Are you sure you want to ignore this report? The comment will remain visible to users.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the report but keep the comment
                CommentReportDAO.delete(reportId);
                
                // Update the comment's reported status
                CommentDAO.updateReportStatus(commentId, false, null);
                
                // Show success message
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Report Ignored");
                success.setHeaderText(null);
                success.setContentText("The report has been ignored successfully.");
                success.showAndWait();
                
                // Refresh the reports list
                refreshReports();
            } catch (SQLException e) {
                showError("Error ignoring report: " + e.getMessage());
            }
        }
    }
    
    private void handleDeleteComment(int reportId, int commentId) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Comment");
        confirmation.setHeaderText("Delete Reported Comment");
        confirmation.setContentText("Are you sure you want to delete this comment? This action cannot be undone.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the comment (this should cascade delete the report)
                CommentDAO.delete(commentId);
                
                // Show success message
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Comment Deleted");
                success.setHeaderText(null);
                success.setContentText("The comment has been deleted successfully.");
                success.showAndWait();
                
                // Refresh the reports list
                refreshReports();
            } catch (SQLException e) {
                showError("Error deleting comment: " + e.getMessage());
            }
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}