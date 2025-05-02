package com.pfe.nova.models;

public class RapportRendezVous {
    private int id;
    private int rendezVousId;
    private int medecinId;
    private String dateRapport;
    private String contenu;
    private String complications;

    public RapportRendezVous() {
    }

    public RapportRendezVous(int id, int rendezVousId, int medecinId, String dateRapport, String contenu, String complications) {
        this.id = id;
        this.rendezVousId = rendezVousId;
        this.medecinId = medecinId;
        this.dateRapport = dateRapport;
        this.contenu = contenu;
        this.complications = complications;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRendezVousId() {
        return rendezVousId;
    }

    public void setRendezVousId(int rendezVousId) {
        this.rendezVousId = rendezVousId;
    }

    public int getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(int medecinId) {
        this.medecinId = medecinId;
    }

    public String getDateRapport() {
        return dateRapport;
    }

    public void setDateRapport(String dateRapport) {
        this.dateRapport = dateRapport;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getComplications() {
        return complications;
    }

    public void setComplications(String complications) {
        this.complications = complications;
    }

    @Override
    public String toString() {
        return "RapportRendezVous{" +
                "id=" + id +
                ", rendezVousId=" + rendezVousId +
                ", medecinId=" + medecinId +
                ", dateRapport='" + dateRapport + '\'' +
                ", contenu='" + contenu + '\'' +
                ", complications='" + complications + '\'' +
                '}';
    }
}