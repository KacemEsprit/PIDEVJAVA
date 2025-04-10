package com.pfe.nova.services;

import com.pfe.nova.models.Rapport;
import com.pfe.nova.configuration.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RapportService {

    public boolean createRapport(Rapport rapport) {
        String query = "INSERT INTO rapports (patient_id, medecin_id, date_rapport, sexe, age, traitement, dose_medicament, frequence_traitement, perte_de_sang, temps_operation, filtration_sang, creatinine, score_glasgow, respiration_assistee, complications) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, rapport.getPatientId());
            preparedStatement.setInt(2, rapport.getMedecinId());
            preparedStatement.setDate(3, Date.valueOf(rapport.getDateRapport()));
            preparedStatement.setString(4, rapport.getSexe());
            preparedStatement.setInt(5, rapport.getAge());
            preparedStatement.setString(6, rapport.getTraitement());
            preparedStatement.setString(7, rapport.getDoseMedicament());
            preparedStatement.setString(8, rapport.getFrequenceTraitement());
            preparedStatement.setString(9, rapport.getPerteDeSang());
            preparedStatement.setString(10, rapport.getTempsOperation());
            preparedStatement.setString(11, rapport.getFiltrationSang());
            preparedStatement.setString(12, rapport.getCreatinine());
            preparedStatement.setString(13, rapport.getScoreGlasgow());
            preparedStatement.setString(14, rapport.getRespirationAssistee());
            preparedStatement.setString(15, rapport.getComplications());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRapport(Rapport rapport) {
        String query = "UPDATE rapports SET patient_id = ?, medecin_id = ?, date_rapport = ?, sexe = ?, age = ?, traitement = ?, dose_medicament = ?, frequence_traitement = ?, perte_de_sang = ?, temps_operation = ?, filtration_sang = ?, creatinine = ?, score_glasgow = ?, respiration_assistee = ?, complications = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, rapport.getPatientId());
            preparedStatement.setInt(2, rapport.getMedecinId());
            preparedStatement.setDate(3, Date.valueOf(rapport.getDateRapport()));
            preparedStatement.setString(4, rapport.getSexe());
            preparedStatement.setInt(5, rapport.getAge());
            preparedStatement.setString(6, rapport.getTraitement());
            preparedStatement.setString(7, rapport.getDoseMedicament());
            preparedStatement.setString(8, rapport.getFrequenceTraitement());
            preparedStatement.setString(9, rapport.getPerteDeSang());
            preparedStatement.setString(10, rapport.getTempsOperation());
            preparedStatement.setString(11, rapport.getFiltrationSang());
            preparedStatement.setString(12, rapport.getCreatinine());
            preparedStatement.setString(13, rapport.getScoreGlasgow());
            preparedStatement.setString(14, rapport.getRespirationAssistee());
            preparedStatement.setString(15, rapport.getComplications());
            preparedStatement.setInt(16, rapport.getId());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRapport(int id) {
        String query = "DELETE FROM rapports WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Rapport> getAllRapports() {
        List<Rapport> rapports = new ArrayList<>();
        String query = "SELECT * FROM rapports";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Rapport rapport = new Rapport();
                rapport.setId(resultSet.getInt("id"));
                rapport.setPatientId(resultSet.getInt("patient_id"));
                rapport.setMedecinId(resultSet.getInt("medecin_id"));
                rapport.setDateRapport(resultSet.getDate("date_rapport").toLocalDate());
                rapport.setSexe(resultSet.getString("sexe"));
                rapport.setAge(resultSet.getInt("age"));
                rapport.setTraitement(resultSet.getString("traitement"));
                rapport.setDoseMedicament(resultSet.getString("dose_medicament"));
                rapport.setFrequenceTraitement(resultSet.getString("frequence_traitement"));
                rapport.setPerteDeSang(resultSet.getString("perte_de_sang"));
                rapport.setTempsOperation(resultSet.getString("temps_operation"));
                rapport.setFiltrationSang(resultSet.getString("filtration_sang"));
                rapport.setCreatinine(resultSet.getString("creatinine"));
                rapport.setScoreGlasgow(resultSet.getString("score_glasgow"));
                rapport.setRespirationAssistee(resultSet.getString("respiration_assistee"));
                rapport.setComplications(resultSet.getString("complications"));
                rapports.add(rapport);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rapports;
    }
}