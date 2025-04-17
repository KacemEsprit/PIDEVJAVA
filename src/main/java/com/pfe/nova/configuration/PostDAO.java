package com.pfe.nova.configuration;

import com.pfe.nova.models.Post;
import com.pfe.nova.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {
    
    public static void save(Post post) throws SQLException {
        if (post.getId() == null) {
            insert(post);
        } else {
            update(post);
        }
    }

    private static void insert(Post post) throws SQLException {
        String sql = "INSERT INTO publication (contenu, date_pb, is_anonymous, image_urls, category, view_count, status, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, post.getContent());
            stmt.setTimestamp(2, Timestamp.valueOf(post.getPublishDate()));
            stmt.setBoolean(3, post.isAnonymous());
            stmt.setString(4, convertImageUrlsToJson(post.getImageUrls()));
            stmt.setString(5, post.getCategory());
            stmt.setInt(6, 0);
            stmt.setString(7, post.getStatus() != null ? post.getStatus() : "pending");
            stmt.setInt(8, post.getUser().getId());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                post.setId(rs.getInt(1));
            }
        }
    }

    private static void update(Post post) throws SQLException {
        String sql = "UPDATE publication SET contenu=?, is_anonymous=?, image_urls=?, category=?, status=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, post.getContent());
            stmt.setBoolean(2, post.isAnonymous());
            stmt.setString(3, convertImageUrlsToJson(post.getImageUrls()));
            stmt.setString(4, post.getCategory());
            stmt.setString(5, post.getStatus());
            stmt.setInt(6, post.getId());
            
            stmt.executeUpdate();
        }
    }

    private static String convertImageUrlsToJson(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return "[]";
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < imageUrls.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(imageUrls.get(i).replace("\"", "\\\"")).append("\"");
        }
        json.append("]");
        return json.toString();
    }

    private static List<String> parseImageUrlsFromJson(String json) {
        List<String> urls = new ArrayList<>();
        if (json == null || json.equals("[]")) {
            return urls;
        }

        json = json.substring(1, json.length() - 1);
        if (!json.isEmpty()) {
            for (String url : json.split(",")) {
                urls.add(url.trim().replace("\"", ""));
            }
        }
        return urls;
    }

    private static Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getInt("id"));
        post.setContent(rs.getString("contenu"));
        post.setPublishDate(rs.getTimestamp("date_pb").toLocalDateTime());
        post.setAnonymous(rs.getBoolean("is_anonymous"));
        post.setImageUrls(parseImageUrlsFromJson(rs.getString("image_urls")));
        post.setCategory(rs.getString("category"));
        post.setViewCount(rs.getInt("view_count"));
        post.setStatus(rs.getString("status"));

        User user = new User();
        user.setId(rs.getInt("user_id"));
        // Remove username setting since we're not fetching it
        post.setUser(user);

        return post; // Remove loadImages(post) call
    }

    // Remove the loadImages method since we don't need it anymore

    // Add the findAll method
    public static List<Post> findAll() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.* FROM publication p " +
                "ORDER BY p.date_pb DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                posts.add(mapResultSetToPost(rs));
            }
        }
        return posts;
    }

    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM publication WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Counts the total number of posts in the database
     * @return The total number of posts
     * @throws SQLException If a database error occurs
     */
    public static int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM publication";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    // Add these methods to your PostDAO class

    // Update SQL queries to use "publication" table instead of "post"
    public static List<Post> findByStatus(String status) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM publication WHERE status = ? ORDER BY date_pb DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapResultSetToPost(rs);
                    posts.add(post);
                }
            }
        }
        
        return posts;
    }
    
    public static List<Post> findByUserId(int userId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM publication WHERE user_id = ? ORDER BY date_pb DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapResultSetToPost(rs);
                    posts.add(post);
                }
            }
        }
        
        return posts;
    }
    
    public static void updateStatus(int postId, String status) throws SQLException {
        String query = "UPDATE publication SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, postId);
            
            stmt.executeUpdate();
        }
    }
    
    // Add a method to find pending posts for a specific user
    public static List<Post> findPendingByUserId(int userId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM publication WHERE user_id = ? AND status = 'pending' ORDER BY date_pb DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapResultSetToPost(rs);
                    posts.add(post);
                }
            }
        }
        
        return posts;
    }

    // Add this method to find both approved posts and user's pending posts
    public static List<Post> findApprovedAndUserPending(int userId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM publication WHERE status = 'approved' OR (status = 'pending' AND user_id = ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Post post = mapResultSetToPost(rs);
                posts.add(post);
            }
        }
        
        return posts;
    }

    /**
     * Counts the number of posts with a specific status
     * @param status The status to count (e.g., "pending", "approved")
     * @return The number of posts with the specified status
     * @throws SQLException If a database error occurs
     */
    public static int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM publication WHERE status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    // Remove this duplicate findByUserId method - REMOVE THIS ENTIRE METHOD
    
    // Fix this method to use "publication" instead of "post"
    public static List<Post> findAll(boolean includePending) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query;
        
        if (includePending) {
            query = "SELECT * FROM publication ORDER BY date_pb DESC";
        } else {
            query = "SELECT * FROM publication WHERE status = 'approved' ORDER BY date_pb DESC";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Post post = mapResultSetToPost(rs);
                posts.add(post);
            }
        }
        
        return posts;
    }
}