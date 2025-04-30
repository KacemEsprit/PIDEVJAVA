package com.pfe.nova.services;

import com.pfe.nova.models.Post;
import com.pfe.nova.models.User;

public class EmailPostTemplateService {
    
    /**
     * Génère un template HTML pour la notification d'approbation de post
     * 
     * @param user L'utilisateur propriétaire du post
     * @param post Le post qui a été approuvé
     * @return Le contenu HTML de l'email
     */
    public static String getPostApprovalTemplate(User user, Post post) {
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <style>\n" +
               "        body {\n" +
               "            font-family: Arial, sans-serif;\n" +
               "            line-height: 1.6;\n" +
               "            color: #333;\n" +
               "            max-width: 600px;\n" +
               "            margin: 0 auto;\n" +
               "            padding: 20px;\n" +
               "        }\n" +
               "        .header {\n" +
               "            background-color: #95E1D3;\n" +
               "            color: white;\n" +
               "            padding: 20px;\n" +
               "            text-align: center;\n" +
               "            border-radius: 5px 5px 0 0;\n" +
               "        }\n" +
               "        .content {\n" +
               "            padding: 20px;\n" +
               "            border: 1px solid #ddd;\n" +
               "            border-top: none;\n" +
               "            border-radius: 0 0 5px 5px;\n" +
               "        }\n" +
               "        .post-content {\n" +
               "            background-color: #f9f9f9;\n" +
               "            padding: 15px;\n" +
               "            border-left: 4px solid #95E1D3;\n" +
               "            margin: 15px 0;\n" +
               "        }\n" +
               "        .footer {\n" +
               "            text-align: center;\n" +
               "            margin-top: 20px;\n" +
               "            font-size: 12px;\n" +
               "            color: #777;\n" +
               "        }\n" +
               "        .button {\n" +
               "            display: inline-block;\n" +
               "            background-color: #95E1D3;\n" +
               "            color: white;\n" +
               "            padding: 10px 20px;\n" +
               "            text-decoration: none;\n" +
               "            border-radius: 5px;\n" +
               "            margin-top: 15px;\n" +
               "        }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"header\">\n" +
               "        <h1>Votre publication a été approuvée</h1>\n" +
               "    </div>\n" +
               "    <div class=\"content\">\n" +
               "        <p>Bonjour <strong>" + user.getPrenom() + "</strong>,</p>\n" +
               "        <p>Nous sommes heureux de vous informer que votre publication a été approuvée par notre équipe NovaSpark de l'hopital ONCOKIDSCARE.</p>\n" +
               "        <div class=\"post-content\">\n" +
               "            <p><em>\"" + post.getContent() + "\"</em></p>\n" +
               "        </div>\n" +
               "        <p>Votre publication est maintenant visible par tous les patietns et medecins de la plateforme.</p>\n" +
               "        <p>Merci de votre contribution à notre communauté!</p>\n" +
               "" +
               "    </div>\n" +
               "    <div class=\"footer\">\n" +
               "        <p>© 2025 NOVASPARK. Tous droits réservés.ONCOKIDSCARE</p>\n" +
               "        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }
}