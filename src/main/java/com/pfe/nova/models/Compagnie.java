package com.pfe.nova.models;

public class Compagnie {

    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String siteWeb;
    private String description;
    private String logo;
    private String siret;
    private java.util.Date dateCreation;
    private String statut_juridique;
    private String statut_validation = "EN_ATTENTE";

    // Constructeur sans ID (pour les ajouts)
    public Compagnie(String nom, String adresse, String telephone, String email, String siteWeb, String description, String logo, String siret, String statut_juridique) {
        this.dateCreation = new java.util.Date();
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.siteWeb = siteWeb;
        this.description = description;
        this.logo = logo;
        this.siret = siret;
        this.statut_juridique = statut_juridique;
    }

    // Constructeur sans ID (pour les ajouts simplifiés)
    public Compagnie(String nom, String adresse, String telephone, String email, String siteWeb, String description, String logo, String siret) {
        this.dateCreation = new java.util.Date();
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.siteWeb = siteWeb;
        this.description = description;
        this.logo = logo;
        this.siret = siret;
        this.statut_juridique = "SARL"; // Valeur par défaut
    }

    // Constructeur avec ID (pour la récupération depuis la base de données)
    public Compagnie(int id, String nom, String adresse, String telephone, String email, String siteWeb, String description, String logo, String siret, String statut_juridique) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.siteWeb = siteWeb;
        this.description = description;
        this.logo = logo;
        this.siret = siret;
        this.statut_juridique = statut_juridique;
    }

    // Getters et setters
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

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getSiret() {
        return siret;
    }

    public void setSiret(String siret) {
        this.siret = siret;
    }

    public java.util.Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(java.util.Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatut_juridique() {
        return statut_juridique;
    }

    public void setStatut_juridique(String statut_juridique) {
        this.statut_juridique = statut_juridique;
    }

    public String getStatut_validation() {
        return statut_validation;
    }

    public void setStatut_validation(String statut_validation) {
        this.statut_validation = statut_validation;
    }


    @Override
    public String toString() {
        return "Compagnie{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", siteWeb='" + siteWeb + '\'' +
                ", description='" + description + '\'' +
                ", logo='" + logo + '\'' +
                '}';
    }
}
