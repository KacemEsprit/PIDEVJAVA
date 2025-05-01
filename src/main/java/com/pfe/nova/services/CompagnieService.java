package com.pfe.nova.services;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.Compagnie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompagnieService implements IService<Compagnie> {

    private Connection connection;

    public CompagnieService() throws SQLException {

        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public void ajouter(Compagnie compagnie) throws SQLException {

        String userQuery = "SELECT id FROM user WHERE role = 'DONATEUR' AND donateur_type = 'COMPAGNIE' AND email = ?";
        int donateurId;
        
        try (PreparedStatement userStmt = connection.prepareStatement(userQuery)) {
            userStmt.setString(1, compagnie.getEmail());
            ResultSet rs = userStmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Aucun donateur trouvé avec cet email");
            }
            donateurId = rs.getInt("id");
        }

        String sql = "INSERT INTO compagnie (nom, adresse, tel, email, site_web, description, logo, donateur_id, siret, date_creation, statut_juridique, statut_validation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, compagnie.getNom());
            preparedStatement.setString(2, compagnie.getAdresse());
            preparedStatement.setString(3, compagnie.getTelephone());
            preparedStatement.setString(4, compagnie.getEmail());
            preparedStatement.setString(5, compagnie.getSiteWeb());
            preparedStatement.setString(6, compagnie.getDescription());
            preparedStatement.setString(7, compagnie.getLogo());
            preparedStatement.setInt(8, donateurId);
            preparedStatement.setString(9, compagnie.getSiret());
            preparedStatement.setTimestamp(10, new java.sql.Timestamp(compagnie.getDateCreation().getTime()));
            preparedStatement.setString(11, compagnie.getStatut_juridique());
            preparedStatement.setString(12, compagnie.getStatut_validation());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public boolean modifier(Compagnie compagnie) throws SQLException {
        String sql = "UPDATE compagnie SET nom = ?, adresse = ?, tel = ?, email = ?, site_web = ?, description = ?, logo = ?, siret = ?, statut_juridique = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, compagnie.getNom());
            preparedStatement.setString(2, compagnie.getAdresse());
            preparedStatement.setString(3, compagnie.getTelephone());
            preparedStatement.setString(4, compagnie.getEmail());
            preparedStatement.setString(5, compagnie.getSiteWeb());
            preparedStatement.setString(6, compagnie.getDescription());
            preparedStatement.setString(7, compagnie.getLogo());
            preparedStatement.setString(8, compagnie.getSiret());
            preparedStatement.setString(9, compagnie.getStatut_juridique());
            preparedStatement.setInt(10, compagnie.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        }

    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM compagnie WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<Compagnie> recuperer() {
        List<Compagnie> compagnies = new ArrayList<>();
        String query = "SELECT * FROM compagnie";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Compagnie compagnie = new Compagnie(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("adresse"),
                        resultSet.getString("tel"),
                        resultSet.getString("email"),
                        resultSet.getString("site_web"),
                        resultSet.getString("description"),
                        resultSet.getString("logo"),
                        resultSet.getString("siret"),
                        resultSet.getString("statut_juridique")
                );
                compagnies.add(compagnie);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception for debugging
        }

        return compagnies;
    }

    @Override
    public List<Compagnie> getAll() throws SQLException {
        return recuperer(); // Reuse the recuperer method
    }

    public boolean hasCompagnie(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM compagnie WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<Compagnie> rechercherParNom(String nom) {
        List<Compagnie> compagnies = new ArrayList<>();
        String query = "SELECT * FROM compagnie WHERE nom LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + nom + "%");
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Compagnie compagnie = new Compagnie(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("adresse"),
                        resultSet.getString("tel"),
                        resultSet.getString("email"),
                        resultSet.getString("site_web"),
                        resultSet.getString("description"),
                        resultSet.getString("logo"),
                        resultSet.getString("siret"),
                        resultSet.getString("statut_juridique")
                    );
                    compagnies.add(compagnie);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return compagnies;
    }


    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}