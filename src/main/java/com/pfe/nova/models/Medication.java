package com.pfe.nova.models;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Medication implements Serializable {
    // Add a serialVersionUID to ensure version compatibility
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String nom;
    private String description;
    private int quantiteStock;
    private double prix;
    private String imagePath;
    // Ajout de la liste des commandes
    private List<Order> orders;
    // Quantité commandée (utilisée temporairement pour les commandes)
    private int quantiteCommande = 1;

    public Medication() {
        this.orders = new ArrayList<>();
    }

    public Medication(String nom, String description, int quantiteStock, double prix) {
        this.nom = nom;
        this.description = description;
        this.quantiteStock = quantiteStock;
        this.prix = prix;
        this.orders = new ArrayList<>();
    }

    public Medication(int id, String nom, String description, int quantiteStock, double prix) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.quantiteStock = quantiteStock;
        this.prix = prix;
        this.orders = new ArrayList<>();
    }

    // Méthodes pour gérer la relation
    public void addOrder(Order order) {
        if (!this.orders.contains(order)) {
            this.orders.add(order);
        }
    }
    
    public void removeOrder(Order order) {
        this.orders.remove(order);
    }
    
    // Getters et setters existants
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(int quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    // Getter et setter pour la liste des commandes
    public List<Order> getOrders() {
        return orders;
    }
    
    // Getter et setter pour la quantité commandée
    public int getQuantiteCommande() {
        return quantiteCommande;
    }
    
    public void setQuantiteCommande(int quantiteCommande) {
        this.quantiteCommande = quantiteCommande;
    }
    
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}