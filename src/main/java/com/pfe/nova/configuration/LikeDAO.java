package com.pfe.nova.configuration;

import com.pfe.nova.models.Like;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LikeDAO {
    
    // Add a like to a post
    public static void save(Like like) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // Check if the like already exists
            if (likeExists(like.getPublicationId(), like.getUserId(), like.getCommentId())) {
                // If it exists, delete it (unlike)
                delete(like.getPublicationId(), like.getUserId(), like.getCommentId());
                return;
            }
            
            // If it doesn't exist, insert it
            String sql = "INSERT INTO publication_like (publication_id, user_id, comment_id, created_at) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, like.getPublicationId());
            stmt.setInt(2, like.getUserId());
            
            if (like.getCommentId() != null) {
                stmt.setInt(3, like.getCommentId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            // Format the datetime for MySQL
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            stmt.setString(4, like.getCreatedAt().format(formatter));
            
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    // Delete a like
    public static void delete(int publicationId, int userId, Integer commentId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql;
            if (commentId != null) {
                sql = "DELETE FROM publication_like WHERE publication_id = ? AND user_id = ? AND comment_id = ?";
            } else {
                sql = "DELETE FROM publication_like WHERE publication_id = ? AND user_id = ? AND comment_id IS NULL";
            }
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, publicationId);
            stmt.setInt(2, userId);
            
            if (commentId != null) {
                stmt.setInt(3, commentId);
            }
            
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    // Check if a like exists
    public static boolean likeExists(int publicationId, int userId, Integer commentId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql;
            if (commentId != null) {
                sql = "SELECT COUNT(*) FROM publication_like WHERE publication_id = ? AND user_id = ? AND comment_id = ?";
            } else {
                sql = "SELECT COUNT(*) FROM publication_like WHERE publication_id = ? AND user_id = ? AND comment_id IS NULL";
            }
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, publicationId);
            stmt.setInt(2, userId);
            
            if (commentId != null) {
                stmt.setInt(3, commentId);
            }
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    // Count likes for a post
    public static int countLikes(int publicationId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT COUNT(*) FROM publication_like WHERE publication_id = ? AND comment_id IS NULL";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, publicationId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    // Check if a user has liked a post
    public static boolean hasUserLiked(int publicationId, int userId) throws SQLException {
        return likeExists(publicationId, userId, null);
    }
}