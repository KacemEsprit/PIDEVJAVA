package com.pfe.nova.configuration;

import com.pfe.nova.models.Medication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicationDAO {
    
    public static List<Medication> getAllMedications() throws SQLException {
        List<Medication> medications = new ArrayList<>();
        String sql = "SELECT * FROM medicament";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Medication medication = new Medication(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getInt("quantite_stock"),
                    rs.getDouble("prix")
                );
                
                if (rs.getString("image_path") != null) {
                    medication.setImagePath(rs.getString("image_path"));
                }
                
                medications.add(medication);
            }
        }
        
        return medications;
    }
    
    public static boolean createMedication(Medication medication) throws SQLException {
        String sql = "INSERT INTO medicament (nom, description, quantite_stock, prix, image_path) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, medication.getNom());
            stmt.setString(2, medication.getDescription());
            stmt.setInt(3, medication.getQuantiteStock());
            stmt.setDouble(4, medication.getPrix());
            stmt.setString(5, medication.getImagePath());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    medication.setId(generatedKeys.getInt(1));
                    conn.commit();
                    return true;
                }
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    public static boolean updateMedication(Medication medication) throws SQLException {
        String sql = "UPDATE medicament SET nom = ?, description = ?, quantite_stock = ?, prix = ?, image_path = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, medication.getNom());
            stmt.setString(2, medication.getDescription());
            stmt.setInt(3, medication.getQuantiteStock());
            stmt.setDouble(4, medication.getPrix());
            stmt.setString(5, medication.getImagePath());
            stmt.setInt(6, medication.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    public static void deleteMedication(int id) throws SQLException {
        String sql = "DELETE FROM medicament WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    public static Medication getMedicationById(int id) throws SQLException {
        String sql = "SELECT * FROM medicament WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Medication medication = new Medication(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getInt("quantite_stock"),
                        rs.getDouble("prix")
                    );
                    
                    if (rs.getString("image_path") != null) {
                        medication.setImagePath(rs.getString("image_path"));
                    }
                    
                    return medication;
                }
            }
        }
        
        return null;
    }
    
    public static boolean isUsedInOrder(int medicationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ligne_commande WHERE medication_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, medicationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
}