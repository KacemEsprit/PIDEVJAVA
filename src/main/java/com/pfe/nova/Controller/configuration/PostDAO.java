package com.pfe.nova.Controller.configuration;

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
            stmt.setString(4, convertImageUrlsToJson(post.getImageUrls())); // Convert list to JSON
            stmt.setString(5, post.getCategory());
            stmt.setInt(6, 0); // Initial view count
            stmt.setString(7, "pending"); // Default status
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
        // Simple JSON array parsing
        json = json.substring(1, json.length() - 1); // Remove [ ]
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

    public static int countAll() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM publication";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
}