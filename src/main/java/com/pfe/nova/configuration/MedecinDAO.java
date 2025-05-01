package com.pfe.nova.configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedecinDAO {

    private Connection connection;

    public MedecinDAO() {
        try {
            connection = DatabaseConnection.getConnection();
            System.out.println("MedecinDAO initialized with connection: " + (connection != null));
        } catch (Exception e) {
            System.err.println("Error initializing MedecinDAO: " + e.getMessage());
        }
    }

    public List<Integer> getAllIds() {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id FROM user WHERE role = 'ROLE_MEDECIN' OR role = 'MEDECIN'";
        try (PreparedStatement pst = connection.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching IDs: " + e.getMessage());
        }
        return ids;
    }
}
