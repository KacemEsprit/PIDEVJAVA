package com.pfe.nova.configuration;

import com.pfe.nova.models.Order;
import com.pfe.nova.models.Medication;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    
    // Créer une commande
    public static int createOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (status, date_commande, quantite_total, montant_total) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, order.getStatus());
            pstmt.setTimestamp(2, Timestamp.valueOf(order.getDateCommande()));
            pstmt.setInt(3, order.getQuantiteTotal());
            pstmt.setDouble(4, order.getMontantTotal());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        order.setId(orderId);
                        
                        // Ajouter les médicaments à la commande avec leur quantité
                        for (Medication medication : order.getMedications()) {
                            int quantite = medication.getQuantiteCommande() > 0 ? medication.getQuantiteCommande() : 1;
                            addMedicationToOrder(orderId, medication.getId(), quantite);
                        }
                        
                        return orderId;
                    }
                }
            }
            
            throw new SQLException("La création de la commande a échoué, aucun ID obtenu.");
        }
    }
    
    // Ajouter un médicament à une commande avec une quantité spécifique
    private static void addMedicationToOrder(int orderId, int medicationId, int quantite) throws SQLException {
        String sql = "INSERT INTO order_medications (order_id, medication_id, quantite) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, medicationId);
            pstmt.setInt(3, quantite);
            
            pstmt.executeUpdate();
        }
    }
    
    // Ajouter un médicament à une commande avec quantité par défaut = 1
    private static void addMedicationToOrder(int orderId, int medicationId) throws SQLException {
        addMedicationToOrder(orderId, medicationId, 1);
    }
    
    // Récupérer une commande par ID
    public static Order getOrderById(int id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order(
                        rs.getInt("id"),
                        rs.getString("status"),
                        rs.getTimestamp("date_commande").toLocalDateTime(),
                        rs.getInt("quantite_total"),
                        rs.getDouble("montant_total")
                    );
                    
                    // Récupérer les médicaments de la commande
                    order.setMedications(getMedicationsForOrder(id));
                    
                    return order;
                }
            }
        }
        
        return null;
    }
    
    // Récupérer les médicaments pour une commande avec leurs quantités
    public static List<Medication> getMedicationsForOrder(int orderId) throws SQLException {
        String sql = "SELECT m.*, om.medication_id, om.quantite FROM medicament m " +
                     "JOIN order_medications om ON m.id = om.medication_id " +
                     "WHERE om.order_id = ?";
        List<Medication> medications = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Medication medication = new Medication(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getInt("quantite_stock"),
                        rs.getDouble("prix")
                    );
                    medication.setImagePath(rs.getString("image_path"));
                    
                    // Récupérer la quantité commandée depuis la table order_medications
                    medication.setQuantiteCommande(rs.getInt("quantite"));
                    
                    // Ajouter le médicament à la liste une seule fois
                    medications.add(medication);
                }
            }
        }
        
        return medications;
    }
    
    // Récupérer toutes les commandes
    public static List<Order> getAllOrders() throws SQLException {
        // D'abord, récupérer toutes les commandes
        String orderSql = "SELECT * FROM orders ORDER BY date_commande DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(orderSql)) {
            
            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("id"),
                    rs.getString("status"),
                    rs.getTimestamp("date_commande").toLocalDateTime(),
                    rs.getInt("quantite_total"),
                    rs.getDouble("montant_total")
                );
                orders.add(order);
            }
        }
        
        // Ensuite, pour chaque commande, récupérer ses médicaments
        for (Order order : orders) {
            order.setMedications(getMedicationsForOrder(order.getId()));
        }
        
        return orders;
    }
    
    // Mettre à jour une commande
    public static boolean updateOrder(Order order) throws SQLException {
        String sql = "UPDATE orders SET status = ?, quantite_total = ?, montant_total = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, order.getStatus());
            pstmt.setInt(2, order.getQuantiteTotal());
            pstmt.setDouble(3, order.getMontantTotal());
            pstmt.setInt(4, order.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Supprimer les anciennes relations
                removeMedicationsFromOrder(order.getId());
                
                // Ajouter les nouvelles relations avec leurs quantités
                for (Medication medication : order.getMedications()) {
                    int quantite = medication.getQuantiteCommande() > 0 ? medication.getQuantiteCommande() : 1;
                    addMedicationToOrder(order.getId(), medication.getId(), quantite);
                }
                
                return true;
            }
            
            return false;
        }
    }
    
    // Supprimer les médicaments d'une commande
    private static void removeMedicationsFromOrder(int orderId) throws SQLException {
        String sql = "DELETE FROM order_medications WHERE order_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            pstmt.executeUpdate();
        }
    }
    
    // Supprimer une commande
    public static boolean deleteOrder(int id) throws SQLException {
        // D'abord supprimer les relations avec les médicaments
        removeMedicationsFromOrder(id);
        
        // Ensuite supprimer la commande
        String sql = "DELETE FROM orders WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            return affectedRows > 0;
        }
    }
    
    // Vérifier si un médicament est utilisé dans une commande
    public static boolean isMedicationUsedInOrder(int medicationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_medications WHERE medication_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, medicationId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
}