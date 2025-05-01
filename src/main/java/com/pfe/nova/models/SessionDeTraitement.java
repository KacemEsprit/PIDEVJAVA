package com.pfe.nova.models;



public class SessionDeTraitement {
    private int id;
    private String dateSession;
    private String typeSession;
    private int numDeChambre;
    private int patientId;
    private int duree;
    private int medecinId;

    public SessionDeTraitement(int id, String dateSession, String typeSession, int numDeChambre, int patientId, int duree, int medecinId) {
        this.id = id;
        this.dateSession = dateSession;
        this.typeSession = typeSession;
        this.numDeChambre = numDeChambre;
        this.patientId = patientId;
        this.duree = duree;
        this.medecinId = medecinId;
    }

    public SessionDeTraitement(String dateSession, String typeSession, int numDeChambre, int patientId, int duree, int medecinId) {
        this.dateSession = dateSession;
        this.typeSession = typeSession;
        this.numDeChambre = numDeChambre;
        this.patientId = patientId;
        this.duree = duree;
        this.medecinId = medecinId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDateSession() { return dateSession; }
    public void setDateSession(String dateSession) { this.dateSession = dateSession; }
    public String getTypeSession() { return typeSession; }
    public void setTypeSession(String typeSession) { this.typeSession = typeSession; }
    public int getNumDeChambre() { return numDeChambre; }
    public void setNumDeChambre(int numDeChambre) { this.numDeChambre = numDeChambre; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }
    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }
}
