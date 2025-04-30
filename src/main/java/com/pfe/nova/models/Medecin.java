package com.pfe.nova.models;

public class Medecin extends User {
    private String specialite;
    private String experience;
    private String diplome;

    public Medecin() {
        super();
        setRole("ROLE_MEDECIN");
    }

    public Medecin(int id, String nom, String prenom, String email, String tel, String adresse, 
                   String password, String picture, String specialite, String experience, String diplome) {
        super(id, nom, prenom, email, tel, adresse, password, picture, "ROLE_MEDECIN");
        this.specialite = specialite;
        this.experience = experience;
        this.diplome = diplome;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getDiplome() {
        return diplome;
    }

    public void setDiplome(String diplome) {
        this.diplome = diplome;
    }
}