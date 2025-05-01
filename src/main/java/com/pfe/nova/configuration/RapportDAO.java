package com.pfe.nova.configuration;



import com.pfe.nova.models.Rapport;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.sql.*;
import java.util.*;

public class RapportDAO {


    public RapportDAO() {
    }
    private static final String ACCOUNT_SID = "ACe6a8fd190e9cb3b97df42fa0c300087f";
    private static final String AUTH_TOKEN = "6ecf570cfed6e53e33cbaf641e98b607";
    private static final String TWILIO_PHONE_NUMBER = "+13373433844"; // Twilio phone number

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }
    // Create a new rapport
    public boolean create(Rapport rapport) {
        String sql = "INSERT INTO rapport_detat (patient_id, medecin_id, date_rapport, age, sexe, tension_arterielle, pouls, temperature, saturation_oxygene, imc, niveau_douleur, traitement, dose_medicament, frequence_traitement, perte_de_sang, temps_operation, duree_seance, filtration_sang, creatinine, score_glasgow, respiration_assistee, complications) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, rapport.getPatientId());
            stmt.setInt(2, rapport.getMedecinId());
            stmt.setString(3, rapport.getDateRapport());
            stmt.setInt(4, rapport.getAge());
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

            int r = stmt.executeUpdate();

            // Check oxygen level and send notification if below 90
            if (rapport.getSaturationOxygene() < 90 || rapport.getPouls() < 60 || rapport.getPouls() > 100 ||
                    rapport.getTemperature() < 36.0 || rapport.getTemperature() > 37.5 ||
                    rapport.getTensionArterielle() < 90 || rapport.getTensionArterielle() > 140 ||
                    rapport.getPerteDeSang() > 500) {
                sendSmsToDoctor(
                        rapport.getPatientId(),
                        rapport.getMedecinId(),
                        rapport.getSaturationOxygene(),
                        rapport.getPouls(),
                        rapport.getTemperature(),
                        rapport.getTensionArterielle(),
                        rapport.getPerteDeSang()
                );
            }

            return r > 0;
        } catch (SQLException e) {
            System.out.println("Error while creating rapport: " + e.getMessage());
            throw new RuntimeException(e);
        }
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

            if (rapport.getSaturationOxygene() < 90 || rapport.getPouls() < 60 || rapport.getPouls() > 100 ||
                    rapport.getTemperature() < 36.0 || rapport.getTemperature() > 37.5 ||
                    rapport.getTensionArterielle() < 90 || rapport.getTensionArterielle() > 140 ||
                    rapport.getPerteDeSang() > 500) {
                sendSmsToDoctor(
                        rapport.getPatientId(),
                        rapport.getMedecinId(),
                        rapport.getSaturationOxygene(),
                        rapport.getPouls(),
                        rapport.getTemperature(),
                        rapport.getTensionArterielle(),
                        rapport.getPerteDeSang()
                );
            }

            return r > 0;
        } catch (SQLException e) {
            System.out.println("Error while updating rapport: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void sendSmsToDoctor(int patientId, int medecinId, int oxygenLevel, int pulse, double temperature, int bloodPressure, int bloodLoss) {
        // Fetch the doctor's phone number (replace with actual logic to fetch from DB)
        String doctorPhoneNumber = "+21653628663"; // Example phone number

        // Fetch the patient's name
        String patientName = PatientDAO.getPatientName(patientId); // Fetch patient's name from DB
        if (patientName == null) {
            patientName = "Unknown Patient";
        }

        // Build the alert message based on conditions
        StringBuilder messageBody = new StringBuilder();
        messageBody.append("Alert: Patient ").append(patientName)
                .append(" (ID: ").append(patientId).append(") is in critical condition due to:");

        boolean critical = false;

        if (oxygenLevel < 90) {
            messageBody.append("\n- Low oxygen level (").append(oxygenLevel).append("%)");
            critical = true;
        }
        if (pulse < 60 || pulse > 100) {
            messageBody.append("\n- Abnormal pulse (").append(pulse).append(" bpm)");
            critical = true;
        }
        if (temperature < 36.0 || temperature > 37.5) {
            messageBody.append("\n- Abnormal temperature (").append(temperature).append(" °C)");
            critical = true;
        }
        if (bloodPressure < 90 || bloodPressure > 140) {
            messageBody.append("\n- Abnormal blood pressure (").append(bloodPressure).append(" mmHg)");
            critical = true;
        }
        if (bloodLoss > 500) {
            messageBody.append("\n- Excessive blood loss (").append(bloodLoss).append(" mL)");
            critical = true;
        }

        if (!critical) {
            System.out.println("No critical conditions detected. SMS not sent.");
            return;
        }

        try {
            // Send SMS
            Message message = Message.creator(
                    new PhoneNumber(doctorPhoneNumber), // Doctor's phone number
                    new PhoneNumber(TWILIO_PHONE_NUMBER), // Twilio phone number
                    messageBody.toString()
            ).create();

            System.out.println("SMS sent successfully: " + message.getSid());
        } catch (Exception e) {
            System.out.println("Error while sending SMS: " + e.getMessage());
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

    public static String getPatientName(int patientId) {
        String sql = "SELECT nom FROM user WHERE id = ?";
        String patientName = null;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                patientName = rs.getString("nom");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving patient name: " + e.getMessage());
            e.printStackTrace();
        }

        return patientName;
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

    public static Rapport getLatestPatientRapport(int userId) throws SQLException {
        String sql = """
        SELECT r.*
        FROM rapport_detat r
        JOIN user u ON r.patient_id = u.id
        WHERE u.id = ? AND u.role = 'ROLE_PATIENT'
        ORDER BY r.date_rapport DESC
        LIMIT 1
    """;

        Rapport rapport = null;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rapport = new Rapport();
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
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du dernier rapport : " + e.getMessage());
            throw e;
        }

        return rapport;
    }





}

