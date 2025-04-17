package com.pfe.nova.configuration;
import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import org.mindrot.jbcrypt.BCrypt;  // Add this import

public class UserDAO {
    
    public static User authenticateUser(String email, String password) {
        String query = "SELECT * FROM user WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPasswordFromDB = rs.getString("password");
                    System.out.println("Attempting login for email: " + email);
                    
                    try {
                        if (BCrypt.checkpw(password, hashedPasswordFromDB)) {
                            String role = rs.getString("role");
                            System.out.println("Password verified successfully for role: " + role);
                            
                            switch (role) {
                                case "ADMIN":
                                    return new User(
                                        rs.getInt("id"),
                                        rs.getString("nom"),
                                        rs.getString("prenom"),
                                        email,
                                        rs.getString("tel"),
                                        rs.getString("adresse"),
                                        hashedPasswordFromDB,
                                        rs.getString("picture"),
                                        "ADMIN"
                                    );
                                    
                                case "PATIENT":
                                    return new Patient(
                                        rs.getInt("id"),
                                        rs.getString("nom"),
                                        rs.getString("prenom"),
                                        email,
                                        rs.getString("tel"),
                                        rs.getString("adresse"),
                                        hashedPasswordFromDB,
                                        rs.getString("picture"),
                                        rs.getInt("age"),
                                        rs.getString("gender"),
                                        rs.getString("blood_type")
                                    );
                                    
                                case "MEDECIN":
                                    return new Medecin(
                                        rs.getInt("id"),
                                        rs.getString("nom"),
                                        rs.getString("prenom"),
                                        email,
                                        rs.getString("tel"),
                                        rs.getString("adresse"),
                                        hashedPasswordFromDB,
                                        rs.getString("picture"),
                                        rs.getString("specialite"),
                                        rs.getString("experience"),
                                        rs.getString("diplome")
                                    );
                                    
                                case "DONATEUR":
                                    return new Donateur(
                                        rs.getInt("id"),
                                        rs.getString("nom"),
                                        rs.getString("prenom"),
                                        email,
                                        rs.getString("tel"),
                                        rs.getString("adresse"),
                                        hashedPasswordFromDB,
                                        rs.getString("picture"),
                                        rs.getString("donateur_type")
                                    );
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("BCrypt verification error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean registerUser(User user) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String userQuery = "INSERT INTO user (nom, prenom, email, tel, adresse, password, picture, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement userStmt = connection.prepareStatement(userQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, user.getNom());
                userStmt.setString(2, user.getPrenom());
                userStmt.setString(3, user.getEmail());
                userStmt.setString(4, user.getTel());
                userStmt.setString(5, user.getAdresse());
                userStmt.setString(6, user.getPassword());
                userStmt.setString(7, user.getPicture());
                userStmt.setString(8, user.getRole());
                
                int affectedRows = userStmt.executeUpdate();
                
                if (affectedRows == 0) {
                    return false;
                }

                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        
                        switch (user.getRole()) {
                            case "MEDECIN":
                                Medecin medecin = (Medecin) user;
                                String medecinQuery = "UPDATE user SET specialite = ?, experience = ?, diplome = ? WHERE id = ?";
                                try (PreparedStatement medecinStmt = connection.prepareStatement(medecinQuery)) {
                                    medecinStmt.setString(1, medecin.getSpecialite());
                                    medecinStmt.setString(2, medecin.getExperience());
                                    medecinStmt.setString(3, medecin.getDiplome());
                                    medecinStmt.setInt(4, userId);
                                    medecinStmt.executeUpdate();
                                }
                                break;

                            case "PATIENT":
                                Patient patient = (Patient) user;
                                String patientQuery = "UPDATE user SET age = ?, gender = ?, blood_type = ? WHERE id = ?";
                                try (PreparedStatement patientStmt = connection.prepareStatement(patientQuery)) {
                                    patientStmt.setInt(1, patient.getAge());
                                    patientStmt.setString(2, patient.getGender());
                                    patientStmt.setString(3, patient.getBloodType());
                                    patientStmt.setInt(4, userId);
                                    patientStmt.executeUpdate();
                                }
                                break;

                            case "DONATEUR":
                                Donateur donateur = (Donateur) user;
                                String donateurQuery = "UPDATE user SET donateur_type = ? WHERE id = ?";
                                try (PreparedStatement donateurStmt = connection.prepareStatement(donateurQuery)) {
                                    donateurStmt.setString(1, donateur.getDonateurType());
                                    donateurStmt.setInt(2, userId);  // Fixed parameter index from 4 to 2
                                    donateurStmt.executeUpdate();
                                }
                                break;
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String role = rs.getString("role");
                User user = null;
                
                switch (role) {
                    case "ADMIN":
                        user = new User(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("tel"),
                            rs.getString("adresse"),
                            rs.getString("password"),
                            rs.getString("picture"),
                            "ADMIN"
                        );
                        break;
                    case "MEDECIN":
                        user = new Medecin(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("tel"),
                            rs.getString("adresse"),
                            rs.getString("password"),
                            rs.getString("picture"),
                            rs.getString("specialite"),
                            rs.getString("experience"),
                            rs.getString("diplome")
                        );
                        break;
                    case "PATIENT":
                        user = new Patient(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("tel"),
                            rs.getString("adresse"),
                            rs.getString("password"),
                            rs.getString("picture"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("blood_type")
                        );
                        break;
                    case "DONATEUR":
                        user = new Donateur(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("tel"),
                            rs.getString("adresse"),
                            rs.getString("password"),
                            rs.getString("picture"),
                            rs.getString("donateur_type")
                        );
                        break;
                }
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void deleteUser(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM user WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean updateUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Update base user information
            String baseQuery = "UPDATE user SET nom = ?, prenom = ?, email = ?, tel = ?, adresse = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(baseQuery)) {
                stmt.setString(1, user.getNom());
                stmt.setString(2, user.getPrenom());
                stmt.setString(3, user.getEmail());
                stmt.setString(4, user.getTel());
                stmt.setString(5, user.getAdresse());
                stmt.setInt(6, user.getId());
                stmt.executeUpdate();
            }

            // Update role-specific information
            switch (user.getRole()) {
                case "MEDECIN":
                    Medecin medecin = (Medecin) user;
                    String medecinQuery = "UPDATE user SET specialite = ?, experience = ?, diplome = ? WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(medecinQuery)) {
                        stmt.setString(1, medecin.getSpecialite());
                        stmt.setString(2, medecin.getExperience());
                        stmt.setString(3, medecin.getDiplome());
                        stmt.setInt(4, user.getId());
                        stmt.executeUpdate();
                    }
                    break;

                case "PATIENT":
                    Patient patient = (Patient) user;
                    String patientQuery = "UPDATE user SET age = ?, gender = ?, blood_type = ? WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(patientQuery)) {
                        stmt.setInt(1, patient.getAge());
                        stmt.setString(2, patient.getGender());
                        stmt.setString(3, patient.getBloodType());
                        stmt.setInt(4, user.getId());
                        stmt.executeUpdate();
                    }
                    break;

                case "DONATEUR":
                    Donateur donateur = (Donateur) user;
                    String donateurQuery = "UPDATE user SET donateur_type = ? WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(donateurQuery)) {
                        stmt.setString(1, donateur.getDonateurType());
                        stmt.setInt(2, user.getId());
                        stmt.executeUpdate();
                    }
                    break;
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static User getUserById(int userId) {
        String query = "SELECT * FROM user WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    switch (role) {
                        case "ADMIN":
                            return new User(
                                rs.getInt("id"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("email"),
                                rs.getString("tel"),
                                rs.getString("adresse"),
                                rs.getString("password"),
                                rs.getString("picture"),
                                "ADMIN"
                            );
                        case "MEDECIN":
                            return new Medecin(
                                rs.getInt("id"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("email"),
                                rs.getString("tel"),
                                rs.getString("adresse"),
                                rs.getString("password"),
                                rs.getString("picture"),
                                rs.getString("specialite"),
                                rs.getString("experience"),
                                rs.getString("diplome")
                            );
                        case "PATIENT":
                            return new Patient(
                                rs.getInt("id"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("email"),
                                rs.getString("tel"),
                                rs.getString("adresse"),
                                rs.getString("password"),
                                rs.getString("picture"),
                                rs.getInt("age"),
                                rs.getString("gender"),
                                rs.getString("blood_type")
                            );
                        case "DONATEUR":
                            return new Donateur(
                                rs.getInt("id"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("email"),
                                rs.getString("tel"),
                                rs.getString("adresse"),
                                rs.getString("password"),
                                rs.getString("picture"),
                                rs.getString("donateur_type")
                            );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

