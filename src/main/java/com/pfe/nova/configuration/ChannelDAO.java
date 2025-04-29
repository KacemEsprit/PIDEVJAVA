package com.pfe.nova.configuration;

import com.pfe.nova.models.Channel;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChannelDAO {
    
    public static List<Channel> getAllChannels() throws SQLException {
        List<Channel> channels = new ArrayList<>();
        String sql = "SELECT * FROM channel ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Channel channel = new Channel();
                channel.setId(rs.getInt("id"));
                channel.setName(rs.getString("name"));
                channel.setDescription(rs.getString("description"));
                channel.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                
                channels.add(channel);
            }
        }
        
        return channels;
    }
    
    public static void saveChannel(Channel channel) throws SQLException {
        String sql = "INSERT INTO channel (name, description, created_at) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, channel.getName());
            stmt.setString(2, channel.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                channel.setId(rs.getInt(1));
            }
        }
    }
    
    public static boolean channelExists(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM channel WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        
        return false;
    }
}