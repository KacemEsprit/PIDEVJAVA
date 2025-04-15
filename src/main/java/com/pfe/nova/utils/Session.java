package com.pfe.nova.utils;

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
}
