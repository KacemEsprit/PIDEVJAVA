package com.pfe.nova.configuration;

import com.pfe.nova.models.User;

/**
 * Utility class to manage user session information
 */
public class Session {
    private static User utilisateurConnecte;
    
    /**
     * Set the currently connected user
     * @param user The user to set as connected
     */
    public static void setUtilisateurConnecte(User user) {
        utilisateurConnecte = user;
    }
    
    /**
     * Get the currently connected user
     * @return The connected user or null if no user is connected
     */
    public static User getUtilisateurConnecte() {
        return utilisateurConnecte;
    }
    
    /**
     * Clear the current user session
     */
    public static void clearSession() {
        utilisateurConnecte = null;
    }
}