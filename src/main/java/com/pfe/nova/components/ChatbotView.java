package com.pfe.nova.components;

import com.pfe.nova.utils.GeminiAPI;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatbotView extends VBox {
    private VBox chatMessagesContainer;
    private TextField messageField;
    private Button sendButton;
    private ScrollPane scrollPane;
    private Label statusLabel;
    private ProgressIndicator progressIndicator;

    public ChatbotView() {
        setupUI();
    }

    private void setupUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");

        // Header
        Label headerLabel = new Label("Nova Assistant (Gemini AI)");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        headerLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Chat messages container
        chatMessagesContainer = new VBox(10);
        chatMessagesContainer.setPadding(new Insets(10));

        // Scroll pane for messages
        scrollPane = new ScrollPane(chatMessagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Status indicator
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusLabel = new Label("Ready");
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(16, 16);
        progressIndicator.setVisible(false);
        statusBox.getChildren().addAll(progressIndicator, statusLabel);

        // Input area
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);

        messageField = new TextField();
        messageField.setPromptText("Type your message here...");
        messageField.setPrefHeight(40);
        HBox.setHgrow(messageField, Priority.ALWAYS);

        sendButton = new Button("Send");
        sendButton.setPrefHeight(40);
        sendButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        inputBox.getChildren().addAll(messageField, sendButton);

        // Add welcome message
        addBotMessage("Hello! I'm Nova Assistant powered by Gemini AI. How can I help you today?");

        // Add all components to the main container
        this.getChildren().addAll(headerLabel, scrollPane, statusBox, inputBox);

        // Set up event handlers
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        // Add user message to chat
        addUserMessage(message);

        // Clear input field
        messageField.clear();

        // Show loading indicator
        progressIndicator.setVisible(true);
        statusLabel.setText("Getting response...");

        // Send message to Gemini API in background thread
        new Thread(() -> {
            try {
                String response = GeminiAPI.sendMessage(message);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    addBotMessage(response);
                    progressIndicator.setVisible(false);
                    statusLabel.setText("Ready");

                    // Scroll to bottom
                    scrollPane.setVvalue(1.0);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addBotMessage("Sorry, I encountered an error: " + e.getMessage());
                    progressIndicator.setVisible(false);
                    statusLabel.setText("Error occurred");
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER_RIGHT);

        TextFlow textFlow = new TextFlow();
        Text text = new Text(message);
        text.setFill(Color.WHITE);
        textFlow.getChildren().add(text);
        textFlow.setPadding(new Insets(10));
        textFlow.setStyle("-fx-background-color: #3498db; -fx-background-radius: 15 0 15 15;");
        textFlow.setMaxWidth(300);

        messageBox.getChildren().add(textFlow);
        chatMessagesContainer.getChildren().add(messageBox);

        // Scroll to bottom
        scrollPane.setVvalue(1.0);
    }

    private void addBotMessage(String message) {
        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER_LEFT);

        TextFlow textFlow = new TextFlow();
        Text text = new Text(message);
        text.setFill(Color.BLACK);
        textFlow.getChildren().add(text);
        textFlow.setPadding(new Insets(10));
        textFlow.setStyle("-fx-background-color: #e9e9e9; -fx-background-radius: 0 15 15 15;");
        textFlow.setMaxWidth(300);

        messageBox.getChildren().add(textFlow);
        chatMessagesContainer.getChildren().add(messageBox);

        // Scroll to bottom
        scrollPane.setVvalue(1.0);
    }
}