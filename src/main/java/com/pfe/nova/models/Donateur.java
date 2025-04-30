package com.pfe.nova.models;

public class Donateur extends User {
    private String donateurType;

    public Donateur() {
        super();
        setRole("ROLE_DONATEUR");
    }

    public Donateur(int id, String nom, String prenom, String email, String tel, String adresse,
                   String password, String picture, String donateurType) {
        super(id, nom, prenom, email, tel, adresse, password, picture, "ROLE_DONATEUR");
        this.donateurType = donateurType;
    }

    public String getDonateurType() {
        return donateurType;
    }

    public void setDonateurType(String donateurType) {
        this.donateurType = donateurType;
    }
}