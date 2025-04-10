package com.pfe.nova.models;

import java.time.LocalDate;

public class Rapport {
    private int id;
    private int patientId;
    private int medecinId;
    private LocalDate dateRapport;
    private String sexe;
    private int age;
    private String traitement;
    private String doseMedicament;
    private String frequenceTraitement;
    private String perteDeSang;
    private String tempsOperation;
    private String filtrationSang;
    private String creatinine;
    private String scoreGlasgow;
    private String respirationAssistee;
    private String complications;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(int medecinId) {
        this.medecinId = medecinId;
    }

    public LocalDate getDateRapport() {
        return dateRapport;
    }

    public void setDateRapport(LocalDate dateRapport) {
        this.dateRapport = dateRapport;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getTraitement() {
        return traitement;
    }

    public void setTraitement(String traitement) {
        this.traitement = traitement;
    }

    public String getDoseMedicament() {
        return doseMedicament;
    }

    public void setDoseMedicament(String doseMedicament) {
        this.doseMedicament = doseMedicament;
    }

    public String getFrequenceTraitement() {
        return frequenceTraitement;
    }

    public void setFrequenceTraitement(String frequenceTraitement) {
        this.frequenceTraitement = frequenceTraitement;
    }

    public String getPerteDeSang() {
        return perteDeSang;
    }

    public void setPerteDeSang(String perteDeSang) {
        this.perteDeSang = perteDeSang;
    }

    public String getTempsOperation() {
        return tempsOperation;
    }

    public void setTempsOperation(String tempsOperation) {
        this.tempsOperation = tempsOperation;
    }

    public String getFiltrationSang() {
        return filtrationSang;
    }

    public void setFiltrationSang(String filtrationSang) {
        this.filtrationSang = filtrationSang;
    }

    public String getCreatinine() {
        return creatinine;
    }

    public void setCreatinine(String creatinine) {
        this.creatinine = creatinine;
    }

    public String getScoreGlasgow() {
        return scoreGlasgow;
    }

    public void setScoreGlasgow(String scoreGlasgow) {
        this.scoreGlasgow = scoreGlasgow;
    }

    public String getRespirationAssistee() {
        return respirationAssistee;
    }

    public void setRespirationAssistee(String respirationAssistee) {
        this.respirationAssistee = respirationAssistee;
    }

    public String getComplications() {
        return complications;
    }

    public void setComplications(String complications) {
        this.complications = complications;
    }

    public Rapport(int id, int patientId, int medecinId, LocalDate dateRapport, String sexe, int age, String traitement, String doseMedicament, String frequenceTraitement, String perteDeSang, String tempsOperation, String filtrationSang, String creatinine, String scoreGlasgow, String respirationAssistee, String complications) {
        this.id = id;
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.dateRapport = dateRapport;
        this.sexe = sexe;
        this.age = age;
        this.traitement = traitement;
        this.doseMedicament = doseMedicament;
        this.frequenceTraitement = frequenceTraitement;
        this.perteDeSang = perteDeSang;
        this.tempsOperation = tempsOperation;
        this.filtrationSang = filtrationSang;
        this.creatinine = creatinine;
        this.scoreGlasgow = scoreGlasgow;
        this.respirationAssistee = respirationAssistee;
        this.complications = complications;
    }

    public Rapport() {
    }

    @Override
    public String toString() {
        return "Rapport{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", medecinId=" + medecinId +
                ", dateRapport=" + dateRapport +
                ", sexe='" + sexe + '\'' +
                ", age=" + age +
                ", traitement='" + traitement + '\'' +
                ", doseMedicament='" + doseMedicament + '\'' +
                ", frequenceTraitement='" + frequenceTraitement + '\'' +
                ", perteDeSang='" + perteDeSang + '\'' +
                ", tempsOperation='" + tempsOperation + '\'' +
                ", filtrationSang='" + filtrationSang + '\'' +
                ", creatinine='" + creatinine + '\'' +
                ", scoreGlasgow='" + scoreGlasgow + '\'' +
                ", respirationAssistee='" + respirationAssistee + '\'' +
                ", complications='" + complications + '\'' +
                '}';
    }

}