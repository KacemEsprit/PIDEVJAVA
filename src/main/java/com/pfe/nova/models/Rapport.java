package com.pfe.nova.models;

public class Rapport {
    private int id;
    private int patientId;
    private int medecinId;

    private int age;
    private String dateRapport;
    private String sexe;
    private int tensionArterielle;
    private int pouls;
    private double temperature;
    private int saturationOxygene;
    private double imc;
    private int niveauDouleur;
    private String traitement;
    private int doseMedicament;
    private String frequenceTraitement;
    private int perteDeSang;
    private int tempsOperation;
    private int dureeSeance;
    private int filtrationSang;
    private int creatinine;
    private int scoreGlasgow;
    private int respirationAssistee;
    private String complications;


    public Rapport() {
    }

    public Rapport(int id, int patientId, int medecinId, int age, String dateRapport, String sexe, int tensionArterielle, int pouls, double temperature, int saturationOxygene, double imc, int niveauDouleur, String traitement, int doseMedicament, String frequenceTraitement, int perteDeSang, int tempsOperation, int dureeSeance, int filtrationSang, int creatinine, int scoreGlasgow,int respirationAssistee, String complications) {
        this.id = id;
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.age = age;
        this.dateRapport = dateRapport;
        this.sexe = sexe;
        this.tensionArterielle = tensionArterielle;
        this.pouls = pouls;
        this.temperature = temperature;
        this.saturationOxygene = saturationOxygene;
        this.imc = imc;
        this.niveauDouleur = niveauDouleur;
        this.traitement = traitement;
        this.doseMedicament = doseMedicament;
        this.frequenceTraitement = frequenceTraitement;
        this.perteDeSang = perteDeSang;
        this.tempsOperation = tempsOperation;
        this.dureeSeance = dureeSeance;
        this.filtrationSang = filtrationSang;
        this.creatinine = creatinine;
        this.scoreGlasgow = scoreGlasgow;
        this.respirationAssistee = respirationAssistee;
        this.complications = complications;
    }


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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDateRapport() {
        return dateRapport;
    }

    public void setDateRapport(String dateRapport) {
        this.dateRapport = dateRapport;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public int getTensionArterielle() {
        return tensionArterielle;
    }

    public void setTensionArterielle(int tensionArterielle) {
        this.tensionArterielle = tensionArterielle;
    }

    public int getPouls() {
        return pouls;
    }

    public void setPouls(int pouls) {
        this.pouls = pouls;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getSaturationOxygene() {
        return saturationOxygene;
    }

    public void setSaturationOxygene(int saturationOxygene) {
        this.saturationOxygene = saturationOxygene;
    }

    public double getImc() {
        return imc;
    }

    public void setImc(double imc) {
        this.imc = imc;
    }

    public int getNiveauDouleur() {
        return niveauDouleur;
    }

    public void setNiveauDouleur(int niveauDouleur) {
        this.niveauDouleur = niveauDouleur;
    }

    public String getTraitement() {
        return traitement;
    }

    public void setTraitement(String traitement) {
        this.traitement = traitement;
    }

    public int getDoseMedicament() {
        return doseMedicament;
    }

    public void setDoseMedicament(int doseMedicament) {
        this.doseMedicament = doseMedicament;
    }

    public String getFrequenceTraitement() {
        return frequenceTraitement;
    }

    public void setFrequenceTraitement(String frequenceTraitement) {
        this.frequenceTraitement = frequenceTraitement;
    }

    public int getPerteDeSang() {
        return perteDeSang;
    }

    public void setPerteDeSang(int perteDeSang) {
        this.perteDeSang = perteDeSang;
    }

    public int getTempsOperation() {
        return tempsOperation;
    }

    public void setTempsOperation(int tempsOperation) {
        this.tempsOperation = tempsOperation;
    }

    public int getDureeSeance() {
        return dureeSeance;
    }

    public void setDureeSeance(int dureeSeance) {
        this.dureeSeance = dureeSeance;
    }

    public int getFiltrationSang() {
        return filtrationSang;
    }

    public void setFiltrationSang(int filtrationSang) {
        this.filtrationSang = filtrationSang;
    }

    public int getCreatinine() {
        return creatinine;
    }

    public void setCreatinine(int creatinine) {
        this.creatinine = creatinine;
    }

    public int getScoreGlasgow() {
        return scoreGlasgow;
    }

    public void setScoreGlasgow(int scoreGlasgow) {
        this.scoreGlasgow = scoreGlasgow;
    }

    public int isRespirationAssistee() {
        return respirationAssistee;
    }

    public void setRespirationAssistee(int respirationAssistee) {
        this.respirationAssistee = respirationAssistee;
    }

    public String getComplications() {
        return complications;
    }

    public void setComplications(String complications) {
        this.complications = complications;
    }

    @Override
    public String toString() {
        return "Rapport{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", medecinId=" + medecinId +
                ", age=" + age +
                ", dateRapport='" + dateRapport + '\'' +
                ", sexe='" + sexe + '\'' +
                ", tensionArterielle=" + tensionArterielle +
                ", pouls=" + pouls +
                ", temperature=" + temperature +
                ", saturationOxygene=" + saturationOxygene +
                ", imc=" + imc +
                ", niveauDouleur=" + niveauDouleur +
                ", traitement='" + traitement + '\'' +
                ", doseMedicament=" + doseMedicament +
                ", frequenceTraitement='" + frequenceTraitement + '\'' +
                ", perteDeSang=" + perteDeSang +
                ", tempsOperation=" + tempsOperation +
                ", dureeSeance=" + dureeSeance +
                ", filtrationSang=" + filtrationSang +
                ", creatinine=" + creatinine +
                ", scoreGlasgow=" + scoreGlasgow +
                ", respirationAssistee=" + respirationAssistee +
                ", complications='" + complications + '\'' +
                '}';
    }
}
