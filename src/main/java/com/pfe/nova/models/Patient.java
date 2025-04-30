package com.pfe.nova.models;

public class Patient extends User {
    private Integer age;
    private String gender;
    private String bloodType;

    public Patient() {
        super();
        setRole("ROLE_PATIENT");
    }

    public Patient(int id, String nom, String prenom, String email, String tel, String adresse,
                  String password, String picture, Integer age, String gender, String bloodType) {
        super(id, nom, prenom, email, tel, adresse, password, picture, "ROLE_PATIENT");
        this.age = age;
        this.gender = gender;
        this.bloodType = bloodType;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
}