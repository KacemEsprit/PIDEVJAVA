package com.pfe.nova.configuration;
import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.User;
import java.sql.*;

public class UserDAO {

    /**
     * Find a user by email
     * @param email The email to search for
     * @return The user if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public static User findByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setTel(rs.getString("tel"));
                    user.setAdresse(rs.getString("adresse"));
                    user.setRole(rs.getString("role"));
                    user.setPassword(rs.getString("password"));
                    
                    // Optional fields
                    if (rs.getObject("picture") != null) {
                        user.setPicture(rs.getString("picture"));
                    }
                    
                    return user;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find a user by ID
     * @param id The user ID to search for
     * @return The user if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public static User findById(int id) throws SQLException {
        String query = "SELECT * FROM user WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setTel(rs.getString("tel"));
                    user.setAdresse(rs.getString("adresse"));
                    user.setRole(rs.getString("role"));
                    user.setPassword(rs.getString("password"));
                    
                    // Optional fields
                    if (rs.getObject("picture") != null) {
                        user.setPicture(rs.getString("picture"));
                    }
                    
                    return user;
                }
            }
        }
        
        return null;
    }
}

