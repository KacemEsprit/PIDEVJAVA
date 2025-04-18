package com.pfe.nova.configuration;

import com.pfe.nova.models.Comment;
import com.pfe.nova.models.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentReportDAO {
    
    public static void reportComment(int commentId, int reporterId, String reason) throws SQLException {
        String sql = "INSERT INTO comment_report (comment_id, reporter_id, reason, created_at) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Add debugging output
            System.out.println("Executing SQL: " + sql);
            System.out.println("Parameters: commentId=" + commentId + ", reporterId=" + reporterId + ", reason=" + reason);
            
            pstmt.setInt(1, commentId);
            pstmt.setInt(2, reporterId);
            pstmt.setString(3, reason);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("SQL Error in reportComment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public static boolean hasUserReported(int commentId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comment_report WHERE comment_id = ? AND reporter_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, commentId);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    public static List<Map<String, Object>> findAllReports() throws SQLException {
        List<Map<String, Object>> reports = new ArrayList<>();
        String sql = "SELECT cr.*, c.contenu_com, c.user_id as comment_user_id, " +
                     "u1.nom as reporter_nom, u1.prenom as reporter_prenom, " +
                     "u2.nom as comment_user_nom, u2.prenom as comment_user_prenom " +
                     "FROM comment_report cr " +
                     "JOIN comment c ON cr.comment_id = c.id " +
                     "JOIN user u1 ON cr.reporter_id = u1.id " +
                     "JOIN user u2 ON c.user_id = u2.id " +
                     "ORDER BY cr.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("id", rs.getInt("id"));
                report.put("commentId", rs.getInt("comment_id"));
                report.put("reporterId", rs.getInt("reporter_id"));
                report.put("reason", rs.getString("reason"));
                report.put("createdAt", rs.getTimestamp("created_at").toLocalDateTime());
                report.put("commentContent", rs.getString("contenu_com"));
                
                User reporter = new User();
                reporter.setId(rs.getInt("reporter_id"));
                reporter.setNom(rs.getString("reporter_nom"));
                reporter.setPrenom(rs.getString("reporter_prenom"));
                report.put("reporter", reporter);
                
                User commentUser = new User();
                commentUser.setId(rs.getInt("comment_user_id"));
                commentUser.setNom(rs.getString("comment_user_nom"));
                commentUser.setPrenom(rs.getString("comment_user_prenom"));
                report.put("commentUser", commentUser);
                
                reports.add(report);
            }
        }
        
        return reports;
    }
    
    public static void deleteReport(int reportId) throws SQLException {
        String sql = "DELETE FROM comment_report WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reportId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Clears all reports for a specific comment
     * @param commentId The ID of the comment
     * @throws SQLException If a database error occurs
     */
    public static void clearReportsForComment(int commentId) throws SQLException {
        String sql = "DELETE FROM comment_report WHERE comment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            stmt.executeUpdate();
        }
    }
    
    public static void deleteAllForComment(int commentId) throws SQLException {
        String sql = "DELETE FROM comment_report WHERE comment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, commentId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Checks if a comment has any remaining reports
     * @param commentId The ID of the comment to check
     * @return true if the comment has any reports, false otherwise
     * @throws SQLException If a database error occurs
     */
    public static boolean hasReportsForComment(int commentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comment_report WHERE comment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
}