package com.pfe.nova.configuration;

import com.pfe.nova.models.SessionDeTraitement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SessionTraitementDAO {

    public boolean createSession(SessionDeTraitement session) throws SQLException {
        String query = "INSERT INTO session_de_traitement (date_session, type_session, num_de_chambre, patient_id, duree, medecin_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, session.getDateSession());
            stmt.setString(2, session.getTypeSession());
            stmt.setInt(3, session.getNumDeChambre());
            stmt.setInt(4, session.getPatientId());
            stmt.setInt(5, session.getDuree());
            stmt.setInt(6, session.getMedecinId());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<SessionDeTraitement> getAllSessions() throws SQLException {
        List<SessionDeTraitement> sessions = new ArrayList<>();
        String query = "SELECT * FROM session_de_traitement";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sessions.add(new SessionDeTraitement(
                    rs.getInt("id"),
                    rs.getString("date_session"),
                    rs.getString("type_session"),
                    rs.getInt("num_de_chambre"),
                    rs.getInt("patient_id"),
                    rs.getInt("duree"),
                    rs.getInt("medecin_id")
                ));
            }
        }
        return sessions;
    }

    public boolean updateSession(SessionDeTraitement session) throws SQLException {
        String query = "UPDATE session_de_traitement SET date_session = ?, duree = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Mettre à jour seulement la date et la durée
            stmt.setString(1, session.getDateSession());
            stmt.setInt(2, session.getDuree());
            stmt.setInt(3, session.getId());  // ID de la session à mettre à jour

            return stmt.executeUpdate() > 0; // Si une ligne est affectée, la mise à jour est réussie
        }
    }

    public boolean deleteSession(int id) throws SQLException {
        String query = "DELETE FROM session_de_traitement WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}