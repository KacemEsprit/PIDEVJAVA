package com.pfe.nova.utils;

import com.pfe.nova.models.User;

/**
 * Singleton class to manage user session
 */
public class Session {
    private static Session instance;
    private static User currentUser;
    
    // Private constructor to prevent instantiation
    private Session() {
        // Initialize session
    }
    
    /**
     * Get the singleton instance
     * @return Session instance
     */
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
    
    /**
     * Set the current user for this session
     * @param user The user to set as current
     */
    public static void setCurrentUser(User user) {
        currentUser = user;  // Remove 'this.' since currentUser is static
    }
    
    /**
     * Get the current logged in user
     * @return The current user or null if not logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get the current logged in user (static version)
     * @return The current user or null if not logged in
     */
    public static User getCurrentUserStatic() {
        return getInstance().getCurrentUser();
    }
    
    /**
     * Check if a user is logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Log out the current user
     */
    public void logout() {
        currentUser = null;  // Remove 'this.' since currentUser is static
    }
    
    /**
     * Alias for getCurrentUser() to maintain compatibility with existing code
     * @return The current user or null if not logged in
     */
    public static User getUtilisateurConnecte() {
        return getCurrentUser();
    }
    
    /**
     * Alias for setCurrentUser() to maintain compatibility with existing code
     * @param user The user to set as current
     */
    public static void setUtilisateurConnecte(User user) {
        setCurrentUser(user);
    }
}
