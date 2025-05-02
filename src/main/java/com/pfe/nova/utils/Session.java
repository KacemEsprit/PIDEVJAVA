package com.pfe.nova.utils;

import com.pfe.nova.models.Appointment;
import com.pfe.nova.models.User;

public class Session {
    private static User utilisateurConnecte;

    public static void setUtilisateurConnecte(User user) {
        utilisateurConnecte = user;
    }

    public static User getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static void logout() {
        utilisateurConnecte = null;
    }
 public static String  getCurrentUserRole() {
        return utilisateurConnecte.getRole();
    }
    public static User getCurrentUser() {
        return utilisateurConnecte;
    }

    public static void setCurrentUser(User currentUser) {
        Session.utilisateurConnecte = currentUser;
    }
}
