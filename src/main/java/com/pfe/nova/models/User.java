package com.pfe.nova.models;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private Role role;


    public User() {
    }
    public User( String username, String password, String email,Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role=role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
