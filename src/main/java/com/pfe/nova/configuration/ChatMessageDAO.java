package com.pfe.nova.configuration;

import com.pfe.nova.models.ChatMessage;
import com.pfe.nova.models.User;
import com.pfe.nova.services.MercureService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageDAO {
    private static final MercureService mercureService = new MercureService();
    
    public static void save(ChatMessage message) throws SQLException {
        String sql = "INSERT INTO chat_message (sender_id, message, timestamp, channel) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, message.getSender() != null ? message.getSender().getId() : message.getUserId());
            stmt.setString(2, message.getMessage() != null ? message.getMessage() : message.getContent());
            stmt.setTimestamp(3, Timestamp.valueOf(message.getTimestamp()));
            stmt.setString(4, message.getChannel() != null ? message.getChannel() : message.getChannelName());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    message.setId(rs.getInt(1));
                    
                    // Publier le message sur Mercure après l'avoir sauvegardé en base de données
                    mercureService.publishMessage(message);
                }
            }
        }
    }
    
    // Alias for save method to maintain compatibility with ChatView
    public static void saveMessage(ChatMessage message) throws SQLException {
        save(message);
    }
    
    public static List<ChatMessage> getMessagesByChannel(String channel) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        // Modification de la requête SQL pour supprimer la référence à u.username
        String sql = "SELECT m.*, u.nom, u.prenom FROM chat_message m " +
                    "LEFT JOIN user u ON m.sender_id = u.id " +
                    "WHERE m.channel = ? " +
                    "ORDER BY m.timestamp ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, channel);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChatMessage message = new ChatMessage();
                    message.setId(rs.getInt("id"));
                    
                    // Handle message content
                    String messageContent = rs.getString("message");
                    message.setMessage(messageContent);
                    message.setContent(messageContent);
                    
                    // Handle timestamp
                    message.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    
                    // Handle channel
                    String channelName = rs.getString("channel");
                    message.setChannel(channelName);
                    message.setChannelName(channelName);
                    
                    // Handle user information
                    int senderId = rs.getInt("sender_id");
                    message.setUserId(senderId);
                    
                    // Création du nom d'utilisateur à partir de nom et prénom
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String username = (nom != null && prenom != null) ? nom + " " + prenom : "Utilisateur inconnu";
                    message.setUsername(username);
                    
                    // Create a User object if we have user data
                    if (senderId > 0) {
                        User sender = new User();
                        sender.setId(senderId);
                        sender.setNom(nom);
                        sender.setPrenom(prenom);
                        message.setSender(sender);
                    }
                    
                    messages.add(message);
                }
            }
        }
        
        return messages;
    }
    
    public static void insertMessage(ChatMessage message) throws SQLException {
        String sql = "INSERT INTO chat_message (channel_name, user_id, username, message, timestamp) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, message.getChannelName());
            stmt.setInt(2, message.getUserId());
            stmt.setString(3, message.getUsername());
            stmt.setString(4, message.getContent());
            stmt.setTimestamp(5, Timestamp.valueOf(message.getTimestamp()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        message.setId(generatedKeys.getInt(1));
                        
                        // Publier le message via Mercure
                        MercureService.publishMessage(message);
                    }
                }
            }
        }
    }
    
    public static MercureService getMercureService() {
        return mercureService;
    }

    public static void updateMessage(ChatMessage message) {
    }

    public static void deleteMessage(int id) {
    }
}