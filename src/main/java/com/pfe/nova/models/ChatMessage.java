package com.pfe.nova.models;

import org.json.JSONObject;

import java.time.LocalDateTime;

public class ChatMessage {
    private int id;
    private User sender;
    private int userId;
    private String username;
    private String message;
    private String content;
    private LocalDateTime timestamp;
    private String channel;
    private String channelName;
    private boolean isLocalMessage = false; // Pour identifier les messages envoyés localement
    
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ChatMessage(User sender, String message, String channel) {
        this();
        this.sender = sender;
        this.message = message;
        this.content = message; // For compatibility
        this.channel = channel;
        this.channelName = channel; // For compatibility
        if (sender != null) {
            this.userId = sender.getId();
            this.username = sender.getUsername();
        }
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public User getSender() {
        return sender;
    }
    
    public void setSender(User sender) {
        this.sender = sender;
        if (sender != null) {
            this.userId = sender.getId();
            this.username = sender.getUsername();
        }
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
        this.content = message; // Keep content in sync
    }
    
    public String getContent() {
        return content != null ? content : message;
    }
    
    public void setContent(String content) {
        this.content = content;
        this.message = content; // Keep message in sync
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getChannel() {
        return channel != null ? channel : channelName;
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
        this.channelName = channel; // Keep channelName in sync
    }
    
    public String getChannelName() {
        return channelName != null ? channelName : channel;
    }
    
    public void setChannelName(String channelName) {
        this.channelName = channelName;
        this.channel = channelName; // Keep channel in sync
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
    
    public boolean isLocalMessage() {
        return isLocalMessage;
    }
    
    public void setLocalMessage(boolean localMessage) {
        isLocalMessage = localMessage;
    }
    
    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", content='" + getContent() + '\'' +
                ", timestamp=" + timestamp +
                ", channel='" + getChannel() + '\'' +
                '}';
    }

    public static ChatMessage fromJson(JSONObject json) {
        ChatMessage message = new ChatMessage();
        
        if (json.has("id")) {
            message.setId(json.getInt("id"));
        }
        
        if (json.has("userId")) {
            message.setUserId(json.getInt("userId"));
        }
        
        if (json.has("username")) {
            message.setUsername(json.getString("username"));
        }
        
        if (json.has("channelName")) {
            message.setChannelName(json.getString("channelName"));
        }
        
        if (json.has("content")) {
            message.setContent(json.getString("content"));
        }
        
        if (json.has("timestamp")) {
            message.setTimestamp(LocalDateTime.parse(json.getString("timestamp")));
        }
        
        return message;
    }
}