package com.pfe.nova.models;

import java.time.LocalDateTime;

public class Chat {
    private int id;
    private int userId;
    private String username;
    private String message;
    private LocalDateTime timestamp;

    public Chat(int id, int userId, String username, String message, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Chat(int userId, String username, String message, LocalDateTime timestamp) {
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}