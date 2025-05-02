package com.pfe.nova.configuration;

import com.pfe.nova.models.RapportRendezVous;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RapportRendezVousDAO {
    public boolean saveRapport(RapportRendezVous rapport) {
        String sql = "INSERT INTO rapport_rendezvous (rendezVousId, medecinId, dateRapport, contenu, complications) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rapport.getRendezVousId());
            stmt.setInt(2, rapport.getMedecinId());
            stmt.setString(3, rapport.getDateRapport());
            stmt.setString(4, rapport.getContenu());
            stmt.setString(5, rapport.getComplications());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}