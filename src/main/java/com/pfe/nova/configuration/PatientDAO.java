package com.pfe.nova.configuration;

import com.pfe.nova.models.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    private Connection connection;

    public PatientDAO() throws SQLException {
        connection = DatabaseConnection.getConnection();
        System.out.println("PatientDAO initialized with connection: " + (connection != null));
    }

    public static String getPatientName(int patientId) {
        String query = "SELECT nom, prenom FROM user WHERE id = ? AND (role = 'ROLE_PATIENT' OR role = 'PATIENT')";
        String patientName = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                patientName = nom + " " + prenom;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patientName;
    }

    public List<Patient> displayPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String query = "SELECT * FROM user WHERE role = 'ROLE_PATIENT' OR role = 'PATIENT'";

        try (PreparedStatement pst = connection.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("id"));
                patient.setNom(rs.getString("nom"));
                patient.setPrenom(rs.getString("prenom"));
                patient.setEmail(rs.getString("email"));
                patient.setTel(rs.getString("tel"));
                patient.setAdresse(rs.getString("adresse"));
                patient.setAge(rs.getInt("age"));
                patient.setGender(rs.getString("gender"));
                patient.setBloodType(rs.getString("blood_type"));
                patient.setPicture(rs.getString("picture"));
                patients.add(patient);
                System.out.println("Loaded patient: " + patient.getNom() + " " + patient.getPrenom());
            }
        }
        return patients;
    }

    public Patient getOne(int id) throws SQLException {
        String query = "SELECT * FROM user WHERE id = ? AND (role = 'ROLE_PATIENT' OR role = 'PATIENT')";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Patient patient = new Patient();
                    patient.setId(rs.getInt("id"));
                    patient.setNom(rs.getString("nom"));
                    patient.setPrenom(rs.getString("prenom"));
                    patient.setEmail(rs.getString("email"));
                    patient.setTel(rs.getString("tel"));
                    patient.setAdresse(rs.getString("adresse"));
                    patient.setAge(rs.getInt("age"));
                    patient.setGender(rs.getString("gender"));
                    patient.setBloodType(rs.getString("blood_type"));
                    patient.setPicture(rs.getString("picture"));
                    return patient;
                }
            }
        }
        return null;
    }

    public List<Integer> getAllIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id FROM user WHERE role = 'ROLE_PATIENT' OR role = 'PATIENT'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        }
        System.out.println("Retrieved IDs: " + ids); // Debug statement
        return ids;
    }


}