package com.pfe.nova.models;

public class Task {
    private int id;
    private String name;
    private boolean completed;

    public Task() {}

    public Task(int id, String name, boolean completed) {
        this.id = id;
        this.name = name;
        this.completed = completed;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isCompleted() { return completed; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
