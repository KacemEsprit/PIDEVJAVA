package com.pfe.nova.models;

import java.sql.Date;

public class Don {
    private int id;
    private String typeDon;
    private double montant;
    private String descriptionMateriel;
    private Date dateDon;
    private int donateurId;
    private int campagneId;
    private String modePaiement;
    private String preuveDon;
    private String statut = "EN_ATTENTE";

    // Constructor without 'id' parameter
    public Don(String typeDon, double montant, String descriptionMateriel, Date dateDon, int donateurId, int campagneId, String modePaiement, String preuveDon) {
        this.typeDon = typeDon;
        this.montant = montant;
        this.descriptionMateriel = descriptionMateriel;
        this.dateDon = dateDon;
        this.donateurId = donateurId;
        this.campagneId = campagneId;
        this.modePaiement = modePaiement;
        this.preuveDon = preuveDon;
        this.statut = "EN_ATTENTE";
    }

    // Constructor with compagnieId parameter
    public Don(String typeDon, double montant, String descriptionMateriel, Date dateDon, int donateurId, int campagneId, int compagnieId, String modePaiement, String preuveDon) {
        this.typeDon = typeDon;
        this.montant = montant;
        this.descriptionMateriel = descriptionMateriel;
        this.dateDon = dateDon;
        this.donateurId = donateurId;
        this.campagneId = campagneId;
        this.modePaiement = modePaiement;
        this.preuveDon = preuveDon;
        this.statut = "EN_ATTENTE";
    }

    // Constructor with 'id' parameter
    public Don(int id, String typeDon, double montant, String descriptionMateriel, Date dateDon, int donateurId, int campagneId, String modePaiement, String preuveDon) {
        this.id = id;
        this.typeDon = typeDon;
        this.montant = montant;
        this.descriptionMateriel = descriptionMateriel;
        this.dateDon = dateDon;
        this.donateurId = donateurId;
        this.campagneId = campagneId;
        this.modePaiement = modePaiement;
        this.preuveDon = preuveDon;
        this.statut = "EN_ATTENTE";
    }

    // Getters
    public int getId() { return id; }
    public String getTypeDon() { return typeDon; }
    public double getMontant() { return montant; }
    public String getDescriptionMateriel() { return descriptionMateriel; }
    public Date getDateDon() { return dateDon; }
    public int getDonateurId() { return donateurId; }
    public int getCampagneId() { return campagneId; }
    public String getModePaiement() { return modePaiement; }
    public String getPreuveDon() { return preuveDon; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTypeDon(String typeDon) { this.typeDon = typeDon; }
    public void setMontant(double montant) { this.montant = montant; }
    public void setDescriptionMateriel(String descriptionMateriel) { this.descriptionMateriel = descriptionMateriel; }
    public void setDateDon(Date dateDon) { this.dateDon = dateDon; }
    public void setDonateurId(int donateurId) { this.donateurId = donateurId; }
    public void setCampagneId(int campagneId) { this.campagneId = campagneId; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }
    public void setPreuveDon(String preuveDon) { this.preuveDon = preuveDon; }

    // Getter and Setter for statut
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}