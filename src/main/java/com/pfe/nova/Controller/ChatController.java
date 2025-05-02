package com.pfe.nova.Controller;

import com.pfe.nova.configuration.ChatDAO;
import com.pfe.nova.configuration.UserDAO;
import com.pfe.nova.models.Chat;
import com.pfe.nova.models.User;
import com.pfe.nova.utils.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class ChatController {

    @FXML private ListView<Chat> chatListView;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Label userCountLabel;

    private ObservableList<Chat> messages = FXCollections.observableArrayList();
    private Timer timer;

    @FXML
    private void initialize() {
        // Initialize message list
        chatListView.setItems(messages);
        chatListView.setCellFactory(listView -> new ListCell<Chat>() {
            private ContextMenu contextMenu = new ContextMenu();

            {
                MenuItem editItem = new MenuItem("Edit");
                MenuItem deleteItem = new MenuItem("Delete");
                contextMenu.getItems().addAll(editItem, deleteItem);

                editItem.setOnAction(e -> {
                    Chat chat = getItem();
                    if (chat != null && isCurrentUserMessage(chat)) {
                        showEditDialog(chat);
                    }
                });

                deleteItem.setOnAction(e -> {
                    Chat chat = getItem();
                    if (chat != null && isCurrentUserMessage(chat)) {
                        deleteMessage(chat);
                    }
                });
            }

            @Override
            protected void updateItem(Chat chat, boolean empty) {
                super.updateItem(chat, empty);
                if (empty || chat == null) {
                    setText(null);
                    setStyle("");
                    setContextMenu(null);
                } else {
                    String formattedMessage = String.format("[%s] %s: %s",
                            chat.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            chat.getUsername(), chat.getMessage());
                    setText(formattedMessage);

                    // Style as a chat bubble
                    User currentUser = Session.getUtilisateurConnecte();
                    String currentUsername = currentUser != null ? currentUser.getNom() + currentUser.getPrenom() : "";
                    if (chat.getUsername().equals(currentUsername)) {
                        setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                "-fx-background-radius: 15 15 0 15; -fx-padding: 10; " +
                                "-fx-alignment: center-right; -fx-margin: 5; -fx-max-width: 300;");
                    } else {
                        setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #2c3e50; " +
                                "-fx-background-radius: 15 15 15 0; -fx-padding: 10; " +
                                "-fx-alignment: center-left; -fx-margin: 5; -fx-max-width: 300;");
                    }

                    // Show context menu on right-click
                    setOnMouseClicked(event -> {
                        if (event.getButton() == MouseButton.SECONDARY && isCurrentUserMessage(chat)) {
                            contextMenu.show(chatListView, event.getScreenX(), event.getScreenY());
                        }
                    });
                }
            }

            private boolean isCurrentUserMessage(Chat chat) {
                User currentUser = Session.getUtilisateurConnecte();
                String currentUsername = currentUser != null ? currentUser.getNom() + currentUser.getPrenom() : "";
                return chat.getUsername().equals(currentUsername);
            }
        });

        // Set user count (static for now; update with actual logic if available)
        userCountLabel.setText("Users: 1");

        // Load initial messages and start polling
        loadMessages();
        startMessagePolling();
    }

    @FXML
    private void handleSend() {
        String msg = messageField.getText().trim();
        User currentUser = Session.getUtilisateurConnecte();

        if (!msg.isEmpty() && currentUser != null) {
            Chat chat = new Chat(
                    currentUser.getId(),
                    currentUser.getNom() + currentUser.getPrenom(),
                    msg,
                    LocalDateTime.now()
            );
            ChatDAO.addMessage(chat);
            messageField.clear();
            loadMessages();
        }
    }

    private void loadMessages() {
        List<Chat> chatList = ChatDAO.getAllMessages();
        messages.setAll(chatList);
        chatListView.scrollTo(messages.size() - 1);
    }

    private void startMessagePolling() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> loadMessages());
            }
        }, 0, 2000);
    }

    public void stopPolling() {
        if (timer != null) {
            timer.cancel();
        }
    }

    @FXML
    private void handleButtonHover(MouseEvent event) {
        sendButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");
    }

    @FXML
    private void handleButtonExit(MouseEvent event) {
        sendButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");
    }

    private void showEditDialog(Chat chat) {
        TextInputDialog dialog = new TextInputDialog(chat.getMessage());
        dialog.setTitle("Edit Message");
        dialog.setHeaderText(null);
        dialog.setContentText("Edit your message:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newMessage -> {
            if (!newMessage.trim().isEmpty() && !newMessage.equals(chat.getMessage())) {
                if (ChatDAO.updateMessage(chat.getId(), newMessage)) {
                    loadMessages();
                }
            }
        });
    }

    private void deleteMessage(Chat chat) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this message?");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (ChatDAO.deleteMessage(chat.getId())) {
                    loadMessages();
                }
            }
        });
    }
}