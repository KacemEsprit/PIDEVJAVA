package com.pfe.nova.components;

import com.pfe.nova.models.ChatMessage;
import com.pfe.nova.models.User;
import com.pfe.nova.configuration.ChatMessageDAO;
import com.pfe.nova.services.MercureService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ChatView extends VBox {
    private final String channelName;
    private final User currentUser;
    private VBox messagesBox;
    private TextField messageInput;
    private MercureService mercureService;
    
    public ChatView(String channelName, User currentUser) {
        this.channelName = channelName;
        this.currentUser = currentUser;
        this.mercureService = ChatMessageDAO.getMercureService();
        
        // Configuration du conteneur principal
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        VBox.setVgrow(this, Priority.ALWAYS);
        
        // Créer la zone de messages
        createMessagesArea();
        
        // Créer la zone de saisie
        createInputArea();
        
        // Charger les messages
        loadMessages();
        
        // S'abonner aux mises à jour en temps réel
        subscribeToRealTimeUpdates();
    }
    
    private void createMessagesArea() {
        messagesBox = new VBox();
        messagesBox.setSpacing(10);
        messagesBox.setPadding(new Insets(10));
        messagesBox.getStyleClass().add("messages-box");
        
        // Permettre au conteneur de s'étendre pour remplir l'espace disponible
        VBox.setVgrow(messagesBox, Priority.ALWAYS);
        
        // Créer un ScrollPane pour permettre le défilement
        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("messages-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        this.getChildren().add(scrollPane);
    }
    
    private void createInputArea() {
        HBox inputArea = new HBox();
        inputArea.setSpacing(10);
        inputArea.setAlignment(Pos.CENTER_LEFT);
        inputArea.setPadding(new Insets(10, 0, 0, 0));
        
        // Champ de saisie
        messageInput = new TextField();
        messageInput.setPromptText("Type your message here...");
        messageInput.getStyleClass().add("message-input");
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        
        // Bouton d'envoi
        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("send-button");
        sendButton.setOnAction(e -> sendMessage());
        
        // Ajouter une icône au bouton
        FontAwesomeIconView sendIcon = new FontAwesomeIconView(FontAwesomeIcon.PAPER_PLANE);
        sendIcon.getStyleClass().add("send-icon");
        sendButton.setGraphic(sendIcon);
        
        // Ajouter les éléments à la zone de saisie
        inputArea.getChildren().addAll(messageInput, sendButton);
        
        // Ajouter un gestionnaire d'événements pour la touche Entrée
        messageInput.setOnAction(e -> sendMessage());
        
        this.getChildren().add(inputArea);
    }
    
    private void loadMessages() {
        try {
            // Vider la liste actuelle
            messagesBox.getChildren().clear();
            
            // Charger les messages depuis la base de données
            List<ChatMessage> messages = ChatMessageDAO.getMessagesByChannel(channelName);
            
            // Ajouter les messages à la liste
            for (ChatMessage message : messages) {
                addMessageToView(message);
            }
            
            // Faire défiler vers le bas pour voir les messages les plus récents
            if (messagesBox.getChildren().size() > 0) {
                messagesBox.getChildren().get(messagesBox.getChildren().size() - 1).requestFocus();
            }
        } catch (SQLException e) {
            // Afficher une erreur
            System.err.println("Erreur lors du chargement des messages: " + e.getMessage());
        }
    }
    
    private void subscribeToRealTimeUpdates() {
        MercureService.subscribeToChannel(channelName, this::handleRealTimeMessage);
    }
    
    private void handleRealTimeMessage(ChatMessage message) {
        // Vérifier si le message existe déjà dans la liste
        boolean messageExists = false;
        for (javafx.scene.Node node : messagesBox.getChildren()) {
            if (node instanceof HBox) {
                HBox alignmentContainer = (HBox) node;
                for (javafx.scene.Node childNode : alignmentContainer.getChildren()) {
                    if (childNode instanceof VBox) {
                        VBox messageContainer = (VBox) childNode;
                        if (messageContainer.getUserData() != null && 
                            messageContainer.getUserData().equals(message.getId())) {
                            messageExists = true;
                            break;
                        }
                    }
                }
            }
            if (messageExists) break;
        }
        
        // Si le message n'existe pas, l'ajouter à la liste
        if (!messageExists) {
            addMessageToView(message);
            
            // Faire défiler vers le bas pour voir le nouveau message
            ScrollPane scrollPane = (ScrollPane) this.getChildren().get(0);
            scrollPane.setVvalue(1.0);
        }
    }
    
    private void addMessageToView(ChatMessage message) {
        // Créer un conteneur pour le message
        VBox messageContainer = new VBox();
        messageContainer.setSpacing(5);
        messageContainer.setPadding(new Insets(10));
        messageContainer.getStyleClass().add("message-container");
        messageContainer.setUserData(message.getId()); // Stocker l'ID du message
        
        // Créer un conteneur HBox pour aligner le message
        HBox alignmentContainer = new HBox();
        alignmentContainer.setPrefWidth(messagesBox.getWidth());
        
        // Ajouter la classe "own-message" si le message est de l'utilisateur actuel
        boolean isOwnMessage = message.getUserId() == currentUser.getId();
        if (isOwnMessage) {
            messageContainer.getStyleClass().add("own-message");
            alignmentContainer.getStyleClass().add("own-message-container");
            alignmentContainer.setAlignment(Pos.CENTER_RIGHT);
            
            // Ajouter des boutons d'action pour les messages de l'utilisateur actuel
            HBox actionButtons = new HBox();
            actionButtons.setSpacing(5);
            actionButtons.setAlignment(Pos.CENTER_RIGHT);
            
            // Bouton de modification
            Button editButton = new Button();
            editButton.getStyleClass().add("message-action-button");
            FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
            editIcon.getStyleClass().add("message-action-icon");
            editIcon.getStyleClass().add("edit-icon");
            editButton.setGraphic(editIcon);
            editButton.setOnAction(e -> editMessage(message, messageContainer));
            
            // Bouton de suppression
            Button deleteButton = new Button();
            deleteButton.getStyleClass().add("message-action-button");
            FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
            deleteIcon.getStyleClass().add("message-action-icon");
            deleteIcon.getStyleClass().add("delete-icon");
            deleteButton.setGraphic(deleteIcon);
            deleteButton.setOnAction(e -> deleteMessage(message, messageContainer));
            
            actionButtons.getChildren().addAll(editButton, deleteButton);
            
            // Ajouter les boutons d'action au conteneur du message
            messageContainer.getChildren().add(actionButtons);
        } else {
            alignmentContainer.getStyleClass().add("other-message-container");
            alignmentContainer.setAlignment(Pos.CENTER_LEFT);
        }
        
        // En-tête du message (nom d'utilisateur + date)
        HBox header = new HBox();
        header.setSpacing(10);
        
        Text username = new Text(message.getUsername());
        username.getStyleClass().add("message-username");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Text timestamp = new Text(message.getTimestamp().format(formatter));
        timestamp.getStyleClass().add("message-timestamp");
        
        // Si c'est notre propre message, inverser l'ordre (timestamp puis username)
        if (isOwnMessage) {
            header.getChildren().addAll(timestamp, username);
        } else {
            header.getChildren().addAll(username, timestamp);
        }
        
        // Contenu du message
        TextFlow content = new TextFlow();
        Text messageText = new Text(message.getContent());
        messageText.getStyleClass().add("message-text");
        content.getChildren().add(messageText);
        
        // Ajouter les éléments au conteneur
        messageContainer.getChildren().add(0, header);
        messageContainer.getChildren().add(1, content);
        
        // Ajouter le message au conteneur d'alignement
        alignmentContainer.getChildren().add(messageContainer);
        HBox.setHgrow(alignmentContainer, Priority.ALWAYS);
        
        // Ajouter le conteneur d'alignement à la liste des messages
        messagesBox.getChildren().add(alignmentContainer);
    }
    
    private void editMessage(ChatMessage message, VBox messageContainer) {
        // Créer une boîte de dialogue pour modifier le message
        TextInputDialog dialog = new TextInputDialog(message.getContent());
        dialog.setTitle("Modifier le message");
        dialog.setHeaderText("Modifier votre message");
        dialog.setContentText("Nouveau contenu :");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            if (!newContent.trim().isEmpty()) {
                // Mettre à jour le message dans la base de données
                message.setContent(newContent);
                ChatMessageDAO.updateMessage(message);

                // Mettre à jour l'affichage du message
                TextFlow content = (TextFlow) messageContainer.getChildren().get(1);
                Text messageText = (Text) content.getChildren().get(0);
                messageText.setText(newContent);

            }
        });
    }
    
    private void deleteMessage(ChatMessage message, VBox messageContainer) {
        // Demander confirmation avant de supprimer
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer le message");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce message ?");
        alert.setContentText("Cette action ne peut pas être annulée.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Supprimer le message de la base de données
            ChatMessageDAO.deleteMessage(message.getId());

            // Supprimer le message de l'affichage
            // Trouver le conteneur parent (HBox alignmentContainer) et le supprimer
            HBox alignmentContainer = (HBox) messageContainer.getParent();
            if (alignmentContainer != null) {
                messagesBox.getChildren().remove(alignmentContainer);
            } else {
                // Fallback si la structure a changé
                messagesBox.getChildren().remove(messageContainer);
            }

            // Rafraîchir la liste des messages pour s'assurer que tout est à jour
            // loadMessages(); // Cette ligne rechargerait tous les messages, ce qui peut être lourd

            // Afficher une notification de succès
            showNotification("Message supprimé avec succès");

        }
    }

    private void showNotification(String message) {
        // Créer une notification temporaire
        Label notification = new Label(message);
        notification.getStyleClass().add("success-notification");
        notification.setMaxWidth(Double.MAX_VALUE);
        notification.setAlignment(Pos.CENTER);
        notification.setPadding(new Insets(10));
        
        // Ajouter la notification en haut de la vue
        this.getChildren().add(0, notification);
        
        // Créer une transition pour faire disparaître la notification
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), notification);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> this.getChildren().remove(notification));
        fadeOut.play();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void sendMessage() {
        String content = messageInput.getText().trim();
        
        if (!content.isEmpty()) {
            try {
                // Créer un nouveau message
                ChatMessage message = new ChatMessage();
                message.setChannelName(channelName);
                message.setUserId(currentUser.getId());
                message.setUsername(currentUser.getNom() + " " + currentUser.getPrenom()); // Utiliser nom et prénom au lieu de username
                message.setContent(content);
                message.setTimestamp(LocalDateTime.now());
                
                // Create a sender object for compatibility
                User sender = new User();
                sender.setId(currentUser.getId());
                sender.setNom(currentUser.getNom());
                sender.setPrenom(currentUser.getPrenom());
                message.setSender(sender);
                
                // Sauvegarder le message dans la base de données
                ChatMessageDAO.saveMessage(message);
                
                // Ajouter le message à la vue
                addMessageToView(message);
                
                // Vider le champ de saisie
                messageInput.clear();
                
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'envoi du message: " + e.getMessage());
            }
        }
    }
    
    // Ajouter cette méthode publique pour permettre le rafraîchissement externe
    public void refreshMessages() {
        loadMessages();
    }
    
    @Override
    protected void finalize() throws Throwable {
        // Se désabonner du canal lorsque la vue est détruite
        if (mercureService != null) {
            mercureService.unsubscribe();
        }
        super.finalize();
    }
}