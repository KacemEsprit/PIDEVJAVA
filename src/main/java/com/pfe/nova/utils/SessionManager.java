package com.pfe.nova.utils;

import com.pfe.nova.models.User;

import java.util.logging.Logger;

public class SessionManager {
    private static User currentUser;
    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static boolean isInitialized = false;

    /**
     * Retrieves the current user from the session.
     *
     * @return the current logged-in User, or null if no user is logged in.
     */
    public static User getCurrentUser() {
        if (currentUser == null) {
            LOGGER.warning("Tentative d'accès à la session alors qu'aucun utilisateur n'est connecté");
            return null;
        }
        LOGGER.info("Utilisateur actuel récupéré : " + currentUser.getEmail());
        return currentUser;
    }

    /**
     * Sets the current user in the session.
     *
     * @param user the User to set as the current session user.
     */
    public static void setCurrentUser(User user) {
        if (user != null) {
            currentUser = user;
            isInitialized = true;
            LOGGER.info("Utilisateur connecté avec succès: " + user.getEmail() + ", Role: " + user.getRole());
        } else {
            LOGGER.warning("Tentative de définir un utilisateur null");
        }
    }

    /**
     * Clears the current session, logging the user out.
     */
    public static void clearSession() {
        if (currentUser != null) {
            LOGGER.info("Déconnexion de l'utilisateur : " + currentUser.getEmail());
        } else {
            LOGGER.warning("Tentative de déconnexion alors qu'aucun utilisateur n'est connecté");
        }
        currentUser = null;
        isInitialized = false;
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise.
     */
    public static boolean isUserLoggedIn() {
        boolean loggedIn = currentUser != null && isInitialized;
        if (loggedIn) {
            LOGGER.info("Utilisateur connecté : " + currentUser.getEmail());
        } else {
            LOGGER.warning("Aucun utilisateur connecté");
        }
        return loggedIn;
    }
}