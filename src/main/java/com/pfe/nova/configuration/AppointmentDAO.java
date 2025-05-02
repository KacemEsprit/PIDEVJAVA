package com.pfe.nova.configuration;

import com.pfe.nova.models.Appointment;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.pfe.nova.utils.SmsUtil;
import com.pfe.nova.configuration.UserDAO;

public class AppointmentDAO {
    private static final Logger LOGGER = Logger.getLogger(AppointmentDAO.class.getName());

    public String getPatientNameById(int patientId) {
        String query = "SELECT CONCAT(nom, ' ', prenom) as full_name FROM user WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("full_name");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting patient name", e);
        }
        return "Unknown Patient";
    }

    public boolean updateAppointmentDateTime(int appointmentId, LocalDateTime newDateTime) {
        String query = "UPDATE rendezvous SET date_time = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, Timestamp.valueOf(newDateTime));
            stmt.setInt(2, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating appointment date/time", e);
            return false;
        }
    }
    public boolean createAppointment(Appointment appointment) {
        String query = "INSERT INTO rendezvous (patient_id, doctor_id, date_time, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDoctorId());
            stmt.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            stmt.setString(4, appointment.getStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    appointment.setId(rs.getInt(1));
                }
                // Send SMS notification to patient
                String patientPhone = UserDAO.getUserPhoneById(appointment.getPatientId());
                if (patientPhone != null && !patientPhone.isEmpty()) {
                    String smsBody = "Votre rendez-vous a été confirmé pour le " + appointment.getAppointmentDateTime().toLocalDate() + " à " + appointment.getAppointmentDateTime().toLocalTime() + ".";
                    SmsUtil.sendSMS(smsBody);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Appointment> getPatientAppointments(int patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT * FROM rendezvous WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public boolean cancelAppointment(int appointmentId) {
        String query = "UPDATE rendezvous SET status = 'CANCELLED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Appointment> getDoctorAppointments(int doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT * FROM rendezvous WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
            rs.getInt("id"),
            rs.getInt("patient_id"),
            rs.getInt("doctor_id"),
            rs.getTimestamp("date_time").toLocalDateTime(),
            rs.getString("status")

        );
    }
    public String getRapportByRendezvousId(int rendezvousId) {
        String query = "SELECT content FROM rapport WHERE rendezvous_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rendezvousId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("content");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.getLogger(AppointmentDAO.class.getName()).log(Level.SEVERE,
                    "Error fetching rapport for rendezvous ID: " + rendezvousId, e);
        }
        return null; // Return null if no rapport is found
    }

    public boolean saveRapport(int rendezvousId, String content) {
        String query = "INSERT INTO rapport (rendezvous_id, content) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rendezvousId);
            stmt.setString(2, content);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.getLogger(AppointmentDAO.class.getName()).log(Level.SEVERE,
                    "Error saving rapport for rendezvous ID: " + rendezvousId, e);
            return false;
        }
    }
    public boolean updateRapport(int rendezvousId, String content) {
        String query = "UPDATE rapport SET content = ? WHERE rendezvous_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, content);
            stmt.setInt(2, rendezvousId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.getLogger(AppointmentDAO.class.getName()).log(Level.SEVERE,
                    "Error updating rapport for rendezvous ID: " + rendezvousId, e);
            return false;
        }
    }
    public boolean deleteRapport(int rendezvousId) {
        String query = "DELETE FROM rapport WHERE rendezvous_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rendezvousId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.getLogger(AppointmentDAO.class.getName()).log(Level.SEVERE,
                    "Error deleting rapport for rendezvous ID: " + rendezvousId, e);
            return false;
        }
    }
}