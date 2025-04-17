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

    // Add this method to your CommentDAO class
    public static void updateReportStatus(int commentId, boolean reported, String reason) throws SQLException {
        String sql = "UPDATE comment SET reported = ?, report_reason = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Add debugging output
            System.out.println("Executing SQL: " + sql);
            System.out.println("Parameters: reported=" + reported + ", reason=" + reason + ", commentId=" + commentId);
            
            pstmt.setBoolean(1, reported);
            pstmt.setString(2, reason);
            pstmt.setInt(3, commentId);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("SQL Error in updateReportStatus: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Update the findByPostId method to include reported status
    public static List<Comment> findByPostId(int postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.nom, u.prenom FROM comment c JOIN user u ON c.user_id = u.id WHERE c.publication_id = ? ORDER BY c.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, postId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setId(rs.getInt("id"));
                    comment.setUserId(rs.getInt("user_id"));
                    comment.setPublicationId(rs.getInt("publication_id"));
                    comment.setContenuCom(rs.getString("contenu_com"));
                    comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    comment.setReported(rs.getBoolean("reported"));
                    comment.setReportReason(rs.getString("report_reason"));
                    
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    comment.setUser(user);
                    
                    comments.add(comment);
                }
            }
        }
        
        return comments;
    }

    /**
     * Deletes a comment and all its related records
     * @param commentId The ID of the comment to delete
     * @throws SQLException If a database error occurs
     */
    public static void delete(int commentId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // First delete any reports for this comment - fixed table name
            String deleteReportsSQL = "DELETE FROM comment_report WHERE comment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteReportsSQL)) {
                stmt.setInt(1, commentId);
                stmt.executeUpdate();
            }
            
            // Finally delete the comment itself - fixed table name
            String deleteCommentSQL = "DELETE FROM comment WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteCommentSQL)) {
                stmt.setInt(1, commentId);
                stmt.executeUpdate();
            }
            
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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