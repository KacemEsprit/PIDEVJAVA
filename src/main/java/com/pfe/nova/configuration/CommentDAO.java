package com.pfe.nova.configuration;

import com.pfe.nova.models.Comment;
import com.pfe.nova.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    public static void save(Comment comment) throws SQLException {
        String sql = comment.getId() == 0
            ? "INSERT INTO comment (user_id, publication_id, created_at, contenu_com, type) VALUES (?, ?, ?, ?, ?)"
            : "UPDATE comment SET contenu_com = ? WHERE id = ?";
            
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (comment.getId() == 0) {
                stmt.setInt(1, comment.getUserId());
                stmt.setInt(2, comment.getPublicationId());
                stmt.setTimestamp(3, Timestamp.valueOf(comment.getCreatedAt()));
                stmt.setString(4, comment.getContenuCom());
                stmt.setString(5, comment.getType());
            } else {
                stmt.setString(1, comment.getContenuCom());
                stmt.setInt(2, comment.getId());
            }
            
            stmt.executeUpdate();
            
            if (comment.getId() == 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
            }
        }
    }

    public static List<Comment> findByPostId(int postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.nom FROM comment c " +
                    "LEFT JOIN user u ON c.user_id = u.id " +
                    "WHERE c.publication_id = ? " +
                    "ORDER BY c.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setUserId(rs.getInt("user_id"));
                comment.setPublicationId(rs.getInt("publication_id"));
                comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                comment.setContenuCom(rs.getString("contenu_com"));
                comment.setType(rs.getString("type"));
                
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("nom"));
                comment.setUser(user);
                
                comments.add(comment);
            }
        }
        return comments;
    }

    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM comment WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public static int countAll() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM comment";
            stmt = conn.prepareStatement(sql);
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
}