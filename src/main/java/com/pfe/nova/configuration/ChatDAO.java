package com.pfe.nova.configuration;

import com.pfe.nova.models.Chat;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {
    public static void addMessage(Chat chat) {
        String sql = "INSERT INTO chat (user_id, username, message, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, chat.getUserId());
            stmt.setString(2, chat.getUsername());
            stmt.setString(3, chat.getMessage());
            stmt.setTimestamp(4, Timestamp.valueOf(chat.getTimestamp()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Chat> getAllMessages() {
        List<Chat> messages = new ArrayList<>();
        String sql = "SELECT * FROM chat ORDER BY timestamp ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Chat chat = new Chat(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("message"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                );
                messages.add(chat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public static boolean deleteMessage(int chatId) {
        String sql = "DELETE FROM chat WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, chatId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateMessage(int chatId, String newMessage) {
        String sql = "UPDATE chat SET message = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newMessage);
            stmt.setInt(2, chatId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}