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
                    displayReports(reports);
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
    
    private void displayReports(List<Map<String, Object>> reports) {
        reportsContainer.getChildren().clear();
        
        if (reports.isEmpty()) {
            statusLabel.setText("No reported comments found");
            reportCountLabel.setText("0 reports");
            
            // Add empty state illustration
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(50));
            
            Label iconLabel = new Label("🔍");
            iconLabel.setStyle("-fx-font-size: 48px;");
            
            Label messageLabel = new Label("No reported comments found");
            messageLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label subMessageLabel = new Label("All comments are in good standing");
            subMessageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            
            emptyState.getChildren().addAll(iconLabel, messageLabel, subMessageLabel);
            reportsContainer.getChildren().add(emptyState);
            return;
        }
        
        statusLabel.setText("Showing all reported comments");
        reportCountLabel.setText(reports.size() + " reports");
        
        for (Map<String, Object> report : reports) {
            reportsContainer.getChildren().add(createReportView(report));
            
            // Add separator between reports
            Separator separator = new Separator();
            separator.setPadding(new Insets(10, 0, 10, 0));
            reportsContainer.getChildren().add(separator);
        }
    }
    
    private VBox createReportView(Map<String, Object> report) {
        VBox reportBox = new VBox(15);
        reportBox.getStyleClass().add("report-card");
        reportBox.setPadding(new Insets(15));
        
        // Report header with reporter info and timestamp
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        User reporter = (User) report.get("reporter");
        Label reporterLabel = new Label("Reported by: " + reporter.getNom() + " " + reporter.getPrenom());
        reporterLabel.getStyleClass().add("report-header");
        
        String reason = (String) report.get("reason");
        Label reasonLabel = new Label("Reason: " + formatReason(reason));
        reasonLabel.getStyleClass().add("report-reason");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        LocalDateTime createdAt = (LocalDateTime) report.get("createdAt");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        Label dateLabel = new Label(createdAt.format(formatter));
        dateLabel.getStyleClass().add("report-date");
        
        header.getChildren().addAll(reporterLabel, reasonLabel, spacer, dateLabel);
        
        // Comment content with improved styling
        VBox commentBox = new VBox(10);
        commentBox.getStyleClass().add("comment-box");
        
        User commentUser = (User) report.get("commentUser");
        HBox commentHeader = new HBox(10);
        commentHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label userIconLabel = new Label("👤");
        userIconLabel.setStyle("-fx-font-size: 16px;");
        
        Label commentAuthorLabel = new Label(commentUser.getNom() + " " + commentUser.getPrenom());
        commentAuthorLabel.setStyle("-fx-font-weight: bold;");
        
        commentHeader.getChildren().addAll(userIconLabel, commentAuthorLabel);
        
        // Use TextFlow for better text wrapping
        Text commentText = new Text((String) report.get("commentContent"));
        TextFlow commentFlow = new TextFlow(commentText);
        commentFlow.setStyle("-fx-font-size: 14px;");
        
        commentBox.getChildren().addAll(commentHeader, commentFlow);
        
        // Action buttons with improved styling
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(10, 0, 0, 0));
        
        Button ignoreButton = new Button("Ignore Report");
        ignoreButton.getStyleClass().add("ignore-button");
        
        Button deleteCommentButton = new Button("Delete Comment");
        deleteCommentButton.getStyleClass().add("delete-button");
        
        int reportId = (int) report.get("id");
        int commentId = (int) report.get("commentId");
        
        ignoreButton.setOnAction(e -> handleIgnoreReport(reportId, commentId));
        deleteCommentButton.setOnAction(e -> handleDeleteComment(reportId, commentId));
        
        actions.getChildren().addAll(ignoreButton, deleteCommentButton);
        
        // Add all components to report box
        reportBox.getChildren().addAll(header, commentBox, actions);
        
        return reportBox;
    }
    
    private String formatReason(String reason) {
        switch (reason) {
            case "contenu_inapproprie":
                return "Inappropriate content";
            case "harcelement":
                return "Harassment";
            case "spam":
                return "Spam";
            case "fausse_information":
                return "False information";
            default:
                return reason;
        }
    }
    
    private void handleIgnoreReport(int reportId, int commentId) {
        // Create a confirmation dialog with improved styling
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Ignore Report");
        confirmation.setHeaderText("Ignore this report?");
        confirmation.setContentText("This will dismiss the report but keep the comment visible to all users.");
        
        // Style the dialog
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/pfe/novaview/styles/admin-styles.css").toExternalForm());
        
        // Add custom buttons
        ButtonType ignoreButtonType = new ButtonType("Ignore Report", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(ignoreButtonType, cancelButtonType);
        
        Optional<ButtonType> result = confirmation.showAndWait();
        
        if (result.isPresent() && result.get() == ignoreButtonType) {
            try {
                // Show processing indicator
                statusLabel.setText("Processing...");
                progressBar.setVisible(true);
                
                // Delete just this report
                CommentReportDAO.deleteReport(reportId);
                
                // Refresh the list
                List<Map<String, Object>> reports = CommentReportDAO.findAllReports();
                displayReports(reports);
                
                // Hide processing indicator
                progressBar.setVisible(false);
                
                // Show confirmation
                showInfo("Report has been ignored successfully");
                
            } catch (SQLException e) {
                progressBar.setVisible(false);
                showError("Error ignoring report: " + e.getMessage());
            }
        }
    }
    
    private void handleDeleteComment(int reportId, int commentId) {
        // Create a confirmation dialog with improved styling
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Comment");
        confirmation.setHeaderText("Delete this comment?");
        confirmation.setContentText("This will permanently remove the comment and all associated reports. This action cannot be undone.");
        
        // Style the dialog
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/pfe/novaview/styles/admin-styles.css").toExternalForm());
        
        // Add custom buttons with warning styling
        ButtonType deleteButtonType = new ButtonType("Delete Comment", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(deleteButtonType, cancelButtonType);
        
        // Get the delete button and style it
        Button deleteButton = (Button) dialogPane.lookupButton(deleteButtonType);
        deleteButton.getStyleClass().add("delete-button");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        
        if (result.isPresent() && result.get() == deleteButtonType) {
            try {
                // Show processing indicator
                statusLabel.setText("Processing...");
                progressBar.setVisible(true);
                
                // Delete the comment
                CommentDAO.delete(commentId);
                
                // Delete all reports for this comment
                CommentReportDAO.deleteAllForComment(commentId);
                
                // Refresh the list
                List<Map<String, Object>> reports = CommentReportDAO.findAllReports();
                displayReports(reports);
                
                // Hide processing indicator
                progressBar.setVisible(false);
                
                // Show confirmation
                showInfo("Comment has been deleted successfully");
                
            } catch (SQLException e) {
                progressBar.setVisible(false);
                showError("Error deleting comment: " + e.getMessage());
            }
        }
    }
    
    @FXML
    public void refreshReports() {
        initialize();
    }
    
    @FXML
    public void backToAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pfe/novaview/admin-posts-management.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) reportsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Posts Management");
        } catch (IOException e) {
            showError("Error returning to admin view: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/pfe/novaview/styles/admin-styles.css").toExternalForm());
        
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/pfe/novaview/styles/admin-styles.css").toExternalForm());
        
        alert.showAndWait();
    }
}