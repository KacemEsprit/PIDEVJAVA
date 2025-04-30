package com.pfe.nova.configuration;
import com.pfe.nova.models.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public static int createTask(Task task) throws SQLException {
        if (task == null || task.getName() == null || task.getName().isEmpty()) {
            throw new SQLException("La tâche est invalide.");
        }

        String sql = "INSERT INTO task (name, completed) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, task.getName());
            pstmt.setBoolean(2, task.isCompleted());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int taskId = rs.getInt(1);
                        task.setId(taskId);
                        return taskId;
                    }
                }
            }

            throw new SQLException("Échec de la création de la tâche.");
        }
    }

    public static List<Task> getAllTasks() throws SQLException {
        String sql = "SELECT * FROM task ORDER BY id DESC";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBoolean("completed")
                );
                tasks.add(task);
            }
        }

        return tasks;
    }

    public static Task getTaskById(int id) throws SQLException {
        String sql = "SELECT * FROM task WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Task(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getBoolean("completed")
                    );
                }
            }
        }

        return null;
    }

    public static boolean updateTask(Task task) throws SQLException {
        if (task == null || task.getId() <= 0) {
            throw new SQLException("Tâche invalide.");
        }

        String sql = "UPDATE task SET name = ?, completed = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getName());
            pstmt.setBoolean(2, task.isCompleted());
            pstmt.setInt(3, task.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public static boolean deleteTask(int id) throws SQLException {
        String sql = "DELETE FROM task WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public static boolean updateTaskStatus(int id, boolean completed) throws SQLException {
        String sql = "UPDATE task SET completed = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, completed);
            pstmt.setInt(2, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}
