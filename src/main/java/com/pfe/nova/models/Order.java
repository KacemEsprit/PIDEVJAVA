package com.pfe.nova.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String status;
    private LocalDateTime dateCommande;
    private int quantiteTotal;
    private double montantTotal;
    private List<Medication> medications;

    public Order() {
        this.dateCommande = LocalDateTime.now();
        this.medications = new ArrayList<>();
    }

    public Order(String status) {
        this.status = status;
        this.dateCommande = LocalDateTime.now();
        this.quantiteTotal = 0;
        this.montantTotal = 0.0;
        this.medications = new ArrayList<>();
    }

    public Order(int id, String status, LocalDateTime dateCommande, int quantiteTotal, double montantTotal) {
        this.id = id;
        this.status = status;
        this.dateCommande = dateCommande;
        this.quantiteTotal = quantiteTotal;
        this.montantTotal = montantTotal;
        this.medications = new ArrayList<>();
    }

    // Méthode pour ajouter un médicament avec une quantité spécifique
    public void addMedication(Medication medication, int quantite) {
        if (!this.medications.contains(medication)) {
            this.medications.add(medication);
            // Mettre à jour les totaux avec la quantité spécifiée
            quantiteTotal += quantite;
            montantTotal += medication.getPrix() * quantite;
            
            // Ajouter cette commande au médicament si ce n'est pas déjà fait
            if (!medication.getOrders().contains(this)) {
                medication.addOrder(this);
            }
        }
    }
    
    // Méthode pour ajouter un médicament (avec quantité par défaut = 1)
    public void addMedication(Medication medication) {
        addMedication(medication, 1);
    }

    // Méthode pour supprimer un médicament
    public void removeMedication(Medication medication) {
        if (this.medications.contains(medication)) {
            this.medications.remove(medication);
            // Mettre à jour les totaux
            quantiteTotal -= 1; // Par défaut, on retire 1 unité
            montantTotal -= medication.getPrix();
            
            // Supprimer cette commande du médicament si présente
            if (medication.getOrders().contains(this)) {
                medication.removeOrder(this);
            }
        }
    }

    // Méthode pour mettre à jour la quantité d'un médicament
    public void updateMedicationQuantity(Medication medication, int newQuantity) {
        if (medications.contains(medication)) {
            int oldQuantity = medication.getQuantiteCommande();
            // Mettre à jour la quantité totale
            quantiteTotal = quantiteTotal - oldQuantity + newQuantity;
            // Mettre à jour le montant total
            montantTotal = montantTotal - (medication.getPrix() * oldQuantity) + (medication.getPrix() * newQuantity);
            // Mettre à jour la quantité du médicament
            medication.setQuantiteCommande(newQuantity);
        }
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    public int getQuantiteTotal() {
        return quantiteTotal;
    }

    public void setQuantiteTotal(int quantiteTotal) {
        this.quantiteTotal = quantiteTotal;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
        
        // Recalculer les totaux
        quantiteTotal = 0;
        montantTotal = 0.0;
        
        if (medications != null) {
            for (Medication med : medications) {
                int quantite = med.getQuantiteCommande() > 0 ? med.getQuantiteCommande() : 1;
                quantiteTotal += quantite;
                montantTotal += med.getPrix() * quantite;
            }
        }
    }

    public String getItemsSummary() {
        if (medications == null || medications.isEmpty()) {
            return "Aucun article";
        }
        
        StringBuilder summary = new StringBuilder();
        for (Medication med : medications) {
            summary.append(med.getNom())
                  .append(" (")
                  .append(med.getQuantiteCommande())
                  .append(" x ")
                  .append(String.format("%.2f", med.getPrix()))
                  .append(" dt)")
                  .append("\n");
        }
        return summary.toString().trim();
    }

    public LocalDateTime getDate() {
        return getDateCommande();
    }
}