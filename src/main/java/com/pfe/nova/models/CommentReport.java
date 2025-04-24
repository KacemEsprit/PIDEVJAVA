package com.pfe.nova.models;

import java.time.LocalDateTime;

public class CommentReport {
    private int id;
    private int commentId;
    private int reporterId;
    private String reason;
    private LocalDateTime createdAt;

    // Additional fields for UI display
    private User reporter;
    private Comment comment;

    public CommentReport() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public int getReporterId() {
        return reporterId;
    }

    public void setReporterId(int reporterId) {
        this.reporterId = reporterId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    // Helper method to get a human-readable reason
    public String getFormattedReason() {
        switch (reason) {
            case "contenu_inapproprie":
                return "Inappropriate content";
            case "harcelement":
                return "Harassment";
            case "spam":
                return "Spam";
            case "fausse_information":
                return "False information";
            default:
                return reason;
        }
    }
}