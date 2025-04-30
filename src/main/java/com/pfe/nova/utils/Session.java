package com.pfe.nova.utils;

import com.pfe.nova.models.User;


public class Session {
    private static Session instance;
    private static User currentUser;
    
    private Session() {
    }
    

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
    

    public static void setCurrentUser(User user) {
        currentUser = user;  // Remove 'this.' since currentUser is static
    }
    

    public static User getCurrentUser() {
        return currentUser;
    }
    

    public static User getCurrentUserStatic() {
        return getInstance().getCurrentUser();
    }
    

    public boolean isLoggedIn() {
        return currentUser != null;
    }
    

    public void logout() {
        currentUser = null;  // Remove 'this.' since currentUser is static
    }
    

    public static User getUtilisateurConnecte() {
        return getCurrentUser();
    }
    

    public static void setUtilisateurConnecte(User user) {
        setCurrentUser(user);
    }
}
