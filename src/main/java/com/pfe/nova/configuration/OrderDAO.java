package com.pfe.nova.configuration;

import com.pfe.nova.models.Medication;
import com.pfe.nova.models.Order;
import com.pfe.nova.models.User;
import com.pfe.nova.utils.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    
    public static int createOrder(Order order) throws SQLException {
        if (order.getUser() == null || order.getUser().getId() <= 0) {
            throw new SQLException("L'utilisateur ne peut pas être null et doit avoir un ID valide pour créer une commande.");
        }
        
        String sql = "INSERT INTO commande (status, date_commande, quantite_total, montant_total, user_id, rate) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, order.getStatus());
            pstmt.setTimestamp(2, Timestamp.valueOf(order.getDateCommande()));
            pstmt.setInt(3, order.getQuantiteTotal());
            pstmt.setDouble(4, order.getMontantTotal());
            pstmt.setInt(5, order.getUser().getId());
            pstmt.setInt(6, order.getRate());

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        order.setId(orderId);
                        
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
    
    private static void addMedicationToOrder(int orderId, int medicationId, int quantite) throws SQLException {
        String sql = "INSERT INTO ligne_commande (order_id, medication_id, quantite) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, medicationId);
            pstmt.setInt(3, quantite);
            
            pstmt.executeUpdate();
        }
    }
    
    private static void addMedicationToOrder(int orderId, int medicationId) throws SQLException {
        addMedicationToOrder(orderId, medicationId, 1);
    }
    
    public static Order getOrderById(int id) throws SQLException {
        String sql = "SELECT o.*, u.* FROM commande o JOIN user u ON o.user_id = u.id WHERE o.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    
                    Order order = new Order(
                        rs.getInt("id"),
                        rs.getString("status"),
                        rs.getTimestamp("date_commande").toLocalDateTime(),
                        rs.getInt("quantite_total"),
                        rs.getDouble("montant_total")
                    );
                    order.setUser(user);
                    
                    // Récupérer les médicaments de la commande
                    order.setMedications(getMedicationsForOrder(id));
                    
                    return order;
                }
            }
        }
        
        return null;
    }
    
    public static List<Medication> getMedicationsForOrder(int orderId) throws SQLException {
        String sql = "SELECT m.*, om.medication_id, om.quantite FROM medicament m " +
                     "JOIN ligne_commande om ON m.id = om.medication_id " +
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
                    
                    medication.setQuantiteCommande(rs.getInt("quantite"));
                    
                    medications.add(medication);
                }
            }
        }
        
        return medications;
    }
    
    public static List<Order> getAllOrders() throws SQLException {
        User currentUser = Session.getUtilisateurConnecte();
        if (currentUser == null) {
            throw new SQLException("Aucun utilisateur connecté");
        }
        
        String orderSql;
        if (currentUser.getRole().equals("ADMIN")) {
            // Pour l'admin, récupérer toutes les commandes
            orderSql = "SELECT o.*, u.* FROM commande o JOIN user u ON o.user_id = u.id ORDER BY date_commande DESC";
        } else {
            // Pour les autres utilisateurs, filtrer par user_id
            orderSql = "SELECT o.*, u.* FROM commande o JOIN user u ON o.user_id = u.id WHERE o.user_id = ? ORDER BY date_commande DESC";
        }
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
            
            if (!currentUser.getRole().equals("ADMIN")) {
                pstmt.setInt(1, currentUser.getId());
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                      User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                
                      Order order = new Order(
                        rs.getInt("id"),
                        rs.getString("status"),
                        rs.getTimestamp("date_commande").toLocalDateTime(),
                        rs.getInt("quantite_total"),
                        rs.getDouble("montant_total")
                    );
                    order.setUser(user);
                    order.setRate(rs.getInt("rate"));

                    orders.add(order);
            }
        }
        
        for (Order order : orders) {
            order.setMedications(getMedicationsForOrder(order.getId()));
        }
        
        return orders;}
    }
    
    public static boolean updateOrder(Order order) throws SQLException {
        if (order == null || order.getId() <= 0) {
            throw new SQLException("Commande invalide");
        }

        String currentStatus = getCurrentOrderStatus(order.getId());
        if (!"En cours".equals(currentStatus) && ("Validée".equals(order.getStatus()) || "Rejetée".equals(order.getStatus()))) {
            throw new SQLException("Impossible de modifier une commande qui n'est pas en cours");
        }

        String sql = "UPDATE commande SET status = ?, quantite_total = ?, montant_total = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, order.getStatus());
            pstmt.setInt(2, order.getQuantiteTotal());
            pstmt.setDouble(3, order.getMontantTotal());
            pstmt.setInt(4, order.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                removeMedicationsFromOrder(order.getId());
                
                for (Medication medication : order.getMedications()) {
                    int quantite = medication.getQuantiteCommande() > 0 ? medication.getQuantiteCommande() : 1;
                    addMedicationToOrder(order.getId(), medication.getId(), quantite);
                }
                
                return true;
            }
            
            return false;
        }
    }

    private static String getCurrentOrderStatus(int orderId) throws SQLException {
        String sql = "SELECT status FROM commande WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
                throw new SQLException("Commande non trouvée");
            }
        }
    }
    
    private static void removeMedicationsFromOrder(int orderId) throws SQLException {
        String sql = "DELETE FROM ligne_commande WHERE order_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            pstmt.executeUpdate();
        }
    }
    
    public static boolean deleteOrder(int id) throws SQLException {
        removeMedicationsFromOrder(id);
        
        String sql = "DELETE FROM commande WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            return affectedRows > 0;
        }
    }
    
    public static boolean isMedicationUsedInOrder(int medicationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ligne_commande WHERE medication_id = ?";
        
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

    public static List<Order> getPendingOrders() throws SQLException {
        String sql = "SELECT o.*, u.* FROM commande o JOIN user u ON o.user_id = u.id WHERE o.status = 'En cours' ORDER BY o.date_commande DESC";
        List<Order> pendingOrders = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    
                    Order order = new Order(
                        rs.getInt("id"),
                        rs.getString("status"),
                        rs.getTimestamp("date_commande").toLocalDateTime(),
                        rs.getInt("quantite_total"),
                        rs.getDouble("montant_total")
                    );
                    order.setUser(user);
                    order.setMedications(getMedicationsForOrder(order.getId()));
                    
                    pendingOrders.add(order);
                }
            }
        }
        return pendingOrders;
    }

    public static boolean updateOrderStatus(int orderId, String newStatus) throws SQLException {
        String sql = "UPDATE commande SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            
            return stmt.executeUpdate() > 0;
        }
        
    }
    public static boolean rateOrder(int orderId, int rate) throws SQLException {
        if (rate < 1 || rate > 5) {
            throw new IllegalArgumentException("Le score doit être entre 1 et 5.");
        }
    
        String sql = "UPDATE commande SET rate = ? WHERE id = ?";
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setInt(1, rate);
            pstmt.setInt(2, orderId);
    
            return pstmt.executeUpdate() > 0;
        }
    }
    public void updateOrderRating(int orderId, int rating) throws SQLException {
        String query = "UPDATE commande SET rate = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rating);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
    }
    public List<Order> getAllRatedOrders() throws SQLException {
        List<Order> ratedOrders = new ArrayList<>();
        String query = "SELECT * FROM commande WHERE rate IS NOT NULL";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setRate(rs.getInt("rate"));
                // ... autres champs nécessaires ...
                ratedOrders.add(order);
            }
        }
        return ratedOrders;
    }
}