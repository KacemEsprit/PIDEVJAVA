package com.pfe.nova.models;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private int userId;
    private int publicationId;
    private LocalDateTime createdAt;
    private String contenuCom;
    private String voiceUrl;
    private String type;
    private Integer duration;
    // Add these fields and methods to your Comment class
    private boolean reported;
    private String reportReason;
    
    public boolean isReported() {
        return reported;
    }
    
    public void setReported(boolean reported) {
        this.reported = reported;
    }
    
    public String getReportReason() {
        return reportReason;
    }
    
    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }
    private User user;

    public Comment() {
        this.createdAt = LocalDateTime.now();
        this.type = "text";
        this.reported = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getPublicationId() { return publicationId; }
    public void setPublicationId(int publicationId) { this.publicationId = publicationId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getContenuCom() { return contenuCom; }
    public void setContenuCom(String contenuCom) { this.contenuCom = contenuCom; }
    
    public String getVoiceUrl() { return voiceUrl; }
    public void setVoiceUrl(String voiceUrl) { this.voiceUrl = voiceUrl; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}