package com.pfe.nova.models;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String tel;
    private String adresse;
    private String password;
    private String picture;
    private String role;
    
    public User() {
    }

    public User(int id, String nom, String prenom, String email, String tel, String adresse, String password, String picture, String role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.tel = tel;
        this.adresse = adresse;
        this.password = password;
        this.picture = picture;
        this.role = role;
    }

    // Add role getter and setter
    // Make sure the getRole method is properly implemented
    public String getRole() {
        return role;
    }
    
    // Add a debug method to print user details
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    // Add a username getter that returns the name
    public String getUsername() {
        return nom;
    }
}
