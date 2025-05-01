package com.pfe.nova.Controller;

import com.pfe.nova.components.ChatView;
import com.pfe.nova.configuration.ChannelDAO;
import com.pfe.nova.configuration.ChatMessageDAO;
import com.pfe.nova.models.Channel;
import com.pfe.nova.models.User;
import com.pfe.nova.services.MercureService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class MessagesViewController {
    @FXML private ComboBox<String> channelSelector;
    @FXML private VBox messagesContainer;
    @FXML private VBox noChannelSelectedContainer;
    
    @FXML
    private Button refreshButton;
    
    private User currentUser;
    @FXML
    private Text selectedChannelName;
    
    @FXML
    private Label onlineUsersCount;
    
    @FXML
    private VBox channelsListContainer;
    
    private MercureService mercureService;

    @FXML
    public void initialize() {
        // Initialiser le service Mercure
        loadChannels();
        
        // Afficher le message "Aucun canal sélectionné" par défaut
        messagesContainer.setVisible(false);
        messagesContainer.setManaged(false);
        noChannelSelectedContainer.setVisible(true);
        noChannelSelectedContainer.setManaged(true);
        
        // Ajouter un écouteur pour le changement de canal
        channelSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadChannel(newVal);
            }
        });
    }
    
    private void loadChannels() {
        try {
            // Vider la liste actuelle
            channelSelector.getItems().clear();
            
            // Vider également la liste des canaux dans le panneau latéral
            channelsListContainer.getChildren().clear();
            
            // Charger les canaux depuis la base de données
            List<Channel> channels = ChannelDAO.getAllChannels();
            
            // Ajouter les noms des canaux au sélecteur et à la liste latérale
            for (Channel channel : channels) {
                // Ajouter au ComboBox
                channelSelector.getItems().add(channel.getName());
                
                // Créer un élément pour la liste latérale
                HBox channelItem = createChannelItem(channel.getName());
                channelsListContainer.getChildren().add(channelItem);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des canaux: " + e.getMessage());
        }
    }

    private HBox createChannelItem(String channelName) {
        HBox channelItem = new HBox();
        channelItem.getStyleClass().add("channel-item");
        channelItem.setPadding(new Insets(10));
        
        // Ajouter une icône de canal
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.HASHTAG);
        icon.getStyleClass().add("channel-icon");
        
        // Ajouter le nom du canal
        Text nameText = new Text(channelName);
        nameText.getStyleClass().add("channel-item-text");
        
        // Ajouter les éléments à l'item
        channelItem.getChildren().addAll(icon, nameText);
        channelItem.setSpacing(10);
        
        // Ajouter un gestionnaire d'événements pour sélectionner le canal
        channelItem.setOnMouseClicked(event -> {
            channelSelector.setValue(channelName);
        });
        
        return channelItem;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Recharger les canaux après avoir défini l'utilisateur
        loadChannels();
        
        // Si un canal était précédemment sélectionné, le recharger
        String currentChannel = channelSelector.getValue();
        if (currentChannel != null) {
            loadChannel(currentChannel);
        }
    }
    
    private void loadChannel(String channelName) {
        // Mettre à jour le nom du canal sélectionné
        selectedChannelName.setText(channelName);
        
        // Créer une nouvelle vue de chat pour ce canal
        ChatView chatView = new ChatView(channelName, currentUser);
        
        // Vider le conteneur de messages et ajouter la nouvelle vue
        messagesContainer.getChildren().clear();
        messagesContainer.getChildren().add(chatView);
        
        // Afficher le conteneur de messages et masquer le message "Aucun canal sélectionné"
        messagesContainer.setVisible(true);
        messagesContainer.setManaged(true);
        noChannelSelectedContainer.setVisible(false);
        noChannelSelectedContainer.setManaged(false);
        
        // Mettre à jour le nombre d'utilisateurs en ligne (à implémenter)
        updateOnlineUsersCount(channelName);
    }

    private void updateOnlineUsersCount(String channelName) {
        // Cette méthode pourrait être implémentée pour afficher le nombre d'utilisateurs en ligne
        // Pour l'instant, nous affichons simplement un message statique
        onlineUsersCount.setText("0 utilisateurs en ligne");
    }
    
    @FXML
    private void refreshChannels() {
        loadChannels();

        // Rafraîchir également les messages du canal actuel si un canal est sélectionné
        String currentChannel = channelSelector.getValue();
        if (currentChannel != null && !messagesContainer.getChildren().isEmpty()) {
            if (messagesContainer.getChildren().get(0) instanceof ChatView) {
                ChatView chatView = (ChatView) messagesContainer.getChildren().get(0);
                chatView.refreshMessages();
            }
        }

        showInfo("Canaux rechargés avec succès");
    }
    
    @FXML
    public void createNewChannel() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouveau Canal");
        dialog.setHeaderText("Créer un nouveau canal de discussion");
        dialog.setContentText("Nom du canal:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(channelName -> {
            if (!channelName.trim().isEmpty()) {
                try {
                    // Vérifier si le canal existe déjà
                    if (ChannelDAO.channelExists(channelName)) {
                        showError("Un canal avec ce nom existe déjà");
                        return;
                    }
                    
                    // Créer et sauvegarder le nouveau canal
                    Channel newChannel = new Channel();
                    newChannel.setName(channelName);
                    newChannel.setDescription(""); // Optionnel: ajouter une description
                    newChannel.setCreatedAt(LocalDateTime.now());
                    
                    ChannelDAO.saveChannel(newChannel);
                    
                    // Recharger la liste des canaux
                    loadChannels();
                    
                    // Sélectionner le nouveau canal
                    channelSelector.getSelectionModel().select(channelName);
                    
                    showInfo("Canal créé avec succès");
                } catch (SQLException e) {
                    showError("Erreur lors de la création du canal: " + e.getMessage());
                }
            }
        });
    }
    

    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}