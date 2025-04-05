package com.pfe.nova.configuration;
import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class UserDAO {

    public static void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // In production, hash this password
            stmt.setString(3, user.getEmail());
            stmt.executeUpdate();
        }
    }

    public static User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        User user = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
            }
        }
        return user;
    }

    // Add other methods as needed (update, delete, etc.)
}

