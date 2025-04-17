package com.pfe.nova.configuration;
import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Find a user by email
     *
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
     *
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

    /**
     * Delete a user from the database by ID
     *
     * @param userId The ID of the user to delete
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    public static boolean deleteUser(int userId) throws SQLException {
        String query = "DELETE FROM user WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;
        }
    }

    /**
     * Get all users from the database
     *
     * @return List of all users
     * @throws SQLException if a database error occurs
     */
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
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

                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving users: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Update an existing user in the database
     *
     * @param user The user object with updated information
     * @return true if update was successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    public static boolean updateUser(User user) throws SQLException {
        String query = "UPDATE user SET nom = ?, prenom = ?, email = ?, tel = ?, adresse = ?, role = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getTel());
            stmt.setString(5, user.getAdresse());
            stmt.setString(6, user.getRole());
            stmt.setInt(7, user.getId());

            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;
        }
    }

    /**
     * Check if a user with the given email already exists
     *
     * @param email The email to check
     * @return true if the email exists, false otherwise
     * @throws SQLException if a database error occurs
     */
    public static boolean isEmailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Register a new user in the database
     *
     * @param user The user object to register
     * @return true if registration was successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    public static boolean registerUser(User user) throws SQLException {
        String query = "INSERT INTO user (nom, prenom, email, tel, adresse, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getTel());
            stmt.setString(5, user.getAdresse());
            stmt.setString(6, user.getPassword());
            stmt.setString(7, user.getRole());

            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;
        }
    }
}

