package com.pfe.nova.models;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonString;
import com.google.api.client.util.Key;

public class GoogleUserInfo {
    @Key("email")
    private String email;
    
    @Key("given_name")
    private String givenName;
    
    @Key("family_name")
    private String familyName;
    
    @Key("picture")
    private String picture;
    
    // Getters
    public String getEmail() {
        return email;
    }
    
    public String getGivenName() {
        return givenName;
    }
    
    public String getFamilyName() {
        return familyName;
    }
    
    public String getPicture() {
        return picture;
    }
}