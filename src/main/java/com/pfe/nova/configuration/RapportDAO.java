package com.pfe.nova.configuration;

import com.pfe.nova.models.Rapport;

import java.sql.*;
import java.util.*;

public class RapportDAO {

public boolean create(Rapport rapport){
    String sql = "INSERT INTO rapport_detat (patient_id, medecin_id, date_rapport,age , sexe, tension_arterielle, pouls, temperature, saturation_oxygene, imc, niveau_douleur, traitement, dose_medicament, frequence_traitement, perte_de_sang, temps_operation, duree_seance, filtration_sang, creatinine, score_glasgow, respiration_assistee, complications) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement stmt = connection.prepareStatement(sql)) {


        stmt.setInt(1, rapport.getPatientId());
        stmt.setInt(2, rapport.getMedecinId());
        stmt.setInt(4, rapport.getAge());
        stmt.setString(3, rapport.getDateRapport());
        stmt.setString(5, rapport.getSexe());
        stmt.setInt(6, rapport.getTensionArterielle());
        stmt.setInt(7, rapport.getPouls());
        stmt.setDouble(8, rapport.getTemperature());
        stmt.setInt(9, rapport.getSaturationOxygene());
        stmt.setDouble(10, rapport.getImc());
        stmt.setInt(11, rapport.getNiveauDouleur());
        stmt.setString(12, rapport.getTraitement());
        stmt.setInt(13, rapport.getDoseMedicament());
        stmt.setString(14, rapport.getFrequenceTraitement());
        stmt.setInt(15, rapport.getPerteDeSang());
        stmt.setInt(16, rapport.getTempsOperation());
        stmt.setInt(17, rapport.getDureeSeance());
        stmt.setInt(18, rapport.getFiltrationSang());
        stmt.setInt(19, rapport.getCreatinine());
        stmt.setInt(20, rapport.getScoreGlasgow());
        stmt.setInt(21, rapport.isRespirationAssistee());
        stmt.setString(22, rapport.getComplications());

int r=stmt.executeUpdate();
       return r>0;
    } catch (SQLException e) {
        System.out.println("Error while creating rapport: " + e.getMessage());
        throw new RuntimeException(e);
    }
}
public Set<Rapport> getAll(){
    Set<Rapport> rapports = new TreeSet<>(
            Comparator.comparing(Rapport::getDateRapport)
    );
   try(Connection connection=DatabaseConnection.getConnection();
       Statement stmt=connection.createStatement();
       ResultSet rs=stmt.executeQuery("SELECT * FROM rapport_detat")) {
       while (rs.next()) {
           Rapport rapport = new Rapport();
           rapport.setId(rs.getInt("id"));
           rapport.setPatientId(rs.getInt("patient_id"));
           rapport.setMedecinId(rs.getInt("medecin_id"));
           rapport.setAge(rs.getInt("age"));
           rapport.setDateRapport(rs.getString("date_rapport"));
           rapport.setSexe(rs.getString("sexe"));
           rapport.setTensionArterielle(rs.getInt("tension_arterielle"));
           rapport.setPouls(rs.getInt("pouls"));
           rapport.setTemperature(rs.getDouble("temperature"));
           rapport.setSaturationOxygene(rs.getInt("saturation_oxygene"));
           rapport.setImc(rs.getDouble("imc"));
           rapport.setNiveauDouleur(rs.getInt("niveau_douleur"));
           rapport.setTraitement(rs.getString("traitement"));
           rapport.setDoseMedicament(rs.getInt("dose_medicament"));
           rapport.setFrequenceTraitement(rs.getString("frequence_traitement"));
           rapport.setPerteDeSang(rs.getInt("perte_de_sang"));
           rapport.setTempsOperation(rs.getInt("temps_operation"));
           rapport.setDureeSeance(rs.getInt("duree_seance"));
           rapport.setFiltrationSang(rs.getInt("filtration_sang"));
           rapport.setCreatinine(rs.getInt("creatinine"));
           rapport.setScoreGlasgow(rs.getInt("score_glasgow"));
           rapport.setRespirationAssistee(rs.getInt("respiration_assistee"));
           rapport.setComplications(rs.getString("complications"));
           rapports.add(rapport);
       }
   } catch (SQLException e) {
       System.out.println("Error while fetching rapports: " + e.getMessage());

       }
    return rapports;
}
    public List<Rapport> getAlls(){
        List<Rapport> rapports = new ArrayList<>();
        try(Connection connection=DatabaseConnection.getConnection();
            Statement stmt=connection.createStatement();
            ResultSet rs=stmt.executeQuery("SELECT * FROM rapport_detat")) {
            while (rs.next()) {
                Rapport rapport = new Rapport();
                rapport.setId(rs.getInt("id"));
                rapport.setPatientId(rs.getInt("patient_id"));
                rapport.setMedecinId(rs.getInt("medecin_id"));
                rapport.setAge(rs.getInt("age"));
                rapport.setDateRapport(rs.getString("date_rapport"));
                rapport.setSexe(rs.getString("sexe"));
                rapport.setTensionArterielle(rs.getInt("tension_arterielle"));
                rapport.setPouls(rs.getInt("pouls"));
                rapport.setTemperature(rs.getDouble("temperature"));
                rapport.setSaturationOxygene(rs.getInt("saturation_oxygene"));
                rapport.setImc(rs.getDouble("imc"));
                rapport.setNiveauDouleur(rs.getInt("niveau_douleur"));
                rapport.setTraitement(rs.getString("traitement"));
                rapport.setDoseMedicament(rs.getInt("dose_medicament"));
                rapport.setFrequenceTraitement(rs.getString("frequence_traitement"));
                rapport.setPerteDeSang(rs.getInt("perte_de_sang"));
                rapport.setTempsOperation(rs.getInt("temps_operation"));
                rapport.setDureeSeance(rs.getInt("duree_seance"));
                rapport.setFiltrationSang(rs.getInt("filtration_sang"));
                rapport.setCreatinine(rs.getInt("creatinine"));
                rapport.setScoreGlasgow(rs.getInt("score_glasgow"));
                rapport.setRespirationAssistee(rs.getInt("respiration_assistee"));
                rapport.setComplications(rs.getString("complications"));
                rapports.add(rapport);
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching rapports: " + e.getMessage());

        }
        return rapports;
    }

    public boolean update(Rapport rapport) {
        String sql = "UPDATE rapport_detat SET tension_arterielle=?, pouls=?, temperature=?, saturation_oxygene=?, traitement=?, complications=? WHERE id=?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, rapport.getTensionArterielle());
            stmt.setInt(2, rapport.getPouls());
            stmt.setDouble(3, rapport.getTemperature());
            stmt.setInt(4, rapport.getSaturationOxygene());
            stmt.setString(5, rapport.getTraitement());
            stmt.setString(6, rapport.getComplications());
            stmt.setInt(7, rapport.getId()); // WHERE id=?

            int r = stmt.executeUpdate();
            return r > 0;

        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du rapport : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Rapport> getRapportsByPatientID(int id) throws SQLException {
        List<Rapport> rapports = new ArrayList<>();
        String sql = "SELECT * FROM rapport_detat WHERE patient_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Rapport rapport = new Rapport();
                    rapport.setId(rs.getInt("id"));
                    rapport.setPatientId(rs.getInt("patient_id"));
                    rapport.setMedecinId(rs.getInt("medecin_id"));
                    rapport.setAge(rs.getInt("age"));
                    rapport.setDateRapport(rs.getString("date_rapport"));
                    rapport.setSexe(rs.getString("sexe"));
                    rapport.setTensionArterielle(rs.getInt("tension_arterielle"));
                    rapport.setPouls(rs.getInt("pouls"));
                    rapport.setTemperature(rs.getDouble("temperature"));
                    rapport.setSaturationOxygene(rs.getInt("saturation_oxygene"));
                    rapport.setImc(rs.getDouble("imc"));
                    rapport.setNiveauDouleur(rs.getInt("niveau_douleur"));
                    rapport.setTraitement(rs.getString("traitement"));
                    rapport.setDoseMedicament(rs.getInt("dose_medicament"));
                    rapport.setFrequenceTraitement(rs.getString("frequence_traitement"));
                    rapport.setPerteDeSang(rs.getInt("perte_de_sang"));
                    rapport.setTempsOperation(rs.getInt("temps_operation"));
                    rapport.setDureeSeance(rs.getInt("duree_seance"));
                    rapport.setFiltrationSang(rs.getInt("filtration_sang"));
                    rapport.setCreatinine(rs.getInt("creatinine"));
                    rapport.setScoreGlasgow(rs.getInt("score_glasgow"));
                    rapport.setRespirationAssistee(rs.getInt("respiration_assistee"));
                    rapport.setComplications(rs.getString("complications"));

                    rapports.add(rapport);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching rapports by patient ID: " + e.getMessage());
            throw e;
        }

        return rapports;
    }
public boolean delete(int id) {
    String sql = "DELETE FROM rapport_detat WHERE id=?";
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, id);
        int r = stmt.executeUpdate();
        return r > 0;
    } catch (SQLException e) {
        System.out.println("Error while deleting rapport: " + e.getMessage());
        throw new RuntimeException(e);
    }
}






}

