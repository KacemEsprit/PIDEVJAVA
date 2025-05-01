package com.pfe.nova.services;

import com.pfe.nova.configuration.DatabaseConnection;
import com.pfe.nova.models.Don;
import com.pfe.nova.models.Donateur;
import com.pfe.nova.models.User;
import com.pfe.nova.utils.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonService implements IService<Don> {

    private Connection connection;

    public DonService() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
        }
    }

    @Override
    public void ajouter(Don don) throws SQLException {

        User sessionUser = SessionManager.getCurrentUser();
        if (sessionUser == null) {
            throw new SQLException("Aucun utilisateur connecté");
        }
        if (!(sessionUser instanceof Donateur)) {
            throw new SQLException("L'utilisateur connecté n'est pas un donateur");
        }
        don.setDonateurId(sessionUser.getId());

        String query = "INSERT INTO don (type_don, montant, description_materiel, date_don, donateur_id, campagne_id, mode_paiement, preuve_don, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, don.getTypeDon());
            statement.setDouble(2, don.getMontant());
            statement.setString(3, don.getDescriptionMateriel());
            statement.setDate(4, don.getDateDon());
            statement.setInt(5, don.getDonateurId());
            if (don.getCampagneId() > 0) {
                statement.setInt(6, don.getCampagneId());
            } else {
                statement.setNull(6, Types.INTEGER);
            }
            statement.setString(7, don.getModePaiement());
            statement.setString(8, don.getPreuveDon());
            statement.setString(9, don.getStatut());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean modifier(Don don) throws SQLException {

        String selectSql = "SELECT campagne_id FROM don WHERE id=?";
        int existingCampagneId;
        try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
            selectStmt.setInt(1, don.getId());
            ResultSet rs = selectStmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Don non trouvé avec l'ID: " + don.getId());
            }
            existingCampagneId = rs.getInt("campagne_id");
        }


        String sql = "UPDATE don SET type_don=?, montant=?, description_materiel=?, date_don=?, donateur_id=?, mode_paiement=?, preuve_don=? WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, don.getTypeDon());
            preparedStatement.setDouble(2, don.getMontant());
            preparedStatement.setString(3, don.getDescriptionMateriel());
            preparedStatement.setDate(4, don.getDateDon());
            preparedStatement.setInt(5, don.getDonateurId());
            preparedStatement.setString(6, don.getModePaiement());
            preparedStatement.setString(7, don.getPreuveDon());
            preparedStatement.setInt(8, don.getId());
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM don WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<Don> recuperer() throws SQLException {
        User sessionUser = SessionManager.getCurrentUser();
        if (sessionUser == null) {
            throw new SQLException("Aucun utilisateur connecté");
        }
        if (!(sessionUser instanceof Donateur)) {
            throw new SQLException("L'utilisateur connecté n'est pas un donateur. Type actuel: " + sessionUser.getClass().getSimpleName());
        }
        String sql = "SELECT * FROM don WHERE donateur_id = ?";
        List<Don> dons = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sessionUser.getId());
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Don don = new Don(
                        rs.getInt("id"),
                        rs.getString("type_don"),
                        rs.getDouble("montant"),
                        rs.getString("description_materiel"),
                        rs.getDate("date_don"),
                        rs.getInt("donateur_id"),
                        rs.getInt("campagne_id"),
                        rs.getString("mode_paiement"),
                        rs.getString("preuve_don")
                );
                don.setStatut(rs.getString("statut"));
                dons.add(don);
            }
        }
        return dons;
    }

    @Override
    public List<Don> getAll() throws SQLException {
        return recuperer();
    }

    public List<Don> rechercherParType(String typeDon) throws SQLException {
        String sql = "SELECT * FROM don WHERE type_don LIKE ?";
        List<Don> dons = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + typeDon + "%");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Don don = new Don(
                        rs.getInt("id"),
                        rs.getString("type_don"),
                        rs.getDouble("montant"),
                        rs.getString("description_materiel"),
                        rs.getDate("date_don"),
                        rs.getInt("donateur_id"),
                        rs.getInt("campagne_id"),
                        rs.getString("mode_paiement"),
                        rs.getString("preuve_don")
                );
                don.setStatut(rs.getString("statut"));
                dons.add(don);
            }
        }
        return dons;
    }

    // Récupérer tous les dons pour l'historique admin (tous donateurs, toutes compagnies ou individuels)
    public List<Don> recupererTousLesDons() {
        List<Don> dons = new ArrayList<>();
        String sql = "SELECT d.id, d.type_don, d.montant, d.description_materiel, d.date_don, d.donateur_id, d.campagne_id, d.mode_paiement, d.preuve_don, d.statut, " +
                "COALESCE(u.nom, u.email) AS donateur, " +
                "CASE WHEN d.campagne_id IS NOT NULL THEN (SELECT nom FROM compagnie WHERE id = (SELECT compagnie_id FROM campagne WHERE id = d.campagne_id)) ELSE 'Individuel' END AS beneficiaire, " +
                "u.donateur_type AS typeDonateur " +
                "FROM don d LEFT JOIN user u ON d.donateur_id = u.id ORDER BY d.date_don DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Don don = new Don(
                        rs.getInt("id"),
                        rs.getString("type_don"),
                        rs.getDouble("montant"),
                        rs.getString("description_materiel"),
                        rs.getDate("date_don"),
                        rs.getInt("donateur_id"),
                        rs.getInt("campagne_id"),
                        rs.getString("mode_paiement"),
                        rs.getString("preuve_don")
                );
                don.setStatut(rs.getString("statut"));
                don.setDonateur(rs.getString("donateur"));
                don.setBeneficiaire(rs.getString("beneficiaire"));
                String typeDonateur = rs.getString("typeDonateur");
                if (typeDonateur == null || typeDonateur.isEmpty()) {
                    typeDonateur = "Individuel";
                } else if (typeDonateur.equalsIgnoreCase("COMPAGNIE")) {
                    typeDonateur = "Compagnie";
                } else {
                    typeDonateur = "Individuel";
                }
                don.setTypeDonateur(typeDonateur);
                dons.add(don);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dons;
    }
}
